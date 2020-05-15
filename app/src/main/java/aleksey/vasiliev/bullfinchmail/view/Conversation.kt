package aleksey.vasiliev.bullfinchmail.view

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_TRANSFORMATION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PUBLIC_KEY
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.createKeyFromJSON
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeByteArray
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.todaysDate
import aleksey.vasiliev.bullfinchmail.model.general.Normalizable
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.addAMessageToUI
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.addAllMessagesFromStorage
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.messageTextIsCorrect
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.saveMessage
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.sendMessageGlobally
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.conversation.*
import javax.crypto.Cipher
import kotlin.concurrent.thread

class Conversation: AppCompatActivity(), Normalizable {

    private val cipher: Cipher =  Cipher.getInstance(KEY_TRANSFORMATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val friendsName = intent.getStringExtra("friendsName")
        val friendsLogin = intent.getStringExtra("friendsLogin")!!
        val publicKey = createKeyFromJSON(friendsLogin, PUBLIC_KEY)
        if (publicKey != null) {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        }
        title = friendsName
        setContentView(R.layout.conversation)
        addAllMessagesFromStorage(this, friendsLogin, dialog_content)
        normalizeFont(this, conversation_container)
        message_input.setOnEditorActionListener { _, actionId, _ ->
            if ((actionId == EditorInfo.IME_ACTION_DONE) && (publicKey != null)) {
                val messageText = message_input.text.toString()
                if (messageTextIsCorrect(messageText)) {
                    val cipheredMessage = cipher.doFinal(messageText.makeByteArray())
                    val cipheredDate = cipher.doFinal(todaysDate().makeByteArray())
                    var result = false
                    thread {
                        result = sendMessageGlobally(this, friendsLogin, cipheredMessage, cipheredDate)
                    }
                    if (result) {
                        addAMessageToUI(this, messageText, dialog_content, 0)
                        saveMessage(friendsLogin, messageText, 0)
                    } else {
                        Toast.makeText(applicationContext, "Due to unknown errors, message wasn't sent.", Toast.LENGTH_LONG).show()
                    }
                    message_input.text.clear()
                }
            } else {
                message_input.text.clear()
            }
            true
        }
        val broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // add to UI
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("UPDATE_VIEW"))
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Profile::class.java))
    }
}