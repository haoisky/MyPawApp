package com.example.mypawapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        findViewById<TextView>(R.id.tvLoginLink).setOnClickListener {
            finish()  // balik sa LoginActivity
        }
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            // kunin lahat ng inputs, validate (password match), then save user data
        }
    }
}
