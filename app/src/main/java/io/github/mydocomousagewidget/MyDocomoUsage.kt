package io.github.mydocomousagewidget

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException


val URL =
    "https://payment2.smt.docomo.ne.jp/smph/charges/gkyap003.srv?Xitraffic=1"

/**
 * 通信量を取得する関数。
 * JSでおしゃれなページだと取得できんから通信量だけ表示されてるサイトへアクセスする
 * あとコルーチンです
 * */
internal fun getMobileDataUsage(context: Context?): Deferred<DataUsage?> =
    GlobalScope.async {
        val prefSetting = PreferenceManager.getDefaultSharedPreferences(context)
        val cookie = prefSetting.getString("cookie", "") ?: ""
        if (cookie.isEmpty()) {
            Toast.makeText(context, "ログイン情報がありません", Toast.LENGTH_SHORT).show()
            return@async null
        } else {
            val request = Request.Builder().apply {
                url(URL)
                header("Cookie", cookie)
                get()
            }.build()
            val okHttpClient = OkHttpClient()
            val response = okHttpClient.newCall(request).execute()
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
                return@async DataUsage(usage, total, usageFloat, totalFloat)
            } else {
                return@async null
            }
        }
    }

data class DataUsage(
    val usage: String,
    val total: String,
    val usageFloat: Float,
    val totalFloat: Float
)
