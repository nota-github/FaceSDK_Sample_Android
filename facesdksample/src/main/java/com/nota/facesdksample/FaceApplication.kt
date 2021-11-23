package com.nota.facesdksample

import android.app.Application
import com.nota.nota_sdk.NotaFaceSDK
import com.nota.nota_sdk.ml.tflite.face.YoloFace

/* Initialize the SDK when the application runs.
    Register in AndroidManifest.xml */
class FaceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // SDK Init
        NotaFaceSDK.initialize(this, "key")
    }
}