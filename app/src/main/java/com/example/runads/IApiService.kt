package com.example.runads

import com.example.runads.models.AdsMedia
import com.google.gson.GsonBuilder
import com.example.runads.models.ResPost
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface IApiService {

    @FormUrlEncoded
    @POST("api/v1/human-event")
    fun postHumanEvent(
        @Field("app_id") app_id: String
    ):Call<ResPost>

    companion object
    {
        private var HOST_SERVER = "http://192.168.1.132:8000/"

        private var gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
        var apiService = Retrofit.Builder().baseUrl(HOST_SERVER)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(IApiService::class.java)
        fun changeBaseDomain(domain:String)
        {
            HOST_SERVER = domain
            apiService = Retrofit.Builder().baseUrl(HOST_SERVER)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(IApiService::class.java)
        }
    }

}