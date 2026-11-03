package com.shilapi.xcertplay

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.shilapi.xcertplay.host.R
import com.shilapi.xcertplay.settings.SettingsTheme

/**
 * The app's only dialog look: a dark rounded card with bold title, tap-to-apply option rows and
 * right-aligned text actions. Car ROMs restyle platform AlertDialogs (light list backgrounds
 * against dark-theme item text produced invisible options on the W6), so nothing here goes
 * through the platform dialog chrome — every pixel is drawn by the app.
 */
internal object UiDialogs {
    private val cardColor = Color.rgb(21, 30, 44)
    private val borderColor = Color.rgb(42, 56, 75)
    private val rowColor = Color.rgb(12, 17, 27)
    private val rowSelectedColor = Color.rgb(31, 43, 61)
    private val textColor = SettingsTheme.CARD.textPrimary
    private val muted = SettingsTheme.CARD.textSecondary
    private val accent = SettingsTheme.CARD.accent

    /**
     * Interactive handle to a shown dialog. Unit tests (Kotlin friend module) drive these
     * custom [Dialog]s through it — platform `ShadowAlertDialog` only tracks `AlertDialog`,
     * which the app deliberately stopped using (see the file header).
     */
    internal class DialogLeash(
        val dialog: Dialog,
        val title: CharSequence,
        val options: List<View>,
    ) {
        internal var negative: View? = null
        internal var positive: View? = null
        internal var neutral: View? = null
        /** Pending row for [pickConfirm]; plain [pick] dismisses on tap. */
        internal var selectedIndex: Int = -1

        fun clickOption(index: Int) { options[index].performClick() }
        fun clickNegative() { negative?.performClick() ?: error("dialog has no negative action") }
        fun clickPositive() { positive?.performClick() ?: error("dialog has no positive action") }
        fun clickNeutral() { neutral?.performClick() ?: error("dialog has no neutral action") }
    }

    /** The most recently shown dialog, for tests. Overwritten on each show; may be reset to null. */
    internal var lastLeash: DialogLeash? = null

    private fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    private fun card(): GradientDrawable = GradientDrawable().apply {
        setColor(cardColor)
        setStroke(2, borderColor)
    }

    private fun titleView(context: Context, value: CharSequence): TextView =
        TextView(context).apply {
            text = value
            textSize = 20f
            setTextColor(textColor)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }

    /** Right-aligned text action; the accent colour marks the primary one. */
    private fun action(context: Context, label: CharSequence, primary: Boolean, click: () -> Unit): TextView =
        TextView(context).apply {
            text = label
            textSize = 16f
            setTextColor(if (primary) accent else muted)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(dp(context, 22), dp(context, 16), dp(context, 22), dp(context, 16))
            setOnClickListener { click() }
        }

    private fun newDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    private fun layoutDialog(dialog: Dialog, widthHint: Int) {
        dialog.show()
        // Car ROMs measure dialog windows against the full stitched display; pin a sane width.
        val windowWidth = dialog.context.resources.displayMetrics.widthPixels
        // S8: clamp in one pass so the 320dp floor cannot exceed the window on narrow
        // displays (the old coerceAtLeast(320) ran after coerceAtMost and re-widened it).
        val available = (windowWidth - dp(dialog.context, 48)).coerceAtLeast(1)
        val width = widthHint.coerceIn(
            minOf(dp(dialog.context, 320), available),
            available,
        )
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun column(context: Context): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        background = card()
        setPadding(dp(context, 24), dp(context, 24), dp(context, 20), dp(context, 12))
    }

    private fun actionRow(context: Context, dialog: Dialog, negative: CharSequence?, positive: CharSequence?,
        onPositive: (() -> Unit)?, leash: DialogLeash? = null): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        if (negative != null) {
            val view = action(context, negative, primary = false) { dialog.dismiss() }
            leash?.negative = view
            row.addView(view)
        }
        if (positive != null && onPositive != null) {
            val view = action(context, positive, primary = true) { onPositive() }
            leash?.positive = view
            row.addView(view)
        }
        return row
    }

    /**
     * Single choice: tapping a row applies it and dismisses — no confirm button, which car ROMs
     * kept clipping out of the dialog window. Optional [negative]/[neutral] actions sit at the
     * bottom of the card (e.g. "manual input" / "cancel").
     */
    fun pick(context: Context, title: CharSequence, options: List<CharSequence>, current: Int,
        neutral: CharSequence? = null, onNeutral: (() -> Unit)? = null,
        negative: CharSequence? = null, onNegative: (() -> Unit)? = null,
        onPick: (Int) -> Unit): Dialog {
        val dialog = newDialog(context)
        val content = column(context)
        content.addView(titleView(context, title))
        content.addView(space(context, 16))
        val maxHeight = (context.resources.displayMetrics.heightPixels * 0.55f).toInt()
        val optionsList = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val scroll = MaxHeightScrollView(context, maxHeight).apply {
            isFillViewport = true
            addView(optionsList, ViewGroup.LayoutParams(-1, -2))
        }
        val optionRows = ArrayList<View>()
        options.forEachIndexed { index, option ->
            val row = TextView(context).apply {
                text = if (index == current) "✓  $option" else "    $option"
                textSize = 17f
                setTextColor(textColor)
                setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 14))
                background = GradientDrawable().apply {
                    setColor(if (index == current) rowSelectedColor else rowColor)
                    cornerRadius = dp(context, 12).toFloat()
                    setStroke(2, borderColor)
                }
                setOnClickListener {
                    lastLeash?.selectedIndex = index
                    dialog.dismiss()
                    onPick(index)
                }
            }
            optionRows.add(row)
            optionsList.addView(row, LinearLayout.LayoutParams(-1, -2))
            optionsList.addView(space(context, 8))
        }
        content.addView(scroll, LinearLayout.LayoutParams(-1, -2))
        val leash = DialogLeash(dialog, title, optionRows).apply { selectedIndex = current }
        if (neutral != null || negative != null) {
            content.addView(space(context, 8))
            val actions = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }
            if (neutral != null && onNeutral != null) {
                val view = action(context, neutral, primary = false) {
                    dialog.dismiss()
                    onNeutral()
                }
                leash.neutral = view
                actions.addView(view)
            }
            if (negative != null) {
                // A cancel action is dismiss-only; the callback is optional (form() behaves
                // the same). Requiring onNegative hid the button from choice() callers that
                // only pass a label.
                val view = action(context, negative, primary = false) {
                    dialog.dismiss()
                    onNegative?.invoke()
                }
                leash.negative = view
                actions.addView(view)
            }
            content.addView(actions)
        }
        dialog.setContentView(content)
        lastLeash = leash
        layoutDialog(dialog, dp(context, 560))
        return dialog
    }

    /**
     * Single choice with confirm actions, for pickers whose rows do extra work before applying
     * (e.g. playing a preview). Rows move a pending selection; the accent action applies it.
     */
    fun pickConfirm(context: Context, title: CharSequence, options: List<CharSequence>, current: Int,
        onPreview: ((Int) -> Unit)? = null, onDismiss: (() -> Unit)? = null,
        onApply: (Int) -> Unit): Dialog {
        val dialog = newDialog(context)
        val content = column(context)
        content.addView(titleView(context, title))
        content.addView(space(context, 16))
        val maxHeight = (context.resources.displayMetrics.heightPixels * 0.55f).toInt()
        val optionsList = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val scroll = MaxHeightScrollView(context, maxHeight).apply {
            isFillViewport = true
            addView(optionsList, ViewGroup.LayoutParams(-1, -2))
        }
        var pending = current
        val rows = ArrayList<TextView>()
        val leash = DialogLeash(dialog, title, rows).apply { selectedIndex = current }
        options.forEachIndexed { index, option ->
            fun style(row: TextView) {
                row.text = if (index == pending) "✓  $option" else "    $option"
                row.background = GradientDrawable().apply {
                    setColor(if (index == pending) rowSelectedColor else rowColor)
                    cornerRadius = dp(context, 12).toFloat()
                    setStroke(2, borderColor)
                }
            }
            val row = TextView(context).apply {
                textSize = 17f
                setTextColor(textColor)
                setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 14))
                setOnClickListener {
                    pending = index
                    leash.selectedIndex = index
                    rows.forEach { style(it) }
                    onPreview?.invoke(index)
                }
            }
            style(row)
            rows.add(row)
            optionsList.addView(row, LinearLayout.LayoutParams(-1, -2))
            optionsList.addView(space(context, 8))
        }
        content.addView(scroll, LinearLayout.LayoutParams(-1, -2))
        content.addView(space(context, 8))
        content.addView(actionRow(context, dialog, negative = context.getString(R.string.cancel),
            positive = context.getString(R.string.save), leash = leash, onPositive = {
                dialog.dismiss()
                onApply(pending)
            }))
        if (onDismiss != null) dialog.setOnDismissListener { onDismiss() }
        dialog.setContentView(content)
        lastLeash = leash
        layoutDialog(dialog, dp(context, 560))
        return dialog
    }

    /** Title + message + right-aligned actions (positive/negative/neutral all optional). */
    fun confirm(context: Context, title: CharSequence, message: CharSequence,
        positive: CharSequence? = null, negative: CharSequence? = null, neutral: CharSequence? = null,
        onNeutral: ((Dialog) -> Unit)? = null, onNegative: ((Dialog) -> Unit)? = null,
        onPositive: ((Dialog) -> Unit)? = null): Dialog {
        val dialog = newDialog(context)
        val content = column(context)
        content.addView(titleView(context, title))
        content.addView(space(context, 12))
        content.addView(TextView(context).apply {
            text = message
            textSize = 16f
            setTextColor(muted)
            setLineSpacing(dp(context, 4).toFloat(), 1f)
        })
        content.addView(space(context, 16))
        val leash = DialogLeash(dialog, title, emptyList())
        val actions = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        if (neutral != null && onNeutral != null) {
            val view = action(context, neutral, primary = false) {
                dialog.dismiss()
                onNeutral(dialog)
            }
            leash.neutral = view
            actions.addView(view)
        }
        if (negative != null) {
            val view = action(context, negative, primary = false) {
                dialog.dismiss()
                onNegative?.invoke(dialog)
            }
            leash.negative = view
            actions.addView(view)
        }
        if (positive != null && onPositive != null) {
            val view = action(context, positive, primary = true) {
                dialog.dismiss()
                onPositive(dialog)
            }
            leash.positive = view
            actions.addView(view)
        }
        content.addView(actions)
        dialog.setContentView(content)
        lastLeash = leash
        layoutDialog(dialog, dp(context, 560))
        return dialog
    }

    /** Title + message + a single close action. */
    fun message(context: Context, title: CharSequence, message: CharSequence,
        close: CharSequence): Dialog =
        confirm(context, title, message, positive = close, negative = null, onPositive = { it.dismiss() })

    /**
     * Title + a caller-built content view + right-aligned actions. [onPositive] decides whether
     * to dismiss, so form validation can keep the dialog open (mirrors the old create()+getButton
     * pattern). [neutral] adds a middle action (e.g. "hide keyboard"); [positive] may be null for
     * dismiss-only dialogs.
     */
    fun form(context: Context, title: CharSequence, content: View,
        positive: CharSequence? = null, negative: CharSequence? = null, neutral: CharSequence? = null,
        onNeutral: ((Dialog) -> Unit)? = null, onNegative: ((Dialog) -> Unit)? = null,
        onPositive: ((Dialog) -> Unit)? = null): Dialog {
        val dialog = newDialog(context)
        val root = column(context)
        root.addView(titleView(context, title))
        root.addView(space(context, 12))
        // Cap the form body like pick/pickConfirm: without the limit a tall form pushes the
        // action row off a short landscape screen (M12).
        val maxHeight = (context.resources.displayMetrics.heightPixels * 0.55f).toInt()
        val scroll = MaxHeightScrollView(context, maxHeight).apply {
            isFillViewport = true
            addView(content, ViewGroup.LayoutParams(-1, -2))
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, -2))
        root.addView(space(context, 12))
        val leash = DialogLeash(dialog, title, emptyList())
        val actions = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        if (neutral != null && onNeutral != null) {
            val view = action(context, neutral, primary = false) { onNeutral(dialog) }
            leash.neutral = view
            actions.addView(view)
        }
        if (negative != null) {
            val view = action(context, negative, primary = false) {
                dialog.dismiss()
                onNegative?.invoke(dialog)
            }
            leash.negative = view
            actions.addView(view)
        }
        if (positive != null && onPositive != null) {
            val view = action(context, positive, primary = true) { onPositive(dialog) }
            leash.positive = view
            actions.addView(view)
        }
        root.addView(actions)
        dialog.setContentView(root)
        lastLeash = leash
        layoutDialog(dialog, dp(context, 620))
        return dialog
    }

    private fun space(context: Context, height: Int): View = View(context).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(context, height))
    }

    /**
     * A ScrollView whose height wraps content but never exceeds [maxHeight] pixels.
     * Lets option lists scroll in dialogs without pushing title/actions off screen.
     */
    private class MaxHeightScrollView(context: Context, private val maxHeight: Int) : ScrollView(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            // S8: honour the parent's height limit too — replacing the spec outright
            // dropped it and could overflow a short parent.
            val parentMode = MeasureSpec.getMode(heightMeasureSpec)
            val parentSize = MeasureSpec.getSize(heightMeasureSpec)
            val cap = when (parentMode) {
                MeasureSpec.EXACTLY -> parentSize
                MeasureSpec.AT_MOST -> minOf(maxHeight, parentSize)
                else -> maxHeight
            }
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(cap, MeasureSpec.AT_MOST))
        }
    }
}
