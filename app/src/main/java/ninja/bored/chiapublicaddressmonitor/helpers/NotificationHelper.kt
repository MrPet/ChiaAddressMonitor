package ninja.bored.chiapublicaddressmonitor.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import ninja.bored.chiapublicaddressmonitor.model.WidgetData

object NotificationHelper {
    fun createNotificationChannels(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // negative
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val negativeChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_NEGATIVE_CHANGE,
                context.getString(R.string.balance_negative_changed_channel_name),
                importance
            ).apply {
                description =
                    context.getString(R.string.balance_negative_changed_channel_description)
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(negativeChannel)

            // positive
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_POSITIVE_CHANGE,
                context.getString(R.string.balance_positive_changed_channel_name),
                importance
            ).apply {
                description =
                    context.getString(R.string.balance_positive_changed_channel_description)
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(
        channelID: String,
        contentTitle: String,
        contentText: String,
        notificationID: Int,
        context: Context
    ) {
        // not the same
        val builder = NotificationCompat.Builder(
            context,
            channelID
        )
            .setSmallIcon(R.drawable.ic_chia_address_widget)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setOnlyAlertOnce(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(context.getColor(R.color.chia))
            .setAutoCancel(false)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, builder.build())
        }
    }

    suspend fun checkIfNecessaryAndSendNotification(
        oldChiaData: WidgetData?,
        newWidgetData: WidgetData,
        context: Context
    ) {
        val database = ChiaWidgetRoomsDatabase.getInstance(context)
        val addressSettingsDao = database.getAddressSettingsDao()
        val addressSettings = addressSettingsDao.getByAddress(newWidgetData.chiaAddress)

        if (addressSettings?.showNotification == true &&
            oldChiaData != null &&
            newWidgetData.chiaAmount != oldChiaData.chiaAmount &&
            newWidgetData.updateDate.after(oldChiaData.updateDate)
        ) {
            val addressStringOrSynonym = when (addressSettings.chiaAddressSynonym) {
                null -> newWidgetData.chiaAddress
                else -> addressSettings.chiaAddressSynonym
            }
            if (newWidgetData.chiaAmount > oldChiaData.chiaAmount) {
                sendNotification(
                    Constants.NOTIFICATION_CHANNEL_POSITIVE_CHANGE,
                    context.getString(
                        R.string.address_balance_changed_notification_header,
                        Slh.formatChiaDecimal(
                            newWidgetData.chiaAmount - oldChiaData.chiaAmount, Constants.Precision.TOTAL
                        ),
                        ForkHelper.getCurrencySymbolFromAddress(newWidgetData.chiaAddress)
                    ),
                    context.getString(
                        R.string.address_balance_changed_notification_text,
                        addressStringOrSynonym,
                        Slh.formatChiaDecimal(oldChiaData.chiaAmount, Constants.Precision.TOTAL),
                        ForkHelper.getCurrencySymbolFromAddress(newWidgetData.chiaAddress),
                        Slh.formatChiaDecimal(newWidgetData.chiaAmount, Constants.Precision.TOTAL),
                        ForkHelper.getCurrencySymbolFromAddress(newWidgetData.chiaAddress)
                    ),
                    Constants.NOTIFICATION_ID_POSITIVE_CHANGE,
                    context
                )
            } else {
                sendNotification(
                    Constants.NOTIFICATION_CHANNEL_NEGATIVE_CHANGE,
                    context.getString(
                        R.string.address_balance_changed_negative_notification_header,
                        Slh.formatChiaDecimal(newWidgetData.chiaAmount, Constants.Precision.TOTAL),
                        ForkHelper.getCurrencySymbolFromAddress(newWidgetData.chiaAddress)
                    ),
                    context.getString(
                        R.string.address_balance_changed_negative_notification_text,
                        addressStringOrSynonym,
                        Slh.formatChiaDecimal(oldChiaData.chiaAmount, Constants.Precision.TOTAL),
                        ForkHelper.getCurrencySymbolFromAddress(newWidgetData.chiaAddress),
                        Slh.formatChiaDecimal(newWidgetData.chiaAmount, Constants.Precision.TOTAL),
                        ForkHelper.getCurrencySymbolFromAddress(newWidgetData.chiaAddress)
                    ),
                    Constants.NOTIFICATION_ID_NEGATIVE_CHANGE,
                    context
                )
            }
        }
    }
}
