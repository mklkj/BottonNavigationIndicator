package ademar.bnindicator.indicator

import ademar.bnindicator.indicator.R.styleable.*
import android.animation.AnimatorSet
import android.animation.ObjectAnimator.ofInt
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.TRANSPARENT
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.bottomnavigation.BottomNavigationMenuView

class BottomNavigationViewIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val maxWidth: Int

    private val targetId: Int
    private var target: BottomNavigationMenuView? = null

    private var rect = Rect()
    private val backgroundDrawable: Drawable

    private var index = 0
    private var animator: AnimatorSet? = null

    init {
        if (attrs == null) {
            targetId = NO_ID
            backgroundDrawable = ColorDrawable(TRANSPARENT)
            maxWidth = 0
        } else {
            with(context.obtainStyledAttributes(attrs, BottomNavigationViewIndicator)) {
                targetId =
                    getResourceId(BottomNavigationViewIndicator_targetBottomNavigation, NO_ID)
                val clipableId =
                    getResourceId(BottomNavigationViewIndicator_clipableBackground, NO_ID)
                maxWidth = getDimensionPixelSize(BottomNavigationViewIndicator_indicatorMaxWidth, 0)

                backgroundDrawable = if (clipableId != NO_ID) {
                    getDrawable(context, clipableId) ?: ColorDrawable(TRANSPARENT)
                } else {
                    ColorDrawable(
                        getColor(BottomNavigationViewIndicator_clipableBackground, TRANSPARENT)
                    )
                }
                recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (targetId == NO_ID) return attachedError("Invalid target id $targetId, did you set the app:targetBottomNavigation attribute?")
        val parentView =
            parent as? View ?: return attachedError("Impossible to find the view using $parent")

        val child = parentView.findViewById<View?>(targetId)
        if (child !is ListenableBottomNavigationView) return attachedError("Invalid view $child, the app:targetBottomNavigation has to be n ListenableBottomNavigationView")

        for (i in 0 until child.childCount) {
            val subView = child.getChildAt(i)
            if (subView is BottomNavigationMenuView) target = subView
        }
        elevation = child.elevation
        child.addOnNavigationItemSelectedListener { updateRectByIndex(it, true) }
        post { updateRectByIndex(index, false) }
    }

    private fun attachedError(message: String) {
        Log.e("BNVIndicator", message)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        target = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.clipRect(rect)
        backgroundDrawable.draw(canvas)
    }

    fun onItemSelected(index: Int) {
        updateRectByIndex(index, true)
    }

    private fun updateRectByIndex(index: Int, animated: Boolean) {
        this.index = index
        target?.apply {
            if (childCount < 1 || index >= childCount) return
            val reference = getChildAt(index)

            val referenceWidth = reference.width
            val horizontalMargin = ((referenceWidth - maxWidth) / 2f).toInt()

            val start = reference.left + left + horizontalMargin
            val end = reference.right + left - horizontalMargin

            backgroundDrawable.setBounds(left, top, right, bottom)
            val newRect = Rect(start, 0, end, height)
            if (animated) startUpdateRectAnimation(newRect) else updateRect(newRect)
        }
    }

    private fun startUpdateRectAnimation(rect: Rect) {
        animator?.cancel()
        animator = AnimatorSet().also {
            it.playTogether(
                ofInt(this, "rectLeft", this.rect.left, rect.left),
                ofInt(this, "rectRight", this.rect.right, rect.right),
                ofInt(this, "rectTop", this.rect.top, rect.top),
                ofInt(this, "rectBottom", this.rect.bottom, rect.bottom)
            )
            it.interpolator = FastOutSlowInInterpolator()
            it.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            it.start()
        }
    }

    private fun updateRect(rect: Rect) {
        this.rect = rect
        postInvalidate()
    }

    override fun invalidate() {
        updateRectByIndex(index, false)
        super.invalidate()
    }

    override fun setVisibility(visibility: Int) {
        if (visibility == VISIBLE && this.visibility != visibility) {
            invalidate()
        }
        super.setVisibility(visibility)
    }

    @Keep
    fun setRectLeft(left: Int) = updateRect(rect.apply { this.left = left })

    @Keep
    fun setRectRight(right: Int) = updateRect(rect.apply { this.right = right })

    @Keep
    fun setRectTop(top: Int) = updateRect(rect.apply { this.top = top })

    @Keep
    fun setRectBottom(bottom: Int) = updateRect(rect.apply { this.bottom = bottom })
}