package com.example.testapp

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.Html
import android.text.SpannableString
import android.util.Log
import com.faendir.rhino_android.RhinoAndroidHelper
import org.mozilla.javascript.Context
import android.app.NotificationManager
import kotlinx.coroutines.*

import androidx.core.app.NotificationCompat

class MyNotificationListenerService : NotificationListenerService() {
    companion object{
        var execContext: android.content.Context? = null
        var lastNotification: StatusBarNotification? = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn != null) {
            lastNotification = sbn
        }

        val packageName: String = sbn?.packageName ?: "Null"
        val extras = sbn?.notification?.extras
        val notificationKey = sbn?.key

        val extraTitle: String = extras?.getString(Notification.EXTRA_TITLE).toString()
        val extraText: String = extras?.get(Notification.EXTRA_TEXT).toString()
        val extraBigText: String = extras?.get(Notification.EXTRA_BIG_TEXT).toString()
        val extraInfoText: String = extras?.get(Notification.EXTRA_INFO_TEXT).toString()
        val extraSubText: String = extras?.get(Notification.EXTRA_SUB_TEXT).toString()
        val extraSummaryText: String = extras?.get(Notification.EXTRA_SUMMARY_TEXT).toString()

//        Log.d(
//            "TAG", "onNotificationPosted:\n"
//                    + "PackageName: $packageName"
//                    + "Title: $extraTitle\n"
//                    + "Text: $extraText\n"
//                        + "BigText: $extraBigText\n"
//                        + "InfoText: $extraInfoText\n"
//                        + "SubText: $extraSubText\n"
//                        + "SummaryText: $extraSummaryText\n"
//        )

        """if (lastNotification?.packageName == "jp.naver.line.android" && notificationKey != null) {
            val wExt = Notification.WearableExtender(sbn?.notification)
            for (act in wExt.actions) {
                if (act.remoteInputs != null && act.remoteInputs.isNotEmpty()) {
                    if (act.title.toString().contains("reply", true) || act.title.toString()
                            .contains("답장", true)
                    ) {
                        execContext = applicationContext
                        //callResponder("2X", extras?.get("android.text"), act, "답장이요~123")
                    }
                }
            }
        }"""

        """println("-----------------------시작-------------------------")
        if (lastNotification?.packageName == "jp.naver.line.android") {
            executeWithDelay(10000) {
                val wExt2 = Notification.WearableExtender(lastNotification?.notification)
                val action = wExt2.actions.firstOrNull { act ->
                    act.remoteInputs != null && act.remoteInputs.isNotEmpty() &&
                            (act.title.toString().contains("reply", true) || act.title.toString()
                                .contains("답장", true))
                }
                if (action != null) {
                    // 액션이 존재하면 실행
                    println("delayed Reply")
                    execContext = applicationContext
                    callResponder(
                        extras?.getString("android.title"),
                        extras?.get("android.text"),
                        action,
                        "테스트답장"
                    )
                }
            }

        }"""
    }

    fun callResponder(room: String?, msg: Any?, session: Notification.Action?, myreply: String){
        val parseContext = RhinoAndroidHelper.prepareContext()
        val sender: String
        val _msg: String
        val replier = SessionCacheReplier(session)

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

    private fun logActiveNotifications() {
        val activeNotifications = getActiveNotifications()
        println("activeNoti : "+ activeNotifications)
        for (notification in activeNotifications) {
            Log.d(
                "ActiveNotification", "Package: ${notification.packageName}, " +
                        "Title: ${notification.notification.extras.getString("android.title")}, " +
                        "Text: ${notification.notification.extras.getString("android.text")}"
            )
        }
    }

    //잠시 시간기다리는 함수
    fun executeWithDelay(delayMillis: Long, task: () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(delayMillis)
            task()
        }
    }

    class SessionCacheReplier (private val session : Notification.Action?){

        fun reply(value: String){
            if (session == null){ return }

            val sendIntent = Intent()
            val msg = Bundle()

            session.remoteInputs?.forEach { inputable -> msg.putCharSequence(inputable.resultKey, value)}

            RemoteInput.addResultsToIntent(session.remoteInputs, sendIntent, msg)

            try {
                session.actionIntent.send(execContext, 0, sendIntent)
            }catch (e:PendingIntent.CanceledException){
                // 예외 처리
            }
        }
    }

}