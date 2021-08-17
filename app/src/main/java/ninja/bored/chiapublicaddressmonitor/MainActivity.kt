package ninja.bored.chiapublicaddressmonitor

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.aboutlibraries.LibsBuilder
import ninja.bored.chiapublicaddressmonitor.helpers.NotificationHelper
import ninja.bored.chiapublicaddressmonitor.helpers.Slh

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.createNotificationChannels(this)
        Slh.setupWidgetUpdateWorker(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.default_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.license -> {
                LibsBuilder().start(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
