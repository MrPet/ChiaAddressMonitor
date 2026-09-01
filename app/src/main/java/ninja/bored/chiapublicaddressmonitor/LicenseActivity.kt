package ninja.bored.chiapublicaddressmonitor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.util.withContext

class LicenseActivity : ComponentActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LicenseActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val libs = remember(context) {
                Libs.Builder().withContext(context).build()
            }
            LibrariesContainer(
                libraries = libs,
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
            )
        }
    }
}
