package io.github.mydocomousagewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.text.HtmlCompat

/**
 * Implementation of App Widget functionality.
 */
class PhoneUsageWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            phoneUsageUpdateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null) {
            phoneUsageWidgetUpdate(context)
        }
    }

}

fun phoneUsageUpdateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.phone_usage_widget)
    // 端末使用期間
    MyDocomoPhoneUsage(context) { phone ->
        // テキスト
        val text = """
            ${phone.phoneName}
            機種ご利用期間：${phone.phoneUsage}
            ドコモご利用期間：${phone.carrierUsage}
            機種代金分割払い残り期間：${phone.bunkatu}
            分割払い残額：${phone.bunkatuNokori}
        """.trimIndent()
        views.setTextViewText(R.id.phone_usage, text)
        // アイコンクリックしたら更新する
        val intent = Intent(context, PhoneUsageWidget::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 514, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        views.setOnClickPendingIntent(R.id.phone_usage_widget_imageView, pendingIntent)
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
        // 更新したよ！
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "端末試用期間更新しました", Toast.LENGTH_SHORT).show()
        }
    }.getPhoneUsage()
    // アイコンクリックしたら更新する
    val intent = Intent(context, PhoneUsageWidget::class.java)
    val pendingIntent =
        PendingIntent.getBroadcast(context, 514, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    views.setOnClickPendingIntent(R.id.phone_usage_widget_imageView, pendingIntent)
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

/**
 * ウィジェット更新関数。Contextあれば動くよ
 * @param context Nullの場合動かないです。
 * */
internal fun phoneUsageWidgetUpdate(context: Context?) {
    if (context == null) {
        return
    }
    val componentName = ComponentName(context, PhoneUsageWidget::class.java)
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val idList = appWidgetManager.getAppWidgetIds(componentName)
    for (id in idList) {
        phoneUsageUpdateAppWidget(context, appWidgetManager, id)
    }
}