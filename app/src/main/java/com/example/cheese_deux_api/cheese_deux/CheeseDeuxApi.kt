package com.example.cheese_deux_api.cheese_deux

import retrofit2.Response
import retrofit2.http.GET

interface CheeseDeuxApi {

    @GET("/obstacleLimit")
    suspend fun getObstacleLimit() : Response<ObstacleLimit>

}