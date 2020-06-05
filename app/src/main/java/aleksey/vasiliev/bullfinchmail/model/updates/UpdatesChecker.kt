package aleksey.vasiliev.bullfinchmail.model.updates

import androidx.annotation.RequiresApi
import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.Constants.APP_NAME
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Timer
import java.util.TimerTask
import aleksey.vasiliev.bullfinchmail.model.general.Constants.UPDATE_VIEW_ACTION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.UPDATE_VIEW_CONVERSATION_ACTION
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.UPDATE_PHRASE
import android.annotation.SuppressLint
import android.os.AsyncTask

class UpdatesChecker: Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                var updates = false
                val registrationLogic = RegistrationLogic()
                if (registrationLogic.exchangeKeysWithServer())  {
                    updates = registrationLogic.checkForFriendRequestsAndNewMessages(applicationContext)
                } else {
                    registrationLogic.closeClientSocket()
                }
                if (updates) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createNotificationChannel()
                    }
                    notifyDearUser(UPDATE_PHRASE)
                    applicationContext.sendBroadcast(Intent(UPDATE_VIEW_ACTION))
                    applicationContext.sendBroadcast(Intent(UPDATE_VIEW_CONVERSATION_ACTION))
                }
            }
        }, 0, 5000)
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(name, name, importance).apply {
            description = name
        }
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun notifyDearUser(str: String) {
        val builder = NotificationCompat.Builder(this, getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(APP_NAME)
            .setContentText(str)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify((0..Int.MAX_VALUE).random(), builder.build())
        }
    }
}