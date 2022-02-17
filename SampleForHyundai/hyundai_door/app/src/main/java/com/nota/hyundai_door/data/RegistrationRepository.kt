package com.nota.hyundai_door.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList

class RegistrationRepository private constructor(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)

    enum class RegistrationErrorType {
        SUCCESS, DUPLICATED_NAME, DUPLICATED_ID, OVERCROWDING
    }

    companion object {

        private var instance: RegistrationRepository? = null
        private const val TAG = "RegistrationRepository"
        private const val KEY_MASK = "USER_LIST"
        const val MAX_USER_COUNT = 10

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

    fun addUser(user: User) : RegistrationErrorType {

        val userList = getUserList()

        if(userList.size >= MAX_USER_COUNT){
            // 등록 인원 10명 제한
            return RegistrationErrorType.OVERCROWDING
        }

        userList.forEach {
            // 이름 중복 방지
            if(it.name == user.name){
                return RegistrationErrorType.DUPLICATED_NAME
            }

            // 아이디 중복 방지
            if(it.id == user.id){
                return RegistrationErrorType.DUPLICATED_ID
            }
        }

        val currentUserList = getUserList()
        currentUserList.add(user)
        val json = Gson().toJson(currentUserList)
        val edit = sharedPreferences.edit()
        edit.putString(KEY_MASK, json)
        edit.apply()
        return RegistrationErrorType.SUCCESS
    }

    fun deleteUserIndex(index: Int) : RegistrationErrorType {
        val userList = getUserList()
        userList.removeAt(index)
        val json = Gson().toJson(userList)
        val edit = sharedPreferences.edit()
        edit.putString(KEY_MASK, json)
        edit.apply()
        return RegistrationErrorType.SUCCESS
    }


}