package com.wmontgom.personsrepo.api

import com.wmontgom.personsrepo.sealed.Result
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException


/**
 * Module which provides all required dependencies about network
 */
@Module
object APIHandler {
    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    //OkhttpClient for building http request url
    private val apiClient = OkHttpClient().newBuilder()
        .addInterceptor(logging)
        .build()

    /**
     * Provides the Post service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Post service implementation.
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun providePersonsApi(retrofit: Retrofit): PersonsApi {
        return retrofit.create(PersonsApi::class.java)
    }

    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .client(apiClient)
            .baseUrl("https://randomuser.me/api/1.2/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, errorMessage: String): T? {
        val result: Result<T> = safeApiResult(call, errorMessage)
        var data: T? = null

        when (result) {
            is Result.Success -> {
                data = result.data
            }
            is Result.Error -> {
                Log.d("1.APIRouter", "$errorMessage & Exception - ${result.exception}")
            }
        }

        return data
    }

    private suspend fun <T : Any> safeApiResult(call: suspend () -> Response<T>, errorMessage: String): Result<T> {
        val response = call.invoke()
        if (response.isSuccessful) return Result.Success(response.body()!!)

        return Result.Error(IOException("Error Occurred during getting safe Api result, Custom ERROR - $errorMessage"))
    }
}
