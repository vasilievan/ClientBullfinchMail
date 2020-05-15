package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.Constants.ALLOWED_STRING_LENGTH
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.todaysDate
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.io.File
import java.lang.StringBuilder

object ConversationLogic {
    fun addAllMessagesFromStorage(context: Context, login: String, container: ViewGroup) {
        val messagesList = File("$MAIN_DIR/$login/messages").list()
        if (messagesList == null || messagesList.isEmpty()) return
        messagesList.sorted().forEach {
            val message = JSONObject(File("$MAIN_DIR/$login/messages/$it").readText(DEFAULT_CHARSET))
            container.addView(makeMessageView(context, message.getString("message")), 0)
            container.addView(makeDateView(context, message.getString("date")), 0)
        }
    }

    fun addAMessageToUI(context: Context, message: String, container: ViewGroup) {
        container.addView(makeMessageView(context, message), 0)
        container.addView(makeDateView(context), 0)
    }

    private fun makeMessageView(context: Context, message: String): TextView {
        val messageView = TextView(context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1.0f
        params.gravity = Gravity.END
        params.setMargins(20, 0, 20, 20)
        messageView.layoutParams = params
        messageView.textSize = 20f
        messageView.setTextColor(Color.GRAY)
        messageView.text = transformTextForAMessage(message)
        return messageView
    }

    private fun makeDateView(context: Context, date: String = todaysDate()): TextView {
        val dateView = TextView(context)
        dateView.text = date
        dateView.textSize = 15f
        val dateParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dateParams.setMargins(0, 20, 10, 0)
        dateParams.gravity = Gravity.END
        dateView.layoutParams = dateParams
        dateView.setTextColor(Color.GRAY)
        return dateView
    }

    fun saveMessage(login: String, message: String) {
        val jsonObject = JSONObject()
        jsonObject.put("date", todaysDate())
        jsonObject.put("message", message)
        val messages = File("${MAIN_DIR}/$login/messages").list()
        if ((messages != null) && (messages.isNotEmpty())) {
            val currentNum = messages.maxBy { it.toInt() }!!.toInt()
            val myMessage = File("${MAIN_DIR}/$login/messages/${currentNum + 1}")
            myMessage.createNewFile()
            myMessage.writeText(jsonObject.toString(), DEFAULT_CHARSET)
        } else {
            val myMessage = File("${MAIN_DIR}/$login/messages/0")
            myMessage.createNewFile()
            myMessage.writeText(jsonObject.toString(), DEFAULT_CHARSET)
        }
    }

    private fun transformTextForAMessage(message: String): String {
        val trimmed = message.trim()
        val sb = StringBuilder()
        for (element in trimmed.windowed(ALLOWED_STRING_LENGTH, ALLOWED_STRING_LENGTH)) {
            sb.append(element)
            if (!element.endsWith("\n")) {
                sb.append("\n")
            }
        }
        val lastSymbols = trimmed.substring(trimmed.length - trimmed.length % ALLOWED_STRING_LENGTH)
        sb.append(lastSymbols)
        return sb.toString()
    }

    fun messageTextIsCorrect(messageText: String): Boolean = messageText.matches(Regex(""".{0,2047}"""))
}