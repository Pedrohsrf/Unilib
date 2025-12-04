package com.example.unilib

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.dotlottie.dlplayer.Mode
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.widget.DotLottieAnimation

class LoadingDialog(context: Context) : Dialog(context) {

    init {
        val params = window!!.attributes
        params.gravity = Gravity.CENTER
        window!!.attributes = params
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_loading_dialog, null)

        setContentView(view)

        val config = Config.Builder()
            .autoplay(true)
            .speed(1f)
            .loop(true)
//            .source(DotLottieSource.Asset("swinging.json")) // file name of json/.lottie
            .source(DotLottieSource.Url("https://lottie.host/192cf67a-e861-4594-b538-3ccebef664cb/8R1bmfPfqy.lottie"))
//            .source(DotLottieSource.Url("https://lottiefiles-mobile-templates.s3.amazonaws.com/ar-stickers/swag_sticker_piggy.lottie"))
            .playMode(Mode.FORWARD)
            .useFrameInterpolation(true)
            .build()

        val dotLottieAnimationView = findViewById<DotLottieAnimation>(R.id.dot_lottie_view)

        dotLottieAnimationView.load(config)

        setTitle("TESTE")
        setCancelable(true)
        setOnCancelListener(null)
    }

}