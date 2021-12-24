package com.nota.facesdksample

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.nota.nota_sdk.NotaFaceSDK

/* Initialize the SDK when the application runs.
    Register in AndroidManifest.xml */
class FaceApplication : Application() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        // SDK Init
        NotaFaceSDK.initialize(this,"")
    }
}