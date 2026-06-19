package com.androidbump.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ProfileApi(
    private val client: OkHttpClient = OkHttpClient(),
) {
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    suspend fun createProfile(baseUrl: String, profile: ContactProfile): ProfileResponse =
        withContext(Dispatchers.IO) {
            val body = profile.toJson().toString().toRequestBody(jsonMedia)
            val request = Request.Builder()
                .url("${baseUrl.trimEnd('/')}/api/v1/profiles")
                .post(body)
                .build()
            execute(request)
        }

    suspend fun updateProfile(
        baseUrl: String,
        id: String,
        editToken: String,
        profile: ContactProfile,
    ): ProfileResponse = withContext(Dispatchers.IO) {
        val body = profile.toJson().toString().toRequestBody(jsonMedia)
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/v1/profiles/$id")
            .header("Authorization", "Bearer $editToken")
            .put(body)
            .build()
        execute(request)
    }

    private fun execute(request: Request): ProfileResponse {
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("Backend error ${response.code}: $text")
            }
            val json = JSONObject(text)
            return ProfileResponse(
                id = json.getString("id"),
                editToken = json.getString("editToken"),
                shareUrl = json.getString("shareUrl"),
            )
        }
    }

    private fun ContactProfile.toJson(): JSONObject {
        return JSONObject().apply {
            put("fullName", fullName)
            put("phone", phone)
            put("email", email)
            put("company", company)
            put("website", website)
        }
    }
}
