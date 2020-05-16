package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.Constants.ALLOWED_STRING_LENGTH
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_TRANSFORMATION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PRIVATE_KEY
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.checkIfConnectionIsAvailable
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.createKeyFromJSON
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeString
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
import javax.crypto.Cipher

object ConversationLogic {
    fun addNewMessagesToUI(context: Context, dialog_content: ViewGroup, login: String) {
        val already = dialog_content.childCount / 2
        val dir = File("$MAIN_DIR/$login/messages")
        val list = dir.list()
        if (list != null && list.isNotEmpty()) {
            val lastOne = list.size
            for (i in already until lastOne) {
                val message = JSONObject(File("$MAIN_DIR/$login/messages/$i").readText(DEFAULT_CHARSET))
                dialog_content.addView(makeMessageView(context, message.getString("message"), message.getInt("gr")), 0)
                dialog_content.addView(makeDateView(context, message.getInt("gr"), message.getString("date")), 0)
            }
        }
    }

    fun sendMessageGlobally(context: Context, receiver: String,  messageText: ByteArray, cipheredDate: ByteArray): Boolean {
        if (!checkIfConnectionIsAvailable(context)) {
            return false
        }
        val registrationLogic = RegistrationLogic()
        val exchanged = registrationLogic.exchangeKeysWithServer()
        if (!exchanged) {
            registrationLogic.closeClientSocket()
            return false
        }
        return registrationLogic.sendMessage(context, receiver, messageText, cipheredDate)
    }

    fun addAllMessagesFromStorage(context: Context, login: String, container: ViewGroup) {
        val messagesList = File("$MAIN_DIR/$login/messages").list()
        if (messagesList == null || messagesList.isEmpty()) return
        messagesList.sortedBy { it.toInt() }.forEach {
            val message = JSONObject(File("$MAIN_DIR/$login/messages/$it").readText(DEFAULT_CHARSET))
            container.addView(makeMessageView(context, message.getString("message"), message.getInt("gr")), 0)
            container.addView(makeDateView(context, message.getInt("gr"), message.getString("date")), 0)
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
        messageView.typeface = Typeface.createFromAsset(context.assets, "consolas.ttf")
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
        dateView.typeface = Typeface.createFromAsset(context.assets, "consolas.ttf")
        dateView.layoutParams = dateParams
        dateView.setTextColor(Color.GRAY)
        return dateView
    }

    fun saveMessage(login: String, message: String) {
        val jsonObject = JSONObject()
        jsonObject.put("gr", 0)
        jsonObject.put("date", todaysDate())
        jsonObject.put("message", message)
        val myMessage = File("${MAIN_DIR}/$login/messages/${makeMessageNumber(login)}")
        myMessage.createNewFile()
        myMessage.writeText(jsonObject.toString(), DEFAULT_CHARSET)
    }

    fun saveReceivedMessage(login: String, message: ByteArray, date: ByteArray) {
        val jsonObject = JSONObject()
        jsonObject.put("gr", 1)
        val decipher = Cipher.getInstance(KEY_TRANSFORMATION)
        val key = createKeyFromJSON(login, PRIVATE_KEY) ?: return
        decipher.init(Cipher.DECRYPT_MODE, key)
        jsonObject.put("date", decipher.doFinal(date).makeString())
        jsonObject.put("message", decipher.doFinal(message).makeString())
        val myMessage = File("${MAIN_DIR}/$login/messages/${makeMessageNumber(login)}")
        myMessage.createNewFile()
        myMessage.writeText(jsonObject.toString(), DEFAULT_CHARSET)
    }

    private fun makeMessageNumber(login: String): Int {
        val messages = File("${MAIN_DIR}/$login/messages").list()
        return if ((messages != null) && (messages.isNotEmpty())) {
            val currentNum = messages.maxBy { it.toInt() }!!.toInt()
            currentNum + 1
        } else {
            0
        }
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