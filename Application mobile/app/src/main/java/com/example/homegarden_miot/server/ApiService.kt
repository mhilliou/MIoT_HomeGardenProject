package com.example.homegarden_miot.server

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("/activePump")
    fun activePump(@Body requestBody: RequestBody): Call<ResponseBody>

    @POST("/pumpMode")
    fun pumpMode(@Body requestBody: RequestBody): Call<ResponseBody>

    @POST("/lightMode")
    fun lightMode(@Body requestBody: RequestBody): Call<ResponseBody>

    @POST("/intensity")
    fun intensity(@Body requestBody: RequestBody): Call<ResponseBody>

    @POST("/scheduledPump")
    fun scheduledPump(@Body requestBody: RequestBody): Call<ResponseBody>

    @POST("/scheduledLight")
    fun scheduledLight(@Body requestBody: RequestBody): Call<ResponseBody>

    @GET("/temperature")
    fun temperature(): Call<ResponseBody>

    @GET("/waterLevelActual")
    fun waterLevel(): Call<ResponseBody>

    @GET("/humidity")
    fun humidity(): Call<ResponseBody>

    @GET("/luminosity")
    fun luminosity(): Call<ResponseBody>

}