package com.nota.hyundai_lobby.http

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nota.hyundai_lobby.data.Auth
import com.nota.hyundai_lobby.data.EnterLog
import com.nota.hyundai_lobby.data.Intruder
import com.nota.hyundai_lobby.data.User
import com.nota.nota_sdk.ai.support.image.BitmapUtil.resize
import com.nota.nota_sdk.task.vision.FacialProcess
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HttpRepository {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var instance: HttpRepository? = null
        private const val TAG = "UserRepository"

        fun createInstance(): HttpRepository {
            if(instance == null){
                instance = HttpRepository()
            }
            return instance!!
        }

        fun getInstance(): HttpRepository {
            return instance!!
        }

    }

    fun getUsers(onSuccess:(ArrayList<User>)->Unit) {

        val api = ManagementApiClient.getInstance().create(HyundaiAPI::class.java)
        Thread {
            api.getMembers().enqueue(object : Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    response.body()?.let {
                        onSuccess(Gson().fromJson(JSONObject(it.string())["members"].toString()
                            , object : TypeToken<ArrayList<User>>(){}.type))
                    } ?: run {
                        onSuccess(arrayListOf())
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }.start()

    }

    fun regUser(user: User, face: Bitmap, onSuccess: () -> Unit, onFailure:() -> Unit){

        val param = HashMap<String,Any>()
        param["dong"] = user.dong
        param["ho"] = user.ho
        param["img"] = HttpUtil.convertBitmapToString(face, 100)
        param["name"] = user.name
        val json = GsonBuilder().disableHtmlEscaping().serializeNulls().create().toJson(param)
        Log.d(TAG, "Request : $json")

        ManagementApiClient.getInstance().create(HyundaiAPI::class.java).regMember(json).enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful){
                        Log.d(TAG, "Response : " + response.body()?.string())
                        onSuccess()
                    }else{
                        Log.d(TAG, "Response : " + response.code())
                        onFailure()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                    onFailure()
                }
            }
        )
    }

    fun regVisitor(user: User, startDate: String, endDate: String, info: String, face: Bitmap, onSuccess: () -> Unit, onFailure:() -> Unit){

        val param = HashMap<String,Any>()
        param["dong"] = user.dong
        param["ho"] = user.ho
        param["img"] = HttpUtil.convertBitmapToString(face, 100)
        param["start_date"] = startDate
        param["end_date"] = endDate
        param["info"] = info
        param["name"] = user.name

        val json = GsonBuilder().disableHtmlEscaping().serializeNulls().create().toJson(param)
        Log.d(TAG, "Request : $json")

        ManagementApiClient.getInstance().create(HyundaiAPI::class.java).regVisitor(json).enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful){
                        Log.d(TAG, "Response : " + response.body()?.string())
                        onSuccess()
                    }else{
                        Log.d(TAG, "Response : " + response.code())
                        onFailure()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                    onFailure()
                }
            }
        )
    }

    fun removeUser(user: User){
        val param = HashMap<String,String>()
        param["id"] = user.id

        val api = ManagementApiClient.getInstance().create(HyundaiAPI::class.java)
        Thread {
            api.delMember(Gson().toJson(param)).enqueue(object : Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d(TAG, "removeUser : $response")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }.start()
    }

    fun getUserEnterHistory(gateId : String,onSuccess:(ArrayList<EnterLog>)->Unit) {

        val param = HashMap<String,String>()
        param["gateId"] = gateId

        val api = ManagementApiClient.getInstance().create(HyundaiAPI::class.java)
        Thread {
            api.getHistory(Gson().toJson(param)).enqueue(object : Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful){

                        response.body()?.let {
                            onSuccess(Gson().fromJson(JSONObject(it.string())["history"].toString()
                                , object : TypeToken<ArrayList<EnterLog>>(){}.type))
                        } ?: run {
                            onSuccess(arrayListOf())
                        }
                    }else{
                        Log.d("","")
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }.start()
    }

    fun getIntruder(gateId : String,onSuccess:(ArrayList<Intruder>)->Unit) {

        val param = HashMap<String,String>()
        param["gateId"] = gateId

        val api = ManagementApiClient.getInstance().create(HyundaiAPI::class.java)
        Thread {
            api.getIntruder(Gson().toJson(param)).enqueue(object : Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful){
                        response.body()?.let {
                            onSuccess(Gson().fromJson(JSONObject(it.string())["intruders"].toString()
                                , object : TypeToken<ArrayList<Intruder>>(){}.type))
                        } ?: run {
                            onSuccess(arrayListOf())
                        }

                    }else{
                        Log.d("","")
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }.start()
    }

    fun auth(gateId: String, facialData: FacialProcess.FaceDetectResult, onComplete: (Boolean) -> Unit){

        val param = HashMap<String,String>()
        param["gateId"] = gateId
        param["img"] = HttpUtil.convertBitmapToString(facialData.detectedFaceBitmap.resize(112,112),100)
        facialData.isSpoof?.let {
            param["spoof"] = if(it) "0" else "1"
        } ?: run {
            param["spoof"] = "1"
        }

        val json = Gson().toJson(param)

        val api = ManagementApiClient.getInstance().create(HyundaiAPI::class.java)
        api.getAuth(json).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                val auth = Gson().fromJson(response.body()?.string(), Auth::class.java)
                if(auth != null) {
                    if (auth.msg == "authorized") {
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(false)
            }
        })

    }


}