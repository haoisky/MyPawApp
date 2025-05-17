package com.example.mypawapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton

class MainPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example: kapag pinindot ang Pet Body Type
        findViewById<Button>(R.id.btnBodyType).setOnClickListener {
            // TODO: open BodyTypeActivity or popup
        }
        findViewById<Button>(R.id.btnWeightGoal).setOnClickListener {
            // TODO: open WeightGoalActivity or popup
        }
        findViewById<Button>(R.id.btnEnter).setOnClickListener {
            // TODO: punta sa next na screen
        }

        // Bottom nav
        findViewById<ImageButton>(R.id.navPaw).setOnClickListener { /* TODO */ }
        findViewById<ImageButton>(R.id.navHome).setOnClickListener { /* we're here */ }
        findViewById<ImageButton>(R.id.navStats).setOnClickListener { /* TODO */ }
    }
}
