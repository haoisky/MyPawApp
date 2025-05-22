package com.example.mypawapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // Read from SharedPreferences
        val sharedPref = getSharedPreferences("PetPrefs", MODE_PRIVATE)
        val bodyType = sharedPref.getString("BODY_TYPE", "Not selected") ?: "Not selected"
        val weightGoal = sharedPref.getString("WEIGHT_GOAL", "Not selected") ?: "Not selected"

        val bodyTypeTextView = findViewById<TextView>(R.id.bodyTypeDisplayText)
        val weightGoalTextView = findViewById<TextView>(R.id.weightGoalDisplayText)
        val recommendationTextView = findViewById<TextView>(R.id.recommendationDisplayText)

        // Display stored selections
        bodyTypeTextView.text = bodyType
        weightGoalTextView.text = weightGoal

        // Create a map for recommendations
        val recommendationMap = mapOf(
            "Slim_Gain Weight" to "20g every 6 hours",
            "Slim_Maintain Weight" to "15g every 6 hours",
            "Slim_Lose Weight" to "12g every 8 hours",
            "Normal_Gain Weight" to "18g every 6 hours",
            "Normal_Maintain Weight" to "15g every 6 hours",
            "Normal_Lose Weight" to "12g every 6 hours",
            "Chubby_Gain Weight" to "Not Recommended",
            "Chubby_Maintain Weight" to "12g every 8 hours",
            "Chubby_Lose Weight" to "10g every 8 hours"
        )

        // Construct key and get recommendation
        val key = "${bodyType}_$weightGoal"
        val recommendation = recommendationMap[key] ?: "No recommendation available"

        recommendationTextView.text = recommendation

        // Bottom nav buttons
        findViewById<ImageButton>(R.id.navPaw).setOnClickListener {
            val intent = Intent(this, PetProfilesActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            Toast.makeText(this, "You're already on Home!", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.navStats).setOnClickListener {
            Toast.makeText(this, "Opening Stats...", Toast.LENGTH_SHORT).show()
            // TODO: Start stats activity
        }
    }
}
