package com.nurshuvo.translateme.network

import com.nurshuvo.translateme.network.responseVO.TranslateResponse
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

private val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
    .baseUrl("https://google-translate1.p.rapidapi.com")
    .build()

interface ApiService {
    @Headers(
        "content-type: application/x-www-form-urlencoded",
        "Accept-Encoding: application/gzip",
        "X-RapidAPI-Key: 881f7cc470mshccd12f04abfa5b5p1b467ejsn482f96509c2c",
        "X-RapidAPI-Host: google-translate1.p.rapidapi.com"
    )
    @POST("language/translate/v2")
    suspend fun translate(@Body body: RequestBody): TranslateResponse
}

object Api {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}