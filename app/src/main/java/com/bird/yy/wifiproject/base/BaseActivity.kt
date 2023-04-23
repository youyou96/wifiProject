package com.bird.yy.wifiproject.base

import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.utils.ViewBindingUtil
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar


abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    lateinit var binding: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        ImmersionBar.with(this).init()
        layoutInflater
        binding = ViewBindingUtil.create(javaClass, layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
    }


    protected open fun jumpActivityFinish(clazz: Class<*>?) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED || lifecycle.currentState == Lifecycle.State.STARTED) {
            startActivity(Intent(this, clazz))
            finish()
        }
    }

    protected open fun jumpActivity(clazz: Class<*>?) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED || lifecycle.currentState == Lifecycle.State.STARTED) {
            startActivity(Intent(this, clazz))
        }
    }

    protected open fun showDialogByActivity(
        content: String,
        sure: String,
        cancel: Boolean = true,
        listener: OnClickListener?
    ) {
        val alertDialog = AlertDialog.Builder(this)
            .setMessage(content)
            .setCancelable(cancel)
            .setPositiveButton(sure, listener)
            .create()
        alertDialog.show()
    }

    protected open fun showDialogByActivity(
        content: String,
        positionContent: String,
        cancel: Boolean = true,
        listener: OnClickListener?,
        negativeContent: String,
        negativeListener: OnClickListener?
    ) {
        val alertDialog = AlertDialog.Builder(this)
            .setMessage(content)
            .setCancelable(cancel)
            .setPositiveButton(positionContent, listener)
            .setNegativeButton(negativeContent, negativeListener)
            .create()
        alertDialog.show()
    }
}