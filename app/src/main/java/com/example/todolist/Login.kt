package com.example.todolist

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)


        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            val intent = Intent(this, todoPage::class.java)
            startActivity(intent)
            finish()
        }

        val loginButton = binding.Loginbutton
        loginButton.setOnClickListener {
            val username = binding.usernameLogin.text.toString().trim()
            val password = binding.passwordLogin.text.toString().trim()
            if (username.isEmpty()) {
                binding.usernameLogin.error = "Email is required"
            } else if (password.isEmpty()) {
                binding.passwordLogin.error = "Password is required"
            } else {
                auth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean("isLoggedIn", true)
                                editor.putString("username", username)
                                editor.apply()

                                val intent = Intent(this, todoPage::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    fun signup(view: View) {
        binding.usernameLogin.text.clear()
        binding.passwordLogin.text.clear()
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
    }
}
