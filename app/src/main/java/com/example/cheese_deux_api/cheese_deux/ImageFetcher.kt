package com.example.cheese_deux_api.cheese_deux

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

class ImageFetcher(val context: Context) {
    private val loader = context.imageLoader

    suspend fun getBitmap(imageEndpoint: String, defaultImageRes: Int): Bitmap {
        //assigning default bitmap in case bitmap isn't loaded
        var bitmap = BitmapFactory.decodeResource(context.resources, defaultImageRes)

        //building API request
        val request = ImageRequest.Builder(context = context)
            .data(imageEndpoint)
            .allowHardware(false)
            .build()

        //initialising the drawable to store image in
        var apiDrawable: BitmapDrawable? = null
        val result = loader.execute(request = request)
        if (result is SuccessResult) {
            apiDrawable = result.drawable as BitmapDrawable
        }
        if (apiDrawable != null) {
            bitmap = apiDrawable.bitmap
        }
        return bitmap
    }
}

enum class BitmapType {
    CAT,
    MOUSE,
    OBSTACLE
}