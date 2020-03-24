package io.github.mydocomousagewidget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

/**
 * 端末使用期間を返す関数
 * */

private val KEIYAKU_URL = "https://www.nttdocomo.co.jp/mydocomo/procedures/"

private var usePhone = "失敗"

class MyDocomoPhoneUsage(val context: Context?, val response: (PhoneUsage) -> Unit) {
    fun getPhoneUsage() {
        // ご契約内容URL
        val prefSetting = PreferenceManager.getDefaultSharedPreferences(context)
        val cookie = prefSetting.getString("cookie", "") ?: ""
        if (cookie.isEmpty()) {
            Toast.makeText(context, "ログイン情報がありません", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "リクエスト開始", Toast.LENGTH_SHORT).show()
            val webView = WebView(context)
            webView.settings.javaScriptEnabled = true
            //  webView.settings.allowFileAccessFromFileURLs = true
            // My docomoはサードパーティークッキーが有効じゃないとだめ
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            // JS実行
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Toast.makeText(context, "読み込み完了。5秒待機", Toast.LENGTH_SHORT).show()
                    // JSが動的にサイトを構築するので待つ
                    Handler().postDelayed({
                        // 進捗
                        Toast.makeText(context, "JavaScript実行", Toast.LENGTH_SHORT).show()
                        // ご利用機種開く
                        view?.loadUrl("javascript:document.getElementsByClassName('card-sec-tit ico-ac-close')[1].click();")
                        // HTMLをWebViewから取得する
                        view?.loadUrl("javascript:window.MyDocomoPhoneUsage.getHtml(document.documentElement.outerHTML);")
                    }, 5000)
                }
            }
            webView.addJavascriptInterface(this, "MyDocomoPhoneUsage")
            webView.loadUrl(KEIYAKU_URL)

        }
    }

    @JavascriptInterface
    fun getHtml(html: String) {
        // HTMLパース
        println(html)
        val document = Jsoup.parse(html)
        val phone = document.getElementsByClass("device-t-inner")[0]
        usePhone = document.getElementsByClass("device-t-inner")[0].html()
        // 使いやすくする
        val phoneName = phone.getElementById("mydcm_procedure_device-04").text()
        val phoneUsage = phone.getElementsByTag("dd")[0].text()
        val docomoUsage = phone.getElementsByTag("dd")[1].text()
        val bunkatu = phone.getElementsByTag("dd")[2].text()
        val bunkatuNokori = phone.getElementsByTag("dd")[3].text()
        // データクラスへ
        val data = PhoneUsage(phoneName, phoneUsage, docomoUsage, bunkatu, bunkatuNokori)
        response(data)
    }
}

data class PhoneUsage(val phoneName: String, val phoneUsage: String, val carrierUsage: String, val bunkatu: String, val bunkatuNokori: String)
