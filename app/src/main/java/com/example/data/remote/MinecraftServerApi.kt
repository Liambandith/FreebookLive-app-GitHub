package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class McStatusResponse(
    @Json(name = "online") val online: Boolean = false,
    @Json(name = "host") val host: String? = null,
    @Json(name = "port") val port: Int? = null,
    @Json(name = "version") val version: McVersion? = null,
    @Json(name = "players") val players: McPlayers? = null,
    @Json(name = "motd") val motd: McMotd? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "eula_blocked") val eulaBlocked: Boolean? = false
)

data class McVersion(
    @Json(name = "name_clean") val nameClean: String? = null,
    @Json(name = "protocol") val protocol: Int? = null
)

data class McPlayers(
    @Json(name = "online") val online: Int = 0,
    @Json(name = "max") val max: Int = 0,
    @Json(name = "list") val list: List<McPlayerItem>? = null
)

data class McPlayerItem(
    @Json(name = "uuid") val uuid: String? = null,
    @Json(name = "name_clean") val nameClean: String? = null
)

data class McMotd(
    @Json(name = "clean") val clean: String? = null,
    @Json(name = "raw") val raw: String? = null
)

interface MinecraftServerApi {
    @GET("v2/status/java/{address}")
    suspend fun getJavaServerStatus(@Path("address") address: String): McStatusResponse

    @GET("v2/status/bedrock/{address}")
    suspend fun getBedrockServerStatus(@Path("address") address: String): McStatusResponse

    companion object {
        private const val BASE_URL = "https://api.mcstatus.io/"

        fun create(): MinecraftServerApi {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(6, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(MinecraftServerApi::class.java)
        }
    }
}
