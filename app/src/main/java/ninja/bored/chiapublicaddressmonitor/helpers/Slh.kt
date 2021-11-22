package ninja.bored.chiapublicaddressmonitor.helpers

import android.content.Context
import android.content.Intent
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Build
import android.provider.Settings
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Locale
import java.util.concurrent.TimeUnit
import ninja.bored.chiapublicaddressmonitor.WidgetUpdaterWork
import ninja.bored.chiapublicaddressmonitor.helpers.Constants.CHIA_ADDRESS_LENGTH
import ninja.bored.chiapublicaddressmonitor.helpers.Constants.CHIA_ADDRESS_PREFIX

object Slh {
    // private const val TAG = "Slh"

    /**
     * validate chia address
     */
    fun isChiaAddressValid(chiaAddress: String?): Boolean {
        // should check real specs
        val checkRegex =
            Regex("^$CHIA_ADDRESS_PREFIX[\\d\\w]{${CHIA_ADDRESS_LENGTH - CHIA_ADDRESS_PREFIX.length}}$")
        if (checkRegex.matches(chiaAddress.toString())) {
            return true
        }
        return false
    }

    fun formatChiaDecimal(chiaAmount: Double, precision: String?): String? {
        val decimalFormat: DecimalFormat =
            when {
                (precision == Constants.Precision.MOJO) ->
                    DecimalFormat("#,##0")

                (precision == Constants.Precision.TOTAL) ->
                    DecimalFormat("#,##0.00############", DecimalFormatSymbols(Locale.getDefault()))

                (precision == Constants.Precision.FIAT) ->
                    DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.getDefault()))

                else -> when {
                    (chiaAmount > Constants.BIG_AMOUNT_THRESHOLD) -> DecimalFormat("#,##0.##")

                    else -> DecimalFormat("#,##0.00####", DecimalFormatSymbols(Locale.getDefault()))
                }
            }
        return decimalFormat.format(chiaAmount)
    }

    fun setupWidgetUpdateWorker(context: Context) {
        val widgetUpdaterWorkRequest: PeriodicWorkRequest =

            PeriodicWorkRequestBuilder<WidgetUpdaterWork>(
                Constants.UPDATE_WORKER_INTERVAL_IN_MINUTES,
                TimeUnit.MINUTES
            )
                .addTag(Constants.UPDATE_WORKER_NAME)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                Constants.UPDATE_WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                widgetUpdaterWorkRequest
            )
    }

    fun openNotificationSettings(context: Context) {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
        } else {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context.getPackageName())
            intent.putExtra("app_uid", context.getApplicationInfo().uid)
        }
        context.startActivity(intent)
    }
}
