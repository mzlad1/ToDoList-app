package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.signupbutton.setOnClickListener {
            val username = binding.username.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()
            val name = binding.name.text.toString().trim()
            val phone = binding.phone.text.toString().trim()
            val address = binding.address.text.toString().trim()
            val termsSwitch = binding.termsSwitch.isChecked

            if (username.isEmpty()) {
                binding.username.error = "Username is required"
            } else if (email.isEmpty()) {
                binding.email.error = "Email is required"
            } else if (password.isEmpty()) {
                binding.password.error = "Password is required"
            } else if (confirmPassword.isEmpty()) {
                binding.confirmPassword.error = "Please confirm your password"
            } else if (password.length < 6 || !password.contains(Regex(".*[0-9].*")) || !password.contains(Regex(".*[!@#\$%^&*].*"))) {
                binding.password.error = "Password must be at least 6 characters long and contain at least one number and one special character"
            } else if (name.isEmpty()) {
                binding.name.error = "Name is required"
            } else if (phone.isEmpty()) {
                binding.phone.error = "Phone number is required"
            } else if (address.isEmpty()) {
                binding.address.error = "Address is required"
            } else if (password != confirmPassword) {
                binding.password.error = "Passwords do not match"
                binding.confirmPassword.error = "Passwords do not match"
            } else if (!termsSwitch) {
                binding.termsSwitch.error = "Please accept the terms and conditions"
            } else {
                db.collection("users").whereEqualTo("username", username).get()
                    .addOnSuccessListener { usernameDocuments ->
                        if (!usernameDocuments.isEmpty) {
                            binding.username.error = "Username already exists"
                            return@addOnSuccessListener
                        }
                        db.collection("users").whereEqualTo("email", email).get()
                            .addOnSuccessListener { emailDocuments ->
                                if (!emailDocuments.isEmpty) {
                                    binding.email.error = "Email already exists"
                                    return@addOnSuccessListener
                                }
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val currentUser = auth.currentUser
                                            if (currentUser != null) {
                                                val user = hashMapOf(
                                                    "username" to username,
                                                    "email" to email,
                                                    "name" to name,
                                                    "phone" to phone,
                                                    "address" to address
                                                )

                                                db.collection("users").document(currentUser.uid).set(user)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                                        binding.username.text.clear()
                                                        binding.email.text.clear()
                                                        binding.password.text.clear()
                                                        binding.confirmPassword.text.clear()
                                                        binding.name.text.clear()
                                                        binding.phone.text.clear()
                                                        binding.address.text.clear()
                                                        startActivity(Intent(this, Login::class.java))
                                                        finish()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(this, "Error adding user: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        } else {
                                            Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error checking email: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error checking username: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
