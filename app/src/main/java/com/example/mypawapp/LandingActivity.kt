package com.example.mypawapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LandingActivity : AppCompatActivity() {

    private lateinit var recommendationDisplayText: TextView
    private lateinit var recommendationEditText: EditText
    private lateinit var customizeButton: Button
    private lateinit var feedButton: Button
    private lateinit var database: FirebaseDatabase

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        database = FirebaseDatabase.getInstance("https://mypawapp-549f0-default-rtdb.asia-southeast1.firebasedatabase.app/")


        val sharedPref = getSharedPreferences("PetPrefs", MODE_PRIVATE)
        val bodyType = sharedPref.getString("BODY_TYPE", "Not selected") ?: "Not selected"
        val weightGoal = sharedPref.getString("WEIGHT_GOAL", "Not selected") ?: "Not selected"

        val bodyTypeTextView = findViewById<TextView>(R.id.bodyTypeDisplayText)
        val weightGoalTextView = findViewById<TextView>(R.id.weightGoalDisplayText)
        recommendationDisplayText = findViewById(R.id.recommendationDisplayText)
        recommendationEditText = findViewById(R.id.recommendationEditText)
        customizeButton = findViewById(R.id.btnEnter)
        feedButton = findViewById(R.id.btnFeed)

        // Display stored selections
        bodyTypeTextView.text = bodyType
        weightGoalTextView.text = weightGoal

        val recommendationMap = mapOf(
            "Slim_Gain Weight" to "20g every 6 hours",
            "Slim_Maintain Weight" to "15g every 6 hours",
            "Slim_Lose Weight" to "12g every 8 hours",
            "Normal_Gain Weight" to "18g every 6 hours",
            "Normal_Maintain Weight" to "15g every 6 hours",
            "Normal_Lose Weight" to "12g every 8 hours",
            "Chubby_Gain Weight" to "Not Recommended",
            "Chubby_Maintain Weight" to "12g every 8 hours",
            "Chubby_Lose Weight" to "10g every 8 hours"
        )

        val key = "${bodyType}_$weightGoal"
        val defaultRecommendation = recommendationMap[key] ?: "No recommendation available"

        val savedCustomRecommendation = sharedPref.getString("CUSTOM_RECOMMENDATION", null)
        recommendationDisplayText.text = savedCustomRecommendation ?: defaultRecommendation

        customizeButton.setOnClickListener {
            if (isEditing) {
                val newText = recommendationEditText.text.toString()
                recommendationDisplayText.text = newText
                recommendationEditText.visibility = View.GONE
                recommendationDisplayText.visibility = View.VISIBLE
                customizeButton.text = "Customize"
                isEditing = false

                with(sharedPref.edit()) {
                    putString("CUSTOM_RECOMMENDATION", newText)
                    apply()
                }
            } else {
                recommendationEditText.setText(recommendationDisplayText.text)
                recommendationEditText.visibility = View.VISIBLE
                recommendationDisplayText.visibility = View.GONE
                customizeButton.text = "Save"
                isEditing = true
            }
        }

        // ✅ Feed button triggers Firebase only if user is logged in
        feedButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val feedRef = database.getReference("feed_signal")
                val data = mapOf(
                    "status" to "feed_now",
                    "timestamp" to System.currentTimeMillis()
                )

                feedRef.setValue(data).addOnSuccessListener {
                    Toast.makeText(this, "Feeding signal sent!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to send signal", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            }
        }


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
