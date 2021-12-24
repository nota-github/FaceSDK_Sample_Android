package com.nota.hyundai_door.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList

class RegistrationRepository private constructor(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)

    companion object {

        private var instance: RegistrationRepository? = null
        private const val TAG = "RegistrationRepository"
        private const val KEY_MASK = "USER_LIST"

        fun createInstance(context: Context): RegistrationRepository {
            if(instance == null){
                instance = RegistrationRepository(context)
            }
            return instance!!
        }

        fun getInstance(): RegistrationRepository {
            return instance!!
        }

    }

    fun getUserList() : ArrayList<User> {
        val json = sharedPreferences.getString(KEY_MASK, null)
        json?.let {
            return Gson().fromJson(json, object : TypeToken<ArrayList<User>>() {}.type)
        } ?: run{
            return ArrayList()
        }
    }

    fun addUser(user: User) {
        val currentUserList = getUserList()
        currentUserList.add(user)
        val json = Gson().toJson(currentUserList)
        val edit = sharedPreferences.edit()
        edit.putString(KEY_MASK, json)
        edit.apply()
    }


}