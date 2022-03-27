package ninja.bored.chiapublicaddressmonitor

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.service.voice.VoiceInteractionService
import androidx.slice.SliceManager


class ChiaWidgetApplication : Application() {
    companion object {
        const val SLICE_AUTHORITY = "ninja.bored.chiapublicaddressmonitor"
    }

    override fun onCreate() {
        super.onCreate()
        grantSlicePermissions()
    }

    private fun grantSlicePermissions() {
        val context: Context = applicationContext
        val sliceProviderUri: Uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(SLICE_AUTHORITY)
            .build()

        val assistantPackage = getAssistantPackage(context) ?: return
        SliceManager.getInstance(context)
            .grantSlicePermission(assistantPackage, sliceProviderUri)
    }

    private fun getAssistantPackage(context: Context): String? {
        val packageManager: PackageManager = context.getPackageManager()
        val resolveInfoList = packageManager.queryIntentServices(
            Intent(VoiceInteractionService.SERVICE_INTERFACE), 0
        )
        return if (resolveInfoList.isEmpty()) {
            null
        } else resolveInfoList[0].serviceInfo.packageName
    }
}
