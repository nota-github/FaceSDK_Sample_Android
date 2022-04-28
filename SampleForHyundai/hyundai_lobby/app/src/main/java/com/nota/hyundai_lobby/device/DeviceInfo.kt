package com.nota.hyundai_lobby.data


import android.content.Context
import android.provider.Settings

object DeviceInfo{
    /**
     * 단말기 고유 값입니다.
     * 디비이스가 boot 될때 생성되는 최초 64-bit 값입니다
     */
    fun getDeviceID(context : Context) : String{
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
    }
}