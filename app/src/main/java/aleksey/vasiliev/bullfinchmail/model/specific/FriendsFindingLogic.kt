package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.Constants.SHARED_PREFERENCES_NAME
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.checkIfConnectionIsAvailable
import aleksey.vasiliev.bullfinchmail.view.Conversation
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Typeface
import android.widget.TextView
import android.widget.Toast
import java.io.File
import kotlin.concurrent.thread

object FriendsFindingLogic {
    fun makeFriends(context: Context, userName: String, registrationLogic: RegistrationLogic): Boolean {
        if (!checkIfConnectionIsAvailable(context)) {
            Toast.makeText(context, "Sorry, connection is anavailable. Try again later.", Toast.LENGTH_LONG).show()
            return false
        }
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val login = sharedPreferences.getString("login", null)
        if (userName == login) {
            Toast.makeText(context, "You can't make friends with yourself.", Toast.LENGTH_LONG).show()
            return false
        }
        if (userName in (File(MAIN_DIR).list() ?: return false)) {
            Toast.makeText(context, "You already sent request to this user or he is you friend.", Toast.LENGTH_LONG).show()
            return false
        }
        val password = sharedPreferences.getString("password", null)
        var friendRequestWasSent = false
        thread {
            val exchanged = registrationLogic.exchangeKeysWithServer()
            if (exchanged) {
                friendRequestWasSent = registrationLogic.sendRequest(login!!, password!!, userName)
            }
        }.join()
        return friendRequestWasSent
    }

    fun makeConversationView(context: Context, userName: String): TextView {
        val typeface = Typeface.createFromAsset(context.assets, "consolas.ttf")
        val view = TextView(context)
        view.text = userName
        view.gravity = 1
        view.textSize = 20f
        view.typeface = typeface
        view.setOnClickListener {
            val intent = Intent(context, Conversation::class.java)
            intent.putExtra("friendsName", userName)
            context.startActivity(intent)
        }
        return view
    }
}