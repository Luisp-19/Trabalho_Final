package com.example.viana_xplore_reset.Webservices

import com.example.viana_xplore_reset.Webservices.Markador
import com.example.viana_xplore_reset.Webservices.Output_Login
import com.example.viana_xplore_reset.Marcadores
import retrofit2.Call
import retrofit2.http.*

interface PostLogin {

    @FormUrlEncoded
    @POST("/myslim/api/login/entra")
    fun postTest(@Field("utilizador") utilizador: String, @Field("palavrapasse") palavrapasse: String): Call<Output_Login>

    @FormUrlEncoded
    @POST("myslim/api/login/criar")
    fun postcriar(@Field("utilizador") utilizador: String, @Field("palavrapasse") palavrapasse: String): Call<Output_Login>

    @GET("/myslim/api/marcadores")
    fun getMarcadores(): Call<List<Markador>>

    @GET("/myslim/api/select/{id}")
    fun getMarcadoresID(@Path("id") id: Int): Call<List<Markador>>

}