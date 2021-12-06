import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class HeaderDecoration(context: Context?, parent: RecyclerView, @LayoutRes resId: Int) :
    ItemDecoration() {
    private val mLayout: View = LayoutInflater.from(context).inflate(resId, parent, false)

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        // layout basically just gets drawn on the reserved space on top of the first view
        mLayout.layout(parent.left, 0, parent.right, mLayout.measuredHeight)
        for (i in 0 until parent.childCount) {
            val view: View = parent.getChildAt(i)
            if (parent.getChildAdapterPosition(view) == 0) {
                c.save()
                val height: Int = mLayout.measuredHeight
                val top: Float = (view.top - height).toFloat()
                c.translate(0f, top)
                mLayout.draw(c)
                c.restore()
                break
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (mLayout.measuredHeight <= 0) {
            mLayout.measure(
                View.MeasureSpec.makeMeasureSpec(parent.measuredWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(parent.measuredHeight, View.MeasureSpec.AT_MOST)
            )
        }
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.set(0, mLayout.measuredHeight, 0, 0)
        } else {
            outRect.setEmpty()
        }
    }
}
