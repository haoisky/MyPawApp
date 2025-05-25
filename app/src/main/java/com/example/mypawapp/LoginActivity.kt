package com.example.mypawapp // Siguraduhing tama ang package name mo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Siguraduhing may layout file ka na activity_login.xml sa res/layout
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        // Palitan ang mga R.id ng actual IDs mula sa activity_login.xml mo
        val etLoginEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etLoginPassword = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToSignUp = findViewById<TextView>(R.id.tvGoToSignUp) // ID ng TextView papuntang SignUp

        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString().trim()
            val password = etLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Sign-In
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.d("LoginActivity", "signInWithEmail:success")
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        goToMainPageActivity()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        tvGoToSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            // Hindi na kailangan ng finish() dito, para makabalik pa siya sa Login page
            // kung sakaling nagbago isip niya sa Sign Up page.
        }
    }

    private fun goToMainPageActivity() {
        val intent = Intent(this, MainPageActivity::class.java)
        startActivity(intent)
        finish() // Isara ang LoginActivity para hindi na makabalik dito via back button
        // pag nasa MainActivity na.
    }

    // Optional: Kung gusto mong i-handle ang back press para hindi bumalik sa SplashActivity
    // kung galing doon.
    // override fun onBackPressed() {
    //     super.onBackPressed()
    //     // Or if you want to exit the app from login screen on back press
    //     // finishAffinity()
    // }
}