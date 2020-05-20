package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.todaysDate
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.io.File
import java.lang.StringBuilder
import aleksey.vasiliev.bullfinchmail.model.general.Constants.ALLOWED_STRING_LENGTH
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DATE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FONT_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.GRAVITY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MESSAGE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MESSAGES

object ConversationLogic {
    fun addNewMessagesToUI(context: Context, dialog_content: ViewGroup, login: String) {
        val already = dialog_content.childCount / 2
        with(File("$MAIN_DIR/$login/$MESSAGES")) {
            val list = list()
            if (list != null && list.isNotEmpty()) {
                val lastOne = list.size
                for (i in already until lastOne) {
                    with (File("$MAIN_DIR/$login/$MESSAGES/$i")) {
                        val message = JSONObject(readText(DEFAULT_CHARSET))
                        dialog_content.addView(makeMessageView(context, message.getString(MESSAGE), message.getInt(GRAVITY)), 0)
                        dialog_content.addView(makeDateView(context, message.getInt(GRAVITY), message.getString(DATE)), 0)
                    }
                }
            }
        }
    }

    fun addAllMessagesFromStorage(context: Context, login: String, container: ViewGroup) {
        with(File("$MAIN_DIR/$login/$MESSAGES")) {
            val messagesList = list()
            if (messagesList == null || messagesList.isEmpty()) return
            messagesList.sortedBy { it.toInt() }.forEach {
                with(File("$MAIN_DIR/$login/$MESSAGES/$it")) {
                    val message = JSONObject(readText(DEFAULT_CHARSET))
                    container.addView(makeMessageView(context, message.getString(MESSAGE), message.getInt(GRAVITY)), 0)
                    container.addView(makeDateView(context, message.getInt(GRAVITY), message.getString(DATE)), 0)
                }
            }
        }
    }

    fun addAMessageToUI(context: Context, message: String, container: ViewGroup, gr: Int) {
        container.addView(makeMessageView(context, message, gr), 0)
        container.addView(makeDateView(context, gr), 0)
    }

    private fun makeMessageView(context: Context, message: String, gr: Int): TextView {
        val messageView = TextView(context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1.0f
        if (gr == 0) {
            params.gravity = Gravity.END
        } else {
            params.gravity = Gravity.START
        }
        params.setMargins(30, 0, 30, 30)
        messageView.layoutParams = params
        messageView.textSize = 20f
        messageView.typeface = Typeface.createFromAsset(context.assets, FONT_NAME)
        messageView.setTextColor(Color.GRAY)
        messageView.text = transformTextForAMessage(message)
        return messageView
    }

    private fun makeDateView(context: Context, gr: Int, date: String = todaysDate()): TextView {
        val dateView = TextView(context)
        dateView.text = date
        dateView.textSize = 15f
        val dateParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dateParams.setMargins(0, 20, 10, 0)
        if (gr == 0) {
            dateParams.gravity = Gravity.END
        } else {
            dateParams.gravity = Gravity.START
        }
        dateView.typeface = Typeface.createFromAsset(context.assets, FONT_NAME)
        dateView.layoutParams = dateParams
        dateView.setTextColor(Color.GRAY)
        return dateView
    }

    private fun transformTextForAMessage(message: String): String {
        val trimmed = message.trim()
        val sb = StringBuilder()
        for (element in trimmed.windowed(ALLOWED_STRING_LENGTH, ALLOWED_STRING_LENGTH)) {
            sb.append(element)
            sb.append("\n")
        }
        val lastSymbols = trimmed.substring(trimmed.length - trimmed.length % ALLOWED_STRING_LENGTH)
        sb.append(lastSymbols)
        return sb.toString()
    }

    fun messageTextIsCorrect(messageText: String): Boolean = messageText.matches(Regex(""".{0,2047}"""))
}