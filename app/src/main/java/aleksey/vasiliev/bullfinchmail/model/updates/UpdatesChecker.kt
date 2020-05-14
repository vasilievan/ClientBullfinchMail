package aleksey.vasiliev.bullfinchmail.model.updates

import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic
import aleksey.vasiliev.bullfinchmail.view.Conversation
import aleksey.vasiliev.bullfinchmail.view.Profile
import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.Timer
import java.util.TimerTask

class UpdatesChecker: Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                var requests = false
                //var messages = false
                val registrationLogic = RegistrationLogic()
                val isEverythingFine = registrationLogic.exchangeKeysWithServer()
                if (isEverythingFine) {
                    requests = registrationLogic.checkForFriendRequests(applicationContext)
                    //messages = registrationLogic.checkForNewMessages(applicationContext)
                }
                if (requests) {
                    applicationContext.sendBroadcast(Intent("UPDATE_VIEW"))
                }
                //if (messages) Intent(applicationContext, Conversation::class.java)
            }
        }, 0, 5000)
        return super.onStartCommand(intent, flags, startId)
    }
}