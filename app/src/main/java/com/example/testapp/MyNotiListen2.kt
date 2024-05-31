package com.example.testapp

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import com.faendir.rhino_android.RhinoAndroidHelper
import org.mozilla.javascript.Context

class MyNotiListen2 : NotificationListenerService() {

    companion object {
        var execContext: android.content.Context? = null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        println("listenerConnected")
        //logActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        println("MyNotiListen2 시작 ")
        logActiveNotifications()
        println("MyNotiListen2 끝 ")
    }

    private fun logActiveNotifications() {
        val activeNotifications = getActiveNotifications()
        for (notification in activeNotifications) {
            if (notification.packageName == "jp.naver.line.android") {
                println("1차 통과")
                val wExt = Notification.WearableExtender(notification?.notification)
                val action = wExt.actions.firstOrNull(){act ->
                    act.remoteInputs != null && act.remoteInputs.isNotEmpty() &&
                            (act.title.toString().contains("reply", true) || act.title.toString()
                                .contains("답장", true))
                }
                println("wEXT, action : " + wExt + " / " + action)
                if (action != null){
                    println("2차통과")
                    execContext = applicationContext
                    callResponder(notification?.notification?.extras?.getString("android.title"),
                        notification?.notification?.extras?.getString("android.text"),
                        action,
                        "지금답장")
                }
                Log.d(
                    "ActiveNotification", "Package: ${notification.packageName}, " +
                            "Title: ${notification.notification.extras.getString("android.title")}, " +
                            "Text: ${notification.notification.extras.getString("android.text")}"
                )
            }
        }
    }

    fun callResponder(room: String?, msg: Any?, session: Notification.Action?, myreply: String){
        val parseContext = RhinoAndroidHelper.prepareContext()
        val sender: String
        val _msg: String
        val replier = MyNotiListen2.SessionCacheReplier(session)

        parseContext.optimizationLevel = -1

        if (msg is String){
            sender = room?: "Unknown"
            _msg = msg
        }
        else{
            val html = Html.toHtml(msg as SpannableString)
            sender = Html.fromHtml(html.split("<b>")[1].split("</b>")[0]).toString()
            _msg = Html.fromHtml(html.split("</b>")[1].split("</p>")[0].substring(1)).toString()
        }

        replier.reply(myreply)

        Context.exit()
    }
    class SessionCacheReplier (private val session : Notification.Action?){

        fun reply(value: String){
            if (session == null){ return }

            val sendIntent = Intent()
            val msg = Bundle()

            session.remoteInputs?.forEach { inputable -> msg.putCharSequence(inputable.resultKey, value)}

            RemoteInput.addResultsToIntent(session.remoteInputs, sendIntent, msg)

            try {
                session.actionIntent.send(MyNotiListen2.execContext, 0, sendIntent)
            }catch (e:PendingIntent.CanceledException){
                // 예외 처리
            }
        }
    }
}

