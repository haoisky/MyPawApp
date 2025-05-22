package com.example.mypawapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PetProfilesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_profiles) // Make sure this layout exists

        // Bottom nav buttons (inside onCreate)
        findViewById<ImageButton>(R.id.navPaw).setOnClickListener {
            Toast.makeText(this, "You're already on Pets profiles!", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.navStats).setOnClickListener {
            Toast.makeText(this, "Opening Stats...", Toast.LENGTH_SHORT).show()
            // TODO: Start stats activity
        }
    }

}
