package com.example.mypawapp // Siguraduhing tama ang package name mo

// Imports para sa Android UI, Firebase, at Date Formatting
import android.content.Intent
import android.os.Bundle
import android.util.Log // Para sa pag-debug kung may error
import android.view.Gravity // Para sa text alignment sa table
import android.view.View
import android.widget.Button // ****** BAGONG IMPORT ******
import android.widget.ImageButton // Para sa bottom navigation
import android.widget.TableLayout // Dito natin ilalagay ang data (nasa loob ng chartPlaceholder mo)
import android.widget.TableRow // Para sa bawat row sa table
import android.widget.TextView // Para sa text sa loob ng table cells
import android.widget.Toast // Para sa messages sa user
import androidx.appcompat.app.AlertDialog // ****** BAGONG IMPORT ******
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.* // Para sa Firebase Realtime Database
import java.text.SimpleDateFormat // Para i-format ang Date at Time
import java.util.Date // Para sa Date object
import java.util.Locale // Para sa Locale ng Date formatting

class StatsActivity : AppCompatActivity() {

    // Firebase variables
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var database: FirebaseDatabase

    private lateinit var feedLogsRef: DatabaseReference

    // UI elements
    private lateinit var tableLayoutLastFeed: TableLayout
    private lateinit var textViewNoSignalData: TextView
    private lateinit var buttonDeleteAll: Button // ****** BAGONG UI ELEMENT ******

