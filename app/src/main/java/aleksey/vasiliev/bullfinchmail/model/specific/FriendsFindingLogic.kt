package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.view.Conversation
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FONT_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_LOGIN
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_NAME

object FriendsFindingLogic {
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