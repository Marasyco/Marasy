package com.blueray.marasy.helpers

import android.view.View
import android.view.animation.Animation
import android.widget.EditText

object ViewUtils {

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.inVisible() {
        visibility = View.INVISIBLE
    }

    fun EditText.isInputEmpty(): Boolean {
        return text.toString().trim().isEmpty()
    }
    fun View.startAnimation(animation: Animation, onEnd : ()->Unit){
        animation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                onEnd()
            }

            override fun onAnimationRepeat(animation: Animation?) {
//                TODO("Not yet implemented")
            }

        })
        this.startAnimation(animation)
    }

}