package com.plucky.wallet.controller

import android.os.AsyncTask
import com.plucky.wallet.config.MapToJson
import com.plucky.wallet.model.Url
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class WebOldController {
  class Post(private var body: HashMap<String, String>) : AsyncTask<Void, Void, JSONObject>() {
    override fun doInBackground(vararg p0: Void?): JSONObject {
      return try {
        val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
        val mediaType: MediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = MapToJson().map(body).toRequestBody(mediaType)
        val request: Request = Request.Builder().url(Url.webOld()).post(body).build()
        val response: Response = client.newCall(request).execute()
        return when {
          response.isSuccessful -> {
            val input = BufferedReader(InputStreamReader(response.body!!.byteStream()))
            val inputData: String = input.readLine()
            val convertJSON = JSONObject(inputData)
            when {
              convertJSON["Status"] == "1" || convertJSON["Status"] == "2" -> {
                try {
                  JSONObject().put("code", 500).put("data", convertJSON["Pesan"])
                } catch (e: Exception) {
                  JSONObject().put("code", 500).put("data", convertJSON["massage"])
                }
              }
              else -> {
                JSONObject().put("code", 200).put("data", convertJSON)
              }
            }
          }
          else -> {
            JSONObject().put("code", 500).put("data", "Unstable connection / Response Not found")
          }
        }
      } catch (e: Exception) {
        JSONObject().put("code", 500).put("data", e.message)
      }
    }
  }
}