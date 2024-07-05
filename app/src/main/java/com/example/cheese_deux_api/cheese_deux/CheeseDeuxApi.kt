package com.example.cheese_deux_api.cheese_deux

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CheeseDeuxApi {

    @GET("/obstacleLimit")
    suspend fun getObstacleLimit() : Response<ObstacleLimit>

    @POST("/obstacleCourse")
    suspend fun getObstacleList(@Body obstacleCourseBody: ObstacleCourseBody): Response<ObstacleCourse>

    @GET("/hitHindrance")
    suspend fun getHitHindrance(): Response<HitHindrance>
}