package io.github.mydocomousagewidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

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
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
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