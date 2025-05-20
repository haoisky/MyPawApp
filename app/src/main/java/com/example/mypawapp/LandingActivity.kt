package com.example.mypawapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val bodyType = intent.getStringExtra("BODY_TYPE") ?: "Not selected"
        val weightGoal = intent.getStringExtra("WEIGHT_GOAL") ?: "Not selected"

        val bodyTypeTextView = findViewById<TextView>(R.id.bodyTypeDisplayText)
        val weightGoalTextView = findViewById<TextView>(R.id.weightGoalDisplayText)

        bodyTypeTextView.text = bodyType
        weightGoalTextView.text = weightGoal
    }
}
