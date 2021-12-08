package ninja.bored.chiapublicaddressmonitor

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mikepenz.aboutlibraries.LibsBuilder
import ninja.bored.chiapublicaddressmonitor.helpers.NotificationHelper
import ninja.bored.chiapublicaddressmonitor.helpers.Slh

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.let { bottomNavigationListener ->
            bottomNavigationListener.selectedItemId = R.id.list
            bottomNavigationListener.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.license -> {
                        setCurrentFragment(LibsBuilder().supportFragment())
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
                onBackPressed()
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
