package com.bebediary.api

import com.bebediary.data.AirQuality
import io.reactivex.Observable
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class AirQualityApi {

    fun getAirQuality(): Observable<List<AirQuality>> {
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
}