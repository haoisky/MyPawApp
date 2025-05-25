package com.example.mypawapp

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log for debugging
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference // Import DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LandingActivity : AppCompatActivity() {

    private lateinit var recommendationDisplayText: TextView
    private lateinit var recommendationEditText: EditText
    private lateinit var customizeButton: Button
    private lateinit var feedButton: Button
    // Change FirebaseDatabase to DatabaseReference for more specific use
    private lateinit var databaseRootRef: DatabaseReference // Reference to the root
    private lateinit var auth: FirebaseAuth // Declare FirebaseAuth

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database Reference
        // Get a reference to the root of your database
        databaseRootRef = FirebaseDatabase.getInstance("https://mypawapp-549f0-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

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
            "Slim_Gain Weight" to "20g",
            "Slim_Maintain Weight" to "15g",
            "Slim_Lose Weight" to "12g",
            "Normal_Gain Weight" to "18g",
            "Normal_Maintain Weight" to "15g",
            "Normal_Lose Weight" to "12g",
            "Chubby_Gain Weight" to "Not Recommended",
            "Chubby_Maintain Weight" to "12g",
            "Chubby_Lose Weight" to "10g"
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

        feedButton.setOnClickListener {
            val user = auth.currentUser // Use the initialized auth instance
            if (user != null) {
                // Get a reference to the "feed_signal" node under the root
                val feedRef = databaseRootRef.child("feed_signal")
                val currentTimestamp = System.currentTimeMillis()

                val data = mapOf(
                    "status" to "feed_now",
                    "timestamp" to currentTimestamp
                )
                // Pwede ring gamitin ang FeedingLog data class:
                // val feedLog = FeedingLog("Done", currentTimestamp)
                // feedRef.setValue(feedLog).addOnSuccessListener { ... }

                feedRef.setValue(data).addOnSuccessListener {
                    Toast.makeText(this, "Feeding signal sent!", Toast.LENGTH_SHORT).show()
                    Log.d("LandingActivity", "Feed signal (Done) successfully written to Firebase at $currentTimestamp")
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send signal: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("LandingActivity", "Error writing feed signal to Firebase", e)
                }
            } else {
                Toast.makeText(this, "Please log in first to send feed signal.", Toast.LENGTH_SHORT).show()
                // Opsyonal: Redirect to LoginActivity
                // startActivity(Intent(this, LoginActivity::class.java))
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
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }
    }
}