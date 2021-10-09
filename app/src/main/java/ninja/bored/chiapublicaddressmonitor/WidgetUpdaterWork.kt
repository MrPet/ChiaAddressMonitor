package ninja.bored.chiapublicaddressmonitor

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.helpers.WidgetHelper
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase

class WidgetUpdaterWork(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "WidgetUpdaterWork"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doing my part .. Working")
        // first get all widgets
        val database = ChiaWidgetRoomsDatabase.getInstance(this.applicationContext)
        // update conversions
        WidgetHelper.refreshAllFiatConversionWidgets(
            database.getWidgetFiatConversionSettingsDao().loadAll(),
            this.applicationContext
        )

        // refresh all Normal widgets
        WidgetHelper.refreshAllAddressWidgets(
            database.getWidgetSettingsAndDataDao().loadAll(),
            this.applicationContext,
            database,
            false
        )

        // refresh all grouping widgets
        WidgetHelper.refreshAllGroupedWidgets(
            database.getWidgetAddressGroupSettingsWithAddressesDao().loadAll(),
            this.applicationContext,
            database
        )

        // done
        return Result.success()
    }
}