    private var feedLogsValueListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats) // Siguraduhing may buttonDeleteAllLogs ID sa XML mo

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        database = FirebaseDatabase.getInstance("https://mypawapp-549f0-default-rtdb.asia-southeast1.firebasedatabase.app/")

        tableLayoutLastFeed = findViewById(R.id.tableLayoutFeedingLogs)
        textViewNoSignalData = findViewById(R.id.textViewNoLogsInsideBox)
        buttonDeleteAll = findViewById(R.id.buttonDeleteAllLogs) // ****** I-REFERENCE ANG BUTTON ******

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view stats.", Toast.LENGTH_LONG).show()
            tableLayoutLastFeed.visibility = View.GONE
            textViewNoSignalData.text = "Please log in to view feeding logs."
            textViewNoSignalData.visibility = View.VISIBLE
            buttonDeleteAll.visibility = View.GONE // ****** ITAGO KUNG HINDI LOGGED IN ******
            return
        } else {
            buttonDeleteAll.visibility = View.VISIBLE // ****** IPAKITA KUNG LOGGED IN ******
        }

        feedLogsRef = database.getReference("feed_logs")
        fetchFeedingLogs()

        // ****** LISTENER PARA SA DELETE ALL BUTTON ******
        buttonDeleteAll.setOnClickListener {
            showDeleteAllConfirmationDialog()
        }

        // Bottom nav buttons
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

    private fun fetchFeedingLogs() {
        tableLayoutLastFeed.visibility = View.VISIBLE
        textViewNoSignalData.visibility = View.GONE

        feedLogsValueListener?.let {
            feedLogsRef.removeEventListener(it)
            Log.d("StatsActivity", "Previous feedLogsValueListener removed.")
        }

        feedLogsValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("StatsActivity", "onDataChange triggered for feed_logs. Snapshot exists: ${snapshot.exists()}, Children count: ${snapshot.childrenCount}")
                clearTableLayout()
                addHeaderRowToTable()

                if (snapshot.exists() && snapshot.hasChildren()) {
                    val logs = mutableListOf<FeedingLog>()
                    for (logSnapshot in snapshot.children) {
                        val log = logSnapshot.getValue(FeedingLog::class.java)
                        // Isama lang sa listahan kung valid ang log at "feed_now" ang status
                        if (log != null && log.status == "feed_now" && log.timestamp != null) {
                            logs.add(log)
                        } else {
                            // Optional: Log kung bakit hindi isinama, pero baka "Done" ang status na hinahanap mo?
                            // Sa example mo sa taas, "Done" ang nasa comment pero "feed_now" ang nasa code.
                            // Siguraduhin kung ano talaga ang status na gusto mong i-display at i-delete.
                            // For now, susundin ko ang "feed_now" na nasa code mo.
                            Log.w("StatsActivity", "Skipping log entry (not 'feed_now' or no timestamp): $logSnapshot")
                        }
                    }

                    if (logs.isNotEmpty()) {
                        logs.sortByDescending { it.timestamp }
                        for (logEntry in logs) {
                            populateTableWithLogEntry(logEntry)
                        }
                        textViewNoSignalData.visibility = View.GONE
                        tableLayoutLastFeed.visibility = View.VISIBLE
                    } else {
                        // Walang "feed_now" logs na nahanap
                        showNoDataMessage("No feeding logs found.")
                    }
                } else {
                    // Kung ang "feed_logs" node ay wala o walang laman
                    showNoDataMessage("No feeding logs found in the database.")
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
        Log.d("StatsActivity", "Showing no data message: $message")
    }

    private fun populateTableWithLogEntry(logEntry: FeedingLog) {
        if (logEntry.timestamp == null) {
            Log.e("StatsActivity", "Timestamp is null in populateTableWithLogEntry. Log: $logEntry")
            return
        }
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

        val dateHeaderTextView = TextView(this).apply {
            text = "Date"
            setPadding(16, 16, 16, 16)
            gravity = Gravity.START
            // Pwede kang mag-apply ng text style dito kung gusto mo
            // setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val timeHeaderTextView = TextView(this).apply {
            text = "Time"
            setPadding(16, 16, 16, 16)
            gravity = Gravity.START
            // setTypeface(null, android.graphics.Typeface.BOLD)
        }

        headerRow.addView(dateHeaderTextView)
        headerRow.addView(timeHeaderTextView)

        // Idagdag ang header row sa index 0 para laging nasa taas
        if (tableLayoutLastFeed.childCount == 0) { // Idagdag lang kung wala pang laman (para maiwasan ang duplicate headers kung sakali)
            tableLayoutLastFeed.addView(headerRow, 0)
        } else {
            // Kung may laman na, at gusto mong palitan ang header (o siguraduhing nasa taas),
            // pwede mong i-clear muna at idagdag ulit, or i-check kung ang unang row ay header na.
            // Pero dahil tinatawag mo ang clearTableLayout() bago ito sa fetchFeedingLogs,
            // dapat okay na ang simpleng addView(headerRow, 0)
            tableLayoutLastFeed.addView(headerRow, 0)
        }
    }

    private fun clearTableLayout() {
        // Tatanggalin lahat ng views sa table, kasama ang header.
        // Ang header ay idadagdag ulit sa fetchFeedingLogs.
        val childCount = tableLayoutLastFeed.childCount
        if (childCount > 0) {
            tableLayoutLastFeed.removeAllViews()
        }
        Log.d("StatsActivity", "TableLayout cleared.")
    }

    // ****** FUNCTIONS PARA SA DELETE ALL ******
    private fun showDeleteAllConfirmationDialog() {
        // Simpleng check kung may laman ang table (bukod sa header)
        // Ang header ay nasa index 0, kaya kung > 1 ang childCount, may data rows.
        val hasLogsToDisplay = tableLayoutLastFeed.childCount > 1

        if (!hasLogsToDisplay) {
            Toast.makeText(this, "Log is empty. Nothing to delete.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete All Logs")
            .setMessage("Are you sure you want to delete ALL 'feed_now' logs? This action cannot be undone.")
            .setPositiveButton("Delete All") { dialog, which ->
                deleteAllFeedLogs()
            }
            .setNegativeButton("Cancel", null) // Walang gagawin pag kinansela
            .setIcon(android.R.drawable.ic_dialog_alert) // Optional: maglagay ng icon
            .show()
    }

    private fun deleteAllFeedLogs() {
        // Kukunin natin lahat ng logs na may status na "feed_now" at buburahin sila.
        // Mas mainam na i-query muna para sigurado.
        val queryToDelete = feedLogsRef.orderByChild("status").equalTo("feed_now")

        queryToDelete.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(this@StatsActivity, "No logs found to delete.", Toast.LENGTH_SHORT).show()
                    return
                }

                val updates = HashMap<String, Any?>()
                for (logSnapshot in dataSnapshot.children) {
                    logSnapshot.key?.let {
                        // Ang path ay dapat relative sa feedLogsRef
                        updates[it] = null // Setting to null deletes the node
                    }
                }

                if (updates.isEmpty()) {
                    Toast.makeText(this@StatsActivity, "No logs matched for deletion (keys were null).", Toast.LENGTH_SHORT).show()
                    return
                }

                feedLogsRef.updateChildren(updates)
                    .addOnSuccessListener {
                        Log.d("StatsActivity", "All 'feed_now' logs successfully deleted.")
                        Toast.makeText(this@StatsActivity, "All 'feed_now' logs deleted.", Toast.LENGTH_SHORT).show()
                        // Hindi na kailangang manu-manong i-clear ang table dito,
                        // dahil ang `feedLogsValueListener` mo ay dapat makadetect ng change
                        // at i-update ang UI (magiging empty ang table).
                    }
                    .addOnFailureListener { e ->
                        Log.e("StatsActivity", "Failed to delete 'feed_now' logs", e)
                        Toast.makeText(this@StatsActivity, "Failed to delete logs: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("StatsActivity", "Error querying logs for deletion: ${databaseError.message}")
                Toast.makeText(this@StatsActivity, "Error during deletion: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        feedLogsValueListener?.let {
            feedLogsRef.removeEventListener(it)
            Log.d("StatsActivity", "feedLogsValueListener removed in onDestroy.")
        }
    }
}