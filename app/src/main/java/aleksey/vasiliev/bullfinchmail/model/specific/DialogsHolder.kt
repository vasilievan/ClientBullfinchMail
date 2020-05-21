package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.view.Conversation
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.File
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.Constants.EXTRAS
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FONT_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_LOGIN
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_USERNAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.JSON_FORMAT
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR

class DialogsHolder(val context: Context, inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder(inflater.inflate(R.layout.dialog_pattern, parent, false)) {
    private val userName: TextView = itemView.findViewById(R.id.dialog_name)

    fun bind(dialog: Dialog) {
        userName.typeface = Typeface.createFromAsset(context.assets, FONT_NAME)
        val usernameExtra = with(File("$MAIN_DIR/${dialog.userName}/$EXTRAS$JSON_FORMAT")) {
            if (exists()) {
                val jsonObject = JSONObject(readText(DEFAULT_CHARSET))
                jsonObject.getString(FRIENDS_USERNAME)
            } else {
                dialog.userName
            }
        }
        userName.text = usernameExtra
        userName.setOnClickListener {
            val intent = Intent(context, Conversation::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(FRIENDS_LOGIN, dialog.userName)
            intent.putExtra(FRIENDS_NAME, usernameExtra)
            context.startActivity(intent)
        }
    }
}