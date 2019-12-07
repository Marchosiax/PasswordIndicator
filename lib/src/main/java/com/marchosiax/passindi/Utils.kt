package com.marchosiax.passindi

import android.animation.Animator
import android.content.Context

internal fun Int.dp(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

internal fun Float.dp(context: Context): Float = this * context.resources.displayMetrics.density

internal fun Animator.onAnimationEnd(action: (Animator?) -> Unit) {
    addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            action(animation)
        }

        override fun onAnimationCancel(animation: Animator?) {

        }

        override fun onAnimationStart(animation: Animator?) {

        }

    })
}