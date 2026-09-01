package ninja.bored.chiapublicaddressmonitor

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import ninja.bored.chiapublicaddressmonitor.helpers.NotificationHelper
import ninja.bored.chiapublicaddressmonitor.helpers.Slh

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinator)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.toolbar)?.setPadding(0, systemBars.top, 0, 0)
            findViewById<View>(R.id.bottomNavigationView)?.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.let { bottomNavigationListener ->
            bottomNavigationListener.selectedItemId = R.id.list
            bottomNavigationListener.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.license -> {
                        setCurrentFragment(LicenseFragment())
                    }
                    R.id.list -> setCurrentFragment(AddressListFragment())
                    R.id.forks -> setCurrentFragment(ForkFragment())
                }
                return@setOnItemSelectedListener true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.createNotificationChannels(this)
        Slh.setupWidgetUpdateWorker(this)
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, fragment)
            commit()
        }
}
