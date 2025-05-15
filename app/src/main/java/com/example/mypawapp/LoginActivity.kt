package com.example.mypawapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // When user taps Sign up text → pindot ito
        findViewById<TextView>(R.id.tvSignup).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        // When user taps Forgot Password → TODO: ilagay password reset
        findViewById<TextView>(R.id.tvForgot).setOnClickListener {
            // ipakita yung password reset flow mo
        }
        // When taps Log In button → TODO: validation
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val u = findViewById<EditText>(R.id.etUsername).text.toString()
            val p = findViewById<EditText>(R.id.etPassword).text.toString()
            // check dito kung tama, kung ok → next screen
        }
    }
}
