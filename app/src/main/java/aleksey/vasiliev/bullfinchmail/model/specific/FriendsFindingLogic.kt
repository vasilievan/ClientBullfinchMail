package aleksey.vasiliev.bullfinchmail.model.specific

import android.content.Context.MODE_PRIVATE
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.checkIfConnectionIsAvailable
import aleksey.vasiliev.bullfinchmail.view.Conversation
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FONT_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_LOGIN
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.LOGIN
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PASSWORD
import aleksey.vasiliev.bullfinchmail.model.general.Constants.SHARED_PREFERENCES_NAME

object FriendsFindingLogic {
    fun makeFriends(context: Context, userName: String): Boolean {
        if (!checkIfConnectionIsAvailable(context)) return false
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val login = sharedPreferences.getString(LOGIN, null)
        if (userName == login) return false
        val alreadyBefriended = File(MAIN_DIR).list() ?: Array(0){""}
        if (userName in alreadyBefriended) return false
        val password = sharedPreferences.getString(PASSWORD, null)
        val registrationLogic = RegistrationLogic()
        val exchanged = registrationLogic.exchangeKeysWithServer()
        if (!exchanged) return false
        return registrationLogic.sendRequest(login!!, password!!, userName)
    }

    fun makeConversationView(context: Context, login: String, userName: String = login): TextView {
        val typeface = Typeface.createFromAsset(context.assets, FONT_NAME)
        val view = TextView(context)
        view.text = userName
        view.textSize = 20f
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(25, 25, 25, 25)
        params.gravity = 1
        view.layoutParams = params
        view.setTextColor(Color.GRAY)
        view.typeface = typeface
        view.setOnClickListener {
            val intent = Intent(context, Conversation::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(FRIENDS_LOGIN, login)
            intent.putExtra(FRIENDS_NAME, userName)
            context.startActivity(intent)
        }
        return view
    }
}