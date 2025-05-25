package com.example.mypawapp // Siguraduhing tama ang package name mo

// Imports para sa Android UI, Firebase, at Date Formatting
import android.content.Intent
import android.os.Bundle
import android.util.Log // Para sa pag-debug kung may error
import android.view.Gravity // Para sa text alignment sa table
import android.view.View
import android.widget.ImageButton // Para sa bottom navigation
import android.widget.TableLayout // Dito natin ilalagay ang data (nasa loob ng chartPlaceholder mo)
import android.widget.TableRow // Para sa bawat row sa table
import android.widget.TextView // Para sa text sa loob ng table cells
import android.widget.Toast // Para sa messages sa user
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.* // Para sa Firebase Realtime Database
import java.text.SimpleDateFormat // Para i-format ang Date at Time
import java.util.Date // Para sa Date object
import java.util.Locale // Para sa Locale ng Date formatting

class StatsActivity : AppCompatActivity() {

    // Firebase variables
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null // Para malaman kung may naka-login
    private lateinit var database: FirebaseDatabase

    // Reference sa "feed_signal" node sa Firebase
    private lateinit var feedSignalRef: DatabaseReference

    // UI elements para sa pag-display ng data
    private lateinit var tableLayoutLastFeed: TableLayout // Ito yung TableLayout mo (dati mong tinawag na chartPlaceholder or nasa loob nito)
    private lateinit var textViewNoSignalData: TextView   // Para sa message kung walang data o hindi "Done"

    // Firebase listener para makinig sa changes sa "feed_signal"
    private var feedSignalListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats) // Siguraduhing tama ang layout file name mo

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        database =
            FirebaseDatabase.getInstance("https://mypawapp-549f0-default-rtdb.asia-southeast1.firebasedatabase.app/") // Siguraduhing tama ang URL mo

        // Get references to UI elements
        // PALITAN ANG MGA IDs KUNG IBA ANG GINAMIT MO SA activity_stats.xml
        tableLayoutLastFeed = findViewById(R.id.tableLayoutFeedingLogs) // ID ng TableLayout mo
        textViewNoSignalData =
            findViewById(R.id.textViewNoLogsInsideBox) // ID ng TextView para sa messages

        // Check kung may naka-login na user
        if (currentUser == null) {
            // Kung walang user, ipakita ang message at huwag nang ituloy ang pag-fetch
            Toast.makeText(this, "Please log in to view stats.", Toast.LENGTH_LONG).show()
            tableLayoutLastFeed.visibility = View.GONE // Itago ang table
            textViewNoSignalData.text = "Please log in to view the last feed time."
            textViewNoSignalData.visibility = View.VISIBLE // Ipakita ang message
            // disableBottomNavIfNoUser() // Kung meron kang function para i-disable ang nav, tawagin dito
            return // Itigil na ang onCreate execution dito
        }

        // Setup ang reference sa "feed_signal" node sa Firebase.
        // PALITAN ANG "feed_signal" KUNG IBA ANG PANGALAN NG NODE MO.
        feedSignalRef = database.getReference("feed_signal")

        // Simulan ang pakikinig sa changes sa feed_signal node
        fetchLastFeedSignal()

            // Bottom nav buttons
            findViewById<ImageButton>(R.id.navPaw).setOnClickListener {
                val intent = Intent(this, PetProfilesActivity::class.java)
                startActivity(intent)
            }

            findViewById<ImageButton>(R.id.navHome).setOnClickListener {
                val intent = Intent(this, LandingActivity::class.java)
                startActivity(intent)
            }

            findViewById<ImageButton>(R.id.navStats).setOnClickListener {
                Toast.makeText(this, "You're already on Stats!", Toast.LENGTH_SHORT).show()
            }

    }

    private fun fetchLastFeedSignal() {
        // Ipakita muna ang table structure (header) at itago ang "no data" message
        // para hindi mag-flash ang "no data" message habang naglo-load.
        tableLayoutLastFeed.visibility = View.VISIBLE
        textViewNoSignalData.visibility = View.GONE

        // Gumawa ng ValueEventListener
        feedSignalListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                clearTableLayout() // Linisin muna ang table bago maglagay ng bago (o walang laman)

                if (snapshot.exists()) {
                    // I-convert ang data mula sa Firebase papunta sa FeedingLog object
                    val lastSignal = snapshot.getValue(FeedingLog::class.java)

                    if (lastSignal != null && lastSignal.status == "Done" && lastSignal.timestamp != null) {
                        // Kung ang status ay "Done" at may timestamp, ipakita ito sa table
                        populateTableWithLastFeed(lastSignal)
                        textViewNoSignalData.visibility = View.GONE // Itago ang "no data" message
                        tableLayoutLastFeed.visibility =
                            View.VISIBLE // Siguraduhing visible ang table
                    } else {
                        // Kung hindi "Done" o walang timestamp, o iba pang issue
                        var message = "No recent 'Done' feeding signal found." // Default message
                        if (lastSignal != null) {
                            if (lastSignal.status != "Done") {
                                message =
                                    "Last signal status: ${lastSignal.status}. Waiting for 'Done'."
                            } else if (lastSignal.timestamp == null) {
                                message = "Signal is 'Done' but no timestamp found."
                            }
                        } else {
                            // Nangyayari ito kung ang structure ng data sa "feed_signal" ay hindi tugma sa FeedingLog data class
                            message = "Feed signal data is not in the expected format."
                            Log.w(
                                "StatsActivity",
                                "feed_signal data could not be parsed into FeedingLog object. Snapshot: ${snapshot.value}"
                            )
                        }
                        textViewNoSignalData.text = message
                        textViewNoSignalData.visibility = View.VISIBLE // Ipakita ang message
                        tableLayoutLastFeed.visibility = View.GONE    // Itago ang table
                    }
                } else {
                    // Kung ang "feed_signal" node ay wala (hindi pa nagagawa o na-delete)
                    Log.d("StatsActivity", "feed_signal node does not exist.")
                    textViewNoSignalData.text = "Feed signal data not found in the database."
                    textViewNoSignalData.visibility = View.VISIBLE
                    tableLayoutLastFeed.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Kung nagkaroon ng error sa pag-access sa Firebase (e.g., network issue, permission denied)
                Log.e("StatsActivity", "Failed to fetch feed signal.", error.toException())
                Toast.makeText(
                    this@StatsActivity,
                    "Failed to load signal: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                textViewNoSignalData.text = "Error loading feed signal: ${error.message}"
                textViewNoSignalData.visibility = View.VISIBLE
                tableLayoutLastFeed.visibility = View.GONE
                clearTableLayout() // Linisin pa rin ang table
            }
        }

        // Idagdag ang listener sa feedSignalRef para makinig sa changes
        // Ang addValueEventListener ay patuloy na makikinig sa changes hangga't hindi tinatanggal ang listener
        feedSignalRef.addValueEventListener(feedSignalListener!!)
    }
    private fun populateTableWithLastFeed(signal: FeedingLog) {
        // Siguraduhing may laman ang timestamp bago gamitin
        if (signal.timestamp == null) {
            Log.e("StatsActivity", "Timestamp is null in populateTableWithLastFeed. Signal: $signal")
            textViewNoSignalData.text = "Error: Timestamp missing for 'Done' signal."
            textViewNoSignalData.visibility = View.VISIBLE
            tableLayoutLastFeed.visibility = View.GONE
            return
        }

        // Formatters para sa Date at Time
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) // e.g., "May 23, 2024"
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())    // e.g., "03:30 PM"

        // Gumawa ng bagong TableRow
        val tableRow = TableRow(this)
        val layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        tableRow.layoutParams = layoutParams

        // Gumawa ng TextView para sa Date
        val dateTextView = TextView(this).apply {
            text = dateFormat.format(Date(signal.timestamp)) // I-format ang timestamp
            setPadding(16, 16, 16, 16) // Maglagay ng padding (adjust as needed)
            gravity = Gravity.START // Text alignment
            // Para mag-share ng space ang columns (optional, depende sa design mo)
            // layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Gumawa ng TextView para sa Time
        val timeTextView = TextView(this).apply {
            text = timeFormat.format(Date(signal.timestamp)) // I-format ang timestamp
            setPadding(16, 16, 16, 16) // Maglagay ng padding
            gravity = Gravity.START // Text alignment
            // layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Idagdag ang mga TextViews sa TableRow
        tableRow.addView(dateTextView)
        tableRow.addView(timeTextView)

        // Idagdag ang TableRow sa TableLayout
        // Dahil isang entry lang (Goal A), pwede nating i-add lang ito.
        // Kung may header row ka na sa XML, baka gusto mong i-add ito sa specific index (e.g., tableLayoutLastFeed.addView(tableRow, 1))
        tableLayoutLastFeed.addView(tableRow)
    }

    private fun clearTableLayout() {
        // Alisin lahat ng views sa table maliban kung may static header row ka
        // Kung may header row ka sa XML (index 0), simulan ang pag-alis sa index 1.
        val childCount = tableLayoutLastFeed.childCount
        if (childCount > 0) { // Kung may header ka sa XML, dapat childCount > 1
            // Alisin lahat ng rows na dinamikong idinagdag.
            // Kung ang header mo ay nasa XML, at gusto mong iwanan,
            // ang loop ay dapat: for (i in childCount - 1 downTo 1)
            // Sa ngayon, aalisin natin lahat.
            tableLayoutLastFeed.removeAllViews()
        }

        // Kung gusto mong maglagay ng header row programmatically tuwing lilinisin:
        // addHeaderRowToTable() // Gagawin mo pa itong function kung kailangan
    }
    override fun onDestroy() {
        super.onDestroy()
        // Mahalaga: Alisin ang listener para maiwasan ang memory leaks
        // kapag ang activity ay nawasak na.
        if (feedSignalListener != null) {
            feedSignalRef.removeEventListener(feedSignalListener!!)
        }
    }
}