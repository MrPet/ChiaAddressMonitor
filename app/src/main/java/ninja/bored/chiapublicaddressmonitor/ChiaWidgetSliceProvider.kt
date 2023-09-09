package ninja.bored.chiapublicaddressmonitor

import android.net.Uri
import androidx.slice.Slice
import androidx.slice.SliceProvider
import androidx.slice.builders.ListBuilder.INFINITY
import androidx.slice.builders.list
import androidx.slice.builders.row


class ChiaWidgetSliceProvider : SliceProvider() {
    companion object {
        const val SLICE_AUTHORITY = "ninja.bored.chiapublicaddressmonitor"
    }

    override fun onBindSlice(sliceUri: Uri): Slice? {
        return createSlice(sliceUri)
    }

    fun createSlice(sliceUri: Uri?): Slice? {
        return sliceUri?.let { sliceUriF ->
            context?.let { contextF ->
                list(contextF, sliceUriF, INFINITY) {
                    this.row { setTitle("URI not found.") }
                }
            }
        }
    }

    override fun onCreateSliceProvider(): Boolean = true
}
