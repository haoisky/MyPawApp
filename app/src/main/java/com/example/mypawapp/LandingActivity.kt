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

    private lateinit var currentFeedSignalRef: DatabaseReference // Para sa "signal"
    private lateinit var feedLogsRef: DatabaseReference          // Para sa history

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database Reference
        // Get a reference to the root of your database
        databaseRootRef = FirebaseDatabase.getInstance("https://mypawapp-549f0-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        currentFeedSignalRef = databaseRootRef.child("feed_signal")
        feedLogsRef = databaseRootRef.child("feed_logs") // Ito dapat ay consistent sa ginagamit ng StatsActivity

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
            val user = auth.currentUser
            if (user != null) {
                // Step 1.3: Baguhin ang target node
                val feedLogsRef = databaseRootRef.child("feed_logs") // Dati ay "feed_signal"
                val currentTimestamp = System.currentTimeMillis()

                // --- START: BAGONG LOGIC PARA SA PAGSULAT SA DALAWANG NODE ---

                // 1. Mag-set ng "signal" sa current_feed_signal node
                val signalData = mapOf(
                    "status" to "feed_now", // O "Feeding...", "Signal Active"
                    "timestamp" to currentTimestamp

                )
                currentFeedSignalRef.setValue(signalData)
                    .addOnSuccessListener {
                        Log.d("LandingActivity", "Feed signal successfully sent to ${currentFeedSignalRef.key}.")
                        Toast.makeText(this, "Feed signal sent!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("LandingActivity", "Failed to send feed signal to ${currentFeedSignalRef.key}.", e)
                        // Optional: Magpakita ng error message sa user
                    }

                // 2. Mag-log ng "Done" entry sa feed_logs (tulad ng ginagawa mo na)
                //    I-assume natin na ang pagpindot ng button ay successful feeding event
                //    para sa purpose ng pag-log sa history.
                val logStatus = "feed_now" // Ito 'yung status na hinahanap ng StatsActivity
                // Siguraduhing may FeedingLog data class ka
                // Kung wala pa, pwede mong gamitin ang mapOf tulad ng dati:
                //val feedDataForLog = mapOf(
                //     "status" to logStatus,
                //    "timestamp" to currentTimestamp
                //)
                // Pero mas maganda kung may data class:
                val feedDataForLog = FeedingLog(logStatus, currentTimestamp)


                feedLogsRef.push().setValue(feedDataForLog) // Gamitin ang feedLogsRef
                    .addOnSuccessListener {
                        Log.d("LandingActivity", "Feeding log successfully saved to ${feedLogsRef.key} at $currentTimestamp")
                        // Hindi na kailangan ng Toast dito kung may Toast na sa signal, para hindi redundant
                    }
                    .addOnFailureListener { e ->
                        Log.e("LandingActivity", "Error saving feeding log to ${feedLogsRef.key}", e)
                        Toast.makeText(this, "Failed to save log: ${e.message}", Toast.LENGTH_LONG).show()
                    }

                // --- END: BAGONG LOGIC PARA SA PAGSULAT SA DALAWANG NODE ---
            } else {
                Toast.makeText(this, "Please log in first to save feed log.", Toast.LENGTH_SHORT).show()
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