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

/**
 * Implementation of App Widget functionality.
 */
class MobileDataUsageWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
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
            widgetUpdate(context)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.mobile_data_usage_widget)
    // データ取得
    getMobileDataUsage(context) {
        val usage = it.usage
        val total = it.total
        val usageInt = (it.usageFloat * 100).toInt()
        val totalInt = (it.totalFloat * 100).toInt()
        views.setTextViewText(R.id.widget_usage, "使用済み：$usage")
        views.setTextViewText(R.id.widget_total, "利用可能：$total")
        views.setProgressBar(R.id.progressBar, totalInt, usageInt, false)
        // アイコンクリックしたら更新する
        val intent = Intent(context, MobileDataUsageWidget::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 2525, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        views.setOnClickPendingIntent(R.id.widget_imageView, pendingIntent)
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
        // 更新したよ！
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "通信量更新しました", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * ウィジェット更新関数。Contextあれば動くよ
 * @param context Nullの場合動かないです。
 * */
internal fun widgetUpdate(context: Context?) {
    if (context == null) {
        return
    }
    val componentName = ComponentName(context, MobileDataUsageWidget::class.java)
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val idList = appWidgetManager.getAppWidgetIds(componentName)
    for (id in idList) {
        updateAppWidget(context, appWidgetManager, id)
    }
}