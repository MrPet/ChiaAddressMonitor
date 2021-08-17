package ninja.bored.chiapublicaddressmonitor

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase

class WidgetUpdaterWork(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "WidgetUpdaterWork"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doing my part")
        // first get all widgets
        val database = ChiaWidgetRoomsDatabase.getInstance(this.applicationContext)
        // update conversions
        Slh.refreshAllFiatConversionWidgets(
            database.getWidgetFiatConversionSettingsDao().loadAll(),
            this.applicationContext
        )

        // refresh all Normal widgets
        Slh.refreshAllAddressWidgets(
            database.getWidgetSettingsAndDataDao().loadAll(),
            this.applicationContext,
            database,
            false
        )

        // done
        return Result.success()
    }
}
