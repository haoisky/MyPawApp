package com.example.mypawapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    // Optional: Kung gagamit ka ng Firestore para sa additional user info
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Siguraduhing may layout file ka na activity_sign_up.xml sa res/layout
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth
        // Optional: db = FirebaseFirestore.getInstance()

        // Palitan ang mga R.id ng actual IDs mula sa activity_signup.xml mo
        val etSignUpEmail = findViewById<EditText>(R.id.etSignUpEmail)
        val etSignUpPassword = findViewById<EditText>(R.id.etSignUpPassword)
        // Optional: Kung may field ka para sa name or other info
        val etName = findViewById<EditText>(R.id.etSignUpName)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val email = etSignUpEmail.text.toString().trim()
            val password = etSignUpPassword.text.toString().trim()
            val name = etName.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase sign-up
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("SignUpActivity", "createUserWithEmail:success")
                        val firebaseUser = auth.currentUser
                        Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()

                        // --- OPTIONAL: Save additional user info to Firestore ---
                        // if (firebaseUser != null && name.isNotEmpty()) {
                        //    saveAdditionalUserInfo(firebaseUser.uid, name, email)
                        //}
                        // --- END OPTIONAL ---

                        auth.signOut()

                        // Proceed to LoginActivity
                        val intent = Intent(this, LoginActivity::class.java)
                        // Optional: Clear task and start new task for LoginActivity
                        // para hindi makabalik sa SignUpActivity via back button.
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish() // Isara ang SignUpActivity
                    } else {
                        Log.w("SignUpActivity", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // --- OPTIONAL: Function to save additional user info to Firestore ---
    private fun saveAdditionalUserInfo(userId: String, name: String, email: String) {
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "createdAt" to System.currentTimeMillis() // Example: timestamp
        )

        db.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Log.d("SignUpActivity", "User profile created for $userId")
            }
            .addOnFailureListener { e ->
                Log.w("SignUpActivity", "Error creating user profile", e)
            }
    }
}