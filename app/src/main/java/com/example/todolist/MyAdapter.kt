package com.example.todolist
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Task(
    var label: String = "",
    var item: String = "",
    var fullDescription: String = ""
)

class MyAdapter(private val context: Context, private val itemList: MutableList<Task>, private val itemIds: MutableList<String>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.taskLabel.text = item.label
        holder.taskDescription.text = item.item
        holder.taskFullDescription.text = item.fullDescription

        holder.taskDescription.setOnClickListener {
            if (holder.taskFullDescription.visibility == View.GONE) {
                holder.taskFullDescription.visibility = View.VISIBLE
            } else {
                holder.taskFullDescription.visibility = View.GONE
            }
        }

        holder.updateButton.setOnClickListener {
            showEditItemDialog(position)
        }

        holder.deleteButton.setOnClickListener {
            deleteItem(position)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskLabel: TextView = itemView.findViewById(R.id.taskLabel)
        val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        val taskFullDescription: TextView = itemView.findViewById(R.id.taskFullDescription)
        val updateButton: ImageButton = itemView.findViewById(R.id.updateButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    private fun showEditItemDialog(position: Int) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_add_item)

        val editTextLabel: EditText = dialog.findViewById(R.id.editTextLabel)
        val editTextNewItem: EditText = dialog.findViewById(R.id.editTextNewItem)
        val editTextFullDescription: EditText = dialog.findViewById(R.id.editTextFullDescription)
        val buttonAdd: Button = dialog.findViewById(R.id.buttonAdd)
        val buttonClose: Button = dialog.findViewById(R.id.buttonClose)

        val item = itemList[position]
        editTextLabel.setText(item.label)
        editTextNewItem.setText(item.item)
        editTextFullDescription.setText(item.fullDescription)

        buttonAdd.text = "Update"
        buttonAdd.setOnClickListener {
            val updatedLabel = editTextLabel.text.toString()
            val updatedText = editTextNewItem.text.toString()
            val updatedFullDescription = editTextFullDescription.text.toString()
            if (updatedText.isNotEmpty()) {
                updateItem(position, updatedLabel, updatedText, updatedFullDescription)
                dialog.dismiss()
            } else {
                editTextNewItem.error = "cannot be empty"
            }
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun addItem(label: String, item: String, fullDescription: String, id: String) {
        itemList.add(Task(label, item, fullDescription))
        itemIds.add(id)
        notifyItemInserted(itemList.size - 1)
    }

    private fun updateItem(position: Int, updatedLabel: String, updatedItem: String, updatedFullDescription: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val itemRef = db.collection("users").document(currentUser.uid).collection("tasks").document(itemIds[position])
            itemRef.update(mapOf(
                "label" to updatedLabel,
                "item" to updatedItem,
                "fullDescription" to updatedFullDescription
            ))
                .addOnSuccessListener {
                    itemList[position].label = updatedLabel
                    itemList[position].item = updatedItem
                    itemList[position].fullDescription = updatedFullDescription
                    notifyItemChanged(position)
                    Toast.makeText(context, "Item updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error updating item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteItem(position: Int) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val itemRef = db.collection("users").document(currentUser.uid).collection("tasks").document(itemIds[position])
            itemRef.delete()
                .addOnSuccessListener {
                    itemList.removeAt(position)
                    itemIds.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemList.size)
                    Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error deleting item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
