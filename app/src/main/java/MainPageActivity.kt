package com.example.mypawapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
//import com.example.mypawapp.com.example.mypawapp.LandingActivity
import android.content.Intent


class MainPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectedBodyTypeText = findViewById<TextView>(R.id.selectedBodyTypeText)
        val bodyTypeFrameLayout = findViewById<FrameLayout>(R.id.bodyTypeFrameLayout) // Give it an ID first

        bodyTypeFrameLayout.setOnClickListener {
            val popupMenu = PopupMenu(this, bodyTypeFrameLayout)
            popupMenu.menu.add("Slim")
            popupMenu.menu.add("Normal")
            popupMenu.menu.add("Chubby")

            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                selectedBodyTypeText.text = item.title
                true
            }

            popupMenu.show()
        }

        val selectedWeightGoalText = findViewById<TextView>(R.id.selectedweightGoalText)
        val weightGoalFrameLayout = findViewById<FrameLayout>(R.id.weightGoalFrameLayout)

        // Show dropdown when paw icon in weight goal is clicked
        weightGoalFrameLayout.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.menu.add("Lose Weight")
            popupMenu.menu.add("Maintain Weight")
            popupMenu.menu.add("Gain Weight")

            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                selectedWeightGoalText.text = item.title
                true
            }

            popupMenu.show()
        }


        findViewById<Button>(R.id.btnEnter).setOnClickListener {
            val intent = Intent(this, LandingActivity::class.java)

            val selectedBodyType = findViewById<TextView>(R.id.selectedBodyTypeText).text.toString()
            val selectedWeightGoal = findViewById<TextView>(R.id.selectedweightGoalText).text.toString()

            intent.putExtra("BODY_TYPE", selectedBodyType)
            intent.putExtra("WEIGHT_GOAL", selectedWeightGoal)

            startActivity(intent)
        }



        // Bottom nav buttons
        findViewById<ImageButton>(R.id.navPaw).setOnClickListener {
            Toast.makeText(this, "Paw clicked!", Toast.LENGTH_SHORT).show()
            // TODO: Open dropdown or another screen
        }

        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            Toast.makeText(this, "You're already on Home!", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.navStats).setOnClickListener {
            Toast.makeText(this, "Opening Stats...", Toast.LENGTH_SHORT).show()
            // TODO: open stats screen
        }
    }
}
