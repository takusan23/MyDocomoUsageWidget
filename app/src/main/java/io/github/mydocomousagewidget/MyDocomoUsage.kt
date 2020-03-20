package io.github.mydocomousagewidget

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException


val URL =
    "https://payment2.smt.docomo.ne.jp/smph/charges/gkyap003.srv?Xitraffic=1"

/**
 * 通信量を取得する関数。
 * JSでおしゃれなページだと取得できんから通信量だけ表示されてるサイトへアクセスする
 * */
internal fun getMobileDataUsage(context: Context?, response: (DataUsage) -> Unit) {
    val prefSetting = PreferenceManager.getDefaultSharedPreferences(context)
    val cookie = prefSetting.getString("cookie", "") ?: ""
    if (cookie.isEmpty()) {
        Toast.makeText(context, "ログイン情報がありません", Toast.LENGTH_SHORT).show()
    } else {
        val request = Request.Builder().apply {
            url(URL)
            header("Cookie", cookie)
            get()
        }.build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseString = response.body?.string()
                    val html = Jsoup.parse(responseString)
                    // データ量
                    val usage = html.getElementsByClass("arrange-r bold mb_5")[0].text()
                    val usageFloat =
                        html.getElementsByClass("arrange-r bold mb_5")[0].text().replace("GB", "")
                            .toFloat()
                    // トータル
                    val total = html.getElementsByClass("arrange-r bold mb_5")[8].text()
                    val totalFloat =
                        html.getElementsByClass("arrange-r bold mb_5")[8].text().replace("GB", "")
                            .toFloat()
                    response(DataUsage(usage, total, usageFloat, totalFloat))
                }
            }
        })
    }
}

data class DataUsage(
    val usage: String,
    val total: String,
    val usageFloat: Float,
    val totalFloat: Float
)
