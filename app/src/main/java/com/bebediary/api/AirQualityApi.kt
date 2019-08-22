package com.bebediary.api

import android.content.Context
import android.util.Log
import com.bebediary.data.AirQuality
import io.reactivex.Observable
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class AirQualityApi(private val context: Context) {

    private val localStorage by lazy { context.getSharedPreferences("air_quality", Context.MODE_PRIVATE) }

    private var response
        get() = localStorage.getString("response", null)
        set(value) = localStorage.edit()
            .putString("response", value)
            .putLong("responseAt", System.currentTimeMillis())
            .apply()

    private val responseAt get() = localStorage.getLong("responseAt", -1L)
    private val now get() = System.currentTimeMillis()

    fun getAirQuality(): Observable<List<AirQuality>> {

        // 이미 받아왔던 정보가 없거나 다시 새로고침 해야할때 네트워크에서 데이터 가져옴
        if (response == null || responseAt == -1L || now - responseAt >= 1000 * 60 * 60 * 6) {
            Log.d("AirQualityApi", "From Network")
            return fromNetwork()
        }

        Log.d("AirQualityApi", "From Cache")
        return fromLocalStorage()
    }

    private fun fromNetwork(): Observable<List<AirQuality>> {
        return Observable.fromPublisher {
            // URL Connection
            var urlConnection: HttpURLConnection? = null

            // 네트워크 통신 후 리스폰스 가져옴
            try {
                val url = URL("http://bebediary.co.kr/app/wair.php")
                urlConnection = url.openConnection() as HttpURLConnection

                val inputStream = BufferedInputStream(urlConnection.getInputStream())
                val content = StringBuilder()
                inputStream.bufferedReader().use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        content.append(line)
                        line = reader.readLine()
                    }
                }

                // 리스폰스 저장
                response = content.toString()

                // JSON Object 파싱
                val obj = JSONObject(content.toString())

                // 데이터 어레이
                val items = arrayListOf<AirQuality>()
                val data = obj.getJSONArray("data")
                for (i in 0 until data.length()) {
                    val airQuality = data.getJSONObject(i)
                    items.add(
                        AirQuality(
                            dataTime = airQuality.getString("dataTime"),
                            sidoName = airQuality.getString("sidoName"),
                            cityName = airQuality.getString("cityName"),
                            pm10 = airQuality.getString("pm10Value").toIntOrNull()
                        )
                    )
                }

                it.onNext(items)
            } catch (e: Exception) {
                it.onError(e)
            } finally {
                // Disconnect URL Connection
                urlConnection?.disconnect()
            }

            it.onComplete()
        }
    }

    private fun fromLocalStorage(): Observable<List<AirQuality>> {
        return Observable.fromPublisher {
            try {
                // JSON Object 파싱
                val obj = JSONObject(response)

                // 데이터 어레이
                val items = arrayListOf<AirQuality>()
                val data = obj.getJSONArray("data")
                for (i in 0 until data.length()) {
                    val airQuality = data.getJSONObject(i)
                    items.add(
                        AirQuality(
                            dataTime = airQuality.getString("dataTime"),
                            sidoName = airQuality.getString("sidoName"),
                            cityName = airQuality.getString("cityName"),
                            pm10 = airQuality.getString("pm10Value").toIntOrNull()
                        )
                    )
                }

                it.onNext(items)
            } catch (e: Exception) {
                it.onError(e)
            }

            it.onComplete()
        }
    }
}