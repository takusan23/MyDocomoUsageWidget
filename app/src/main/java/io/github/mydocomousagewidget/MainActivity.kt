package io.github.mydocomousagewidget

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val MY_DOCOMO_URL = "https://www.nttdocomo.co.jp/mydocomo/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // WebViewでログインさせる
        webview.webViewClient = WebViewClient()
        webview.settings.javaScriptEnabled = true
        webview.loadUrl(MY_DOCOMO_URL)
        // My docomoはサードパーティークッキーが有効じゃないとだめ
        CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true)

        // ログインできたらCookieを保存する。
        val prefSetting = PreferenceManager.getDefaultSharedPreferences(this)
        cookie_save_button.setOnClickListener {
            // クッキー取得
            prefSetting.edit {
                val cookies = CookieManager.getInstance().getCookie(MY_DOCOMO_URL)
                putString("cookie", cookies)
            }
            // Widget更新
            widgetUpdate(this)
            phoneUsageWidgetUpdate(this)
        }
    }
}
