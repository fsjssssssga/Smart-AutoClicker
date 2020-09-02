/*
 * Copyright (C) 2020 Nain57
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.ui.overlays

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.core.content.res.use
import androidx.core.graphics.toPoint

import com.buzbuz.smartautoclicker.R
import com.buzbuz.smartautoclicker.clicks.ClickInfo
import com.buzbuz.smartautoclicker.ui.base.OverlayMenuController

/**
 * [OverlayMenuController] implementation for displaying the click area selection menu and its overlay view.
 *
 * This class will display the overlay menu for selecting the area (or areas for swipe) for a click. The overlay view
 * displayed between the menu and the activity shows those areas.
 *
 * @param context the Android Context for the overlay menu shown by this controller.
 * @param type the type of click this overlay offers to select.
 * @param onClickSelectedListener listener on the type of click and area(s) to user have selected.
 */
class ClickSelectorMenu(
    context: Context,
    @ClickInfo.Companion.ClickType private val type: Int,
    private val onClickSelectedListener: (Int, Point, Point?) -> Unit
) : OverlayMenuController(context) {

    companion object {

        /** Defines the current step of selection the user is. */
        @IntDef(SINGLE, SWIPE_FROM, SWIPE_TO)
        @Retention(AnnotationRetention.SOURCE)
        private annotation class SelectionStep
        /** The user is currently selecting the position for a simple click. */
        private const val SINGLE = 1
        /** The user is currently selecting the start position for a swipe. */
        private const val SWIPE_FROM = 2
        /** The user us currently selecting the end position for a swipe. */
        private const val SWIPE_TO = 3
    }

    @LayoutRes override val menuLayoutRes: Int = R.layout.overlay_validation_menu
    override val screenOverlayView: View? = ClickSelectorView(context)

    /**
     * Current [SelectionStep] the user is.
     * Changing this value will update the overlay menu items accordingly, as well as displaying the toast providing
     * information about the current step.
     */
    @SelectionStep
    private var selectionStep: Int? = null
        set(value) {
            val stepValues = when (value) {
                SINGLE -> Pair(fromPosition != null, R.string.toast_configure_single_click)
                SWIPE_FROM -> Pair(fromPosition != null, R.string.toast_configure_swipe_from)
                SWIPE_TO -> Pair(toPosition != null, R.string.toast_configure_swipe_to)
                else -> return
            }

            setMenuItemViewEnabled(R.id.btn_confirm, stepValues.first)
            Toast.makeText(context, stepValues.second, Toast.LENGTH_SHORT).show()
            field = value
        }
    /**
     * First position selected by the user, null until one is selected.
     * When selecting a position for a simple click, it's the position of the click. When selecting a position for a
     * swipe, it's the start position of the swipe.
     */
    private var fromPosition: PointF? = null
        set(value) {
            field = value
            if (selectionStep == SINGLE || selectionStep == SWIPE_FROM) {
                setMenuItemViewEnabled(R.id.btn_confirm, value != null)
            }
            screenOverlayView?.invalidate()
        }
    /**
     * First position selected by the user, null until one is selected.
     * When selecting a position for a simple click, this value will always be null. When selecting a position for a
     * swipe, it's the end position of the swipe.
     */
    private var toPosition: PointF? = null
        set(value) {
            field = value
            if (selectionStep == SWIPE_TO) {
                setMenuItemViewEnabled(R.id.btn_confirm, value != null)
            }
            screenOverlayView?.invalidate()
        }

    override fun onMenuShown() {
        selectionStep = if (type == ClickInfo.SINGLE) SINGLE else SWIPE_FROM
    }

    override fun onItemClicked(viewId: Int) {
        when (viewId) {
            R.id.btn_confirm -> onConfirm()
            R.id.btn_cancel -> onCancel()
        }
    }

    /**
     * Confirm the position selected by the user and goes to the next selection step.
     * If this is the final step, notify the listener for the selection and dismiss the overlay.
     */
    private fun onConfirm() {
        when (selectionStep) {
            SINGLE -> {
                onClickSelectedListener.invoke(type, fromPosition!!.toPoint(), toPosition?.toPoint())
                dismiss()
            }
            SWIPE_FROM -> selectionStep = SWIPE_TO
            SWIPE_TO -> {
                onClickSelectedListener.invoke(type, fromPosition!!.toPoint(), toPosition!!.toPoint())
                dismiss()
            }
        }
    }

    /**
     * Cancel the position selected by the user and goes to the previous selection step.
     * If this is the initial step, dismiss the overlay without notifying the listener.
     */
    private fun onCancel() {
        when (selectionStep) {
            SINGLE, SWIPE_FROM  -> {
                dismiss()
            }
            SWIPE_TO -> {
                toPosition = null
                selectionStep = SWIPE_FROM
            }
        }
    }

    /** Overlay view used as [screenOverlayView] showing the positions selected by the user. */
    private inner class ClickSelectorView(context: Context) : View(context) {

        /** Paint drawing the inner circle drawn between the outlines. */
        private val paint = Paint()
        /** Paint drawing the outline of the [fromPosition] circle. */
        private val outlineFromPaint = Paint()
        /** Paint drawing the outline of the [toPosition] circle. */
        private val outlineToPaint = Paint()

        /** The inner circle radius. */
        private var radius: Float = 0f
        /** The inner outline circle radius. */
        private var outlineInRadius: Float = 0f
        /** The outer outline circle radius. */
        private var outlineOutRadius: Float = 0f

        init {
            context.obtainStyledAttributes(R.style.OverlaySelectorView_Click, R.styleable.ClickSelectorView).use { ta ->
                val thickness = ta.getDimensionPixelSize(R.styleable.ClickSelectorView_thickness, 4).toFloat()
                val innerThickness = ta.getDimensionPixelSize(R.styleable.ClickSelectorView_innerThickness, 2).toFloat()
                val outerThickness = (thickness - innerThickness) / 2

                radius = ta.getDimensionPixelSize(R.styleable.ClickSelectorView_radius, 30).toFloat()
                outlineInRadius = radius - (thickness - outerThickness) / 2
                outlineOutRadius = radius + (thickness - outerThickness) / 2

                outlineFromPaint.apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    color = ta.getColor(R.styleable.ClickSelectorView_colorOutlinePrimary, Color.RED)
                    strokeWidth = outerThickness
                }

                outlineToPaint.apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    color = ta.getColor(R.styleable.ClickSelectorView_colorOutlineSecondary, Color.GREEN)
                    strokeWidth = outerThickness
                }

                paint.apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    color = ta.getColor(R.styleable.ClickSelectorView_colorInner, Color.WHITE)
                    strokeWidth = innerThickness
                }
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action != MotionEvent.ACTION_DOWN || event.action != MotionEvent.ACTION_MOVE) {
                super.onTouchEvent(event)
            }

            when (selectionStep) {
                SINGLE, SWIPE_FROM -> fromPosition = PointF(event.x, event.y)
                SWIPE_TO -> toPosition = PointF(event.x, event.y)
            }

            return true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            fromPosition?.let { drawSelectorCircle(canvas, it, outlineFromPaint) }
            toPosition?.let { drawSelectorCircle(canvas, it, outlineToPaint) }
        }

        /**
         * Draw the selector circle at the specified position.
         *
         * @param canvas the canvas to draw the circles on.
         * @param position the position of the circle selector.
         * @param outlinePaint the paint used to draw the circle outline.
         */
        private fun drawSelectorCircle(canvas: Canvas, position: PointF, outlinePaint: Paint) {
            canvas.drawCircle(position.x, position.y, radius, paint)
            canvas.drawCircle(position.x, position.y, outlineOutRadius, outlinePaint)
            canvas.drawCircle(position.x, position.y, outlineInRadius, outlinePaint)
        }
    }
}