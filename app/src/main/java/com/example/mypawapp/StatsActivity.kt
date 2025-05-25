package com.example.mypawapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var feedLogsRef: DatabaseReference

    private lateinit var tableLayoutLastFeed: TableLayout
    private lateinit var textViewNoSignalData: TextView
    private lateinit var buttonDeleteAll: Button
    private lateinit var buttonLogout: Button

    private var feedLogsValueListener: ValueEventListener? = null

    private val CHANNEL_ID = "low_food_alert_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        createNotificationChannel()

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        database = FirebaseDatabase.getInstance("https://mypawapp-549f0-default-rtdb.asia-southeast1.firebasedatabase.app/")

        val foodLevelRef = database.getReference("foodLevel/current")
        val textViewFoodLevel = findViewById<TextView>(R.id.textViewFoodLevel)

        foodLevelRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val foodLevel = snapshot.getValue(String::class.java)
                if (foodLevel != null) {
                    textViewFoodLevel.text = "Food Level: $foodLevel"
                    if (foodLevel.lowercase() == "low") {
                        sendLowFoodNotification()
                    }
                } else {
                    textViewFoodLevel.text = "Food Level: Unknown"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StatsActivity", "Failed to read foodLevel: ${error.message}")
                textViewFoodLevel.text = "Error loading food level"
            }
        })

        tableLayoutLastFeed = findViewById(R.id.tableLayoutFeedingLogs)
        textViewNoSignalData = findViewById(R.id.textViewNoLogsInsideBox)
        buttonDeleteAll = findViewById(R.id.buttonDeleteAllLogs)
        buttonLogout = findViewById(R.id.btnLogout)  // <- NEW

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view stats.", Toast.LENGTH_LONG).show()
            tableLayoutLastFeed.visibility = View.GONE
            textViewNoSignalData.text = "Please log in to view feeding logs."
            textViewNoSignalData.visibility = View.VISIBLE
            buttonDeleteAll.visibility = View.GONE
            buttonLogout.visibility = View.GONE
            return
        } else {
            buttonDeleteAll.visibility = View.VISIBLE
            buttonLogout.visibility = View.VISIBLE
        }

        feedLogsRef = database.getReference("feed_logs")
        fetchFeedingLogs()

        buttonDeleteAll.setOnClickListener {
            showDeleteAllConfirmationDialog()
        }

        buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }


        findViewById<ImageButton>(R.id.navPaw).setOnClickListener {
            startActivity(Intent(this, PetProfilesActivity::class.java))
        }
        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, LandingActivity::class.java))
        }
        findViewById<ImageButton>(R.id.navStats).setOnClickListener {
            Toast.makeText(this, "You're already on Stats!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            Firebase.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Low Food Alert"
            val descriptionText = "Notifies you when food level is low"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendLowFoodNotification() {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Low Food Level Alert")
            .setContentText("Your pet's food level is low. Please refill soon.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(1, builder.build())
        }
    }

    private fun fetchFeedingLogs() {
        tableLayoutLastFeed.visibility = View.VISIBLE
        textViewNoSignalData.visibility = View.GONE

        feedLogsValueListener?.let {
            feedLogsRef.removeEventListener(it)
        }

        feedLogsValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                clearTableLayout()
                addHeaderRowToTable()

                val logs = mutableListOf<FeedingLog>()
                for (logSnapshot in snapshot.children) {
                    val log = logSnapshot.getValue(FeedingLog::class.java)
                    if (log != null && log.status == "feed_now" && log.timestamp != null) {
                        logs.add(log)
                    }
                }

                if (logs.isNotEmpty()) {
                    logs.sortByDescending { it.timestamp }
                    logs.forEach { populateTableWithLogEntry(it) }
                    textViewNoSignalData.visibility = View.GONE
                    tableLayoutLastFeed.visibility = View.VISIBLE
                } else {
                    showNoDataMessage("No feeding logs found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StatsActivity", "Failed to fetch feeding logs.", error.toException())
                Toast.makeText(this@StatsActivity, "Failed to load logs: ${error.message}", Toast.LENGTH_SHORT).show()
                showNoDataMessage("Error loading feeding logs: ${error.message}")
                clearTableLayout()
            }
        }
        feedLogsRef.addValueEventListener(feedLogsValueListener!!)
    }

    private fun showNoDataMessage(message: String) {
        textViewNoSignalData.text = message
        textViewNoSignalData.visibility = View.VISIBLE
        tableLayoutLastFeed.visibility = View.GONE
    }

    private fun populateTableWithLogEntry(logEntry: FeedingLog) {
        if (logEntry.timestamp == null) return
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val tableRow = TableRow(this)

        val dateTextView = TextView(this).apply {
            text = dateFormat.format(Date(logEntry.timestamp))
            setPadding(16, 16, 16, 16)
            gravity = Gravity.START
        }

        val timeTextView = TextView(this).apply {
            text = timeFormat.format(Date(logEntry.timestamp))
            setPadding(16, 16, 16, 16)
            gravity = Gravity.START
        }

        tableRow.addView(dateTextView)
        tableRow.addView(timeTextView)
        tableLayoutLastFeed.addView(tableRow)
    }

    private fun addHeaderRowToTable() {
        val headerRow = TableRow(this)
        val dateHeader = TextView(this).apply {
            text = "Date"
            setPadding(16, 16, 16, 16)
            gravity = Gravity.START
        }
        val timeHeader = TextView(this).apply {
            text = "Time"
            setPadding(16, 16, 16, 16)
            gravity = Gravity.START
        }
        headerRow.addView(dateHeader)
        headerRow.addView(timeHeader)
        if (tableLayoutLastFeed.childCount == 0) {
            tableLayoutLastFeed.addView(headerRow, 0)
        } else {
            tableLayoutLastFeed.addView(headerRow, 0)
        }
    }

    private fun clearTableLayout() {
        if (tableLayoutLastFeed.childCount > 0) {
            tableLayoutLastFeed.removeAllViews()
        }
    }

    private fun showDeleteAllConfirmationDialog() {
        val hasLogsToDisplay = tableLayoutLastFeed.childCount > 1

        if (!hasLogsToDisplay) {
            Toast.makeText(this, "Log is empty. Nothing to delete.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete All Logs")
            .setMessage("Are you sure you want to delete ALL 'feed_now' logs? This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ -> deleteAllFeedLogs() }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteAllFeedLogs() {
        val queryToDelete = feedLogsRef.orderByChild("status").equalTo("feed_now")

        queryToDelete.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(this@StatsActivity, "No logs found to delete.", Toast.LENGTH_SHORT).show()
                    return
                }

                val updates = HashMap<String, Any?>()
                for (logSnapshot in dataSnapshot.children) {
                    logSnapshot.key?.let { updates[it] = null }
                }

                if (updates.isEmpty()) {
                    Toast.makeText(this@StatsActivity, "No logs matched for deletion.", Toast.LENGTH_SHORT).show()
                    return
                }

                feedLogsRef.updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this@StatsActivity, "All 'feed_now' logs deleted.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@StatsActivity, "Failed to delete logs: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@StatsActivity, "Error during deletion: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        feedLogsValueListener?.let {
            feedLogsRef.removeEventListener(it)
        }
    }
}
