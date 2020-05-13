package aleksey.vasiliev.bullfinchmail.model.updates

import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic
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
                val registrationLogic = RegistrationLogic()
                val isEverythingFine = registrationLogic.exchangeKeysWithServer()
                if (isEverythingFine) registrationLogic.checkForFriendRequests(applicationContext)
            }
        }, 0, 5000)
        return super.onStartCommand(intent, flags, startId)
    }
}