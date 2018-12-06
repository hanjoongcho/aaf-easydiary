package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.ViewGroup
import io.github.aafactory.commons.activities.BaseSimpleActivity
import kotlinx.android.synthetic.main.activity_lock_setting.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R

class FingerprintActivity : BaseSimpleActivity() {
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)
    }

    override fun onResume() {
        isBackgroundColorFromPrimaryColor = true
        super.onResume()
        FontUtils.setFontsTypeface(applicationContext, assets, null, container)
    }

    override fun getMainViewGroup(): ViewGroup? = container
}