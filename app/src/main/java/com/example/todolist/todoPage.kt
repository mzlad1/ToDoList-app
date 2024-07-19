package com.example.todolist

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ActivityTodoPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class todoPage : AppCompatActivity() {
    private lateinit var binding: ActivityTodoPageBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private val itemList: MutableList<Task> = mutableListOf()
    private val itemIds: MutableList<String> = mutableListOf()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            return
        }

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(this, itemList, itemIds)
        recyclerView.adapter = adapter

        binding.addButton.setOnClickListener {
            showAddItemDialog()
        }

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        binding.Logoutbutton.setOnClickListener {
            auth.signOut()
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.putString("username", "")
            editor.apply()

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        loadItemsFromFirestore()
    }

    private fun showAddItemDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_item)

        val editTextLabel: EditText = dialog.findViewById(R.id.editTextLabel)
        val editTextNewItem: EditText = dialog.findViewById(R.id.editTextNewItem)
        val editTextFullDescription: EditText = dialog.findViewById(R.id.editTextFullDescription)
        val buttonAdd: Button = dialog.findViewById(R.id.buttonAdd)
        val buttonClose: Button = dialog.findViewById(R.id.buttonClose)

        buttonAdd.setOnClickListener {
            val label = editTextLabel.text.toString() + "  | "
            val newItem = editTextNewItem.text.toString()
            val fullDescription = editTextFullDescription.text.toString()
            if (newItem.isNotEmpty()) {
                addItemToFirestore(label, newItem, fullDescription)
                dialog.dismiss()
            } else {
                editTextNewItem.error = "Item cannot be empty"
            }
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addItemToFirestore(label: String, item: String, fullDescription: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val task = hashMapOf(
                "label" to label,
                "item" to item,
                "fullDescription" to fullDescription
            )
            db.collection("users").document(currentUser.uid).collection("tasks")
                .add(task)
                .addOnSuccessListener { documentReference ->
                    itemList.add(Task(label, item, fullDescription))
                    itemIds.add(documentReference.id)
                    adapter.notifyItemInserted(itemList.size - 1)
                    Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadItemsFromFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).collection("tasks")
                .get()
                .addOnSuccessListener { result ->
                    itemList.clear()
                    itemIds.clear()
                    for (document in result) {
                        val label = document.getString("label")
                        val item = document.getString("item")
                        val fullDescription = document.getString("fullDescription")
                        if (label != null && item != null && fullDescription != null) {
                            itemList.add(Task(label, item, fullDescription))
                            itemIds.add(document.id)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Items loaded", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading items: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show()
        }
    }
}
