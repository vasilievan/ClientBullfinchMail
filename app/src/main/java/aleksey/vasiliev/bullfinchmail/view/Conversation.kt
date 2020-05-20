package aleksey.vasiliev.bullfinchmail.view

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.DataBase
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeByteArray
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.todaysDate
import aleksey.vasiliev.bullfinchmail.model.general.Normalizable
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.addAMessageToUI
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.addAllMessagesFromStorage
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.addNewMessagesToUI
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.messageTextIsCorrect
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
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.MESSAGE_NOT_SENT_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_LOGIN
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_TRANSFORMATION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PUBLIC_KEY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.UPDATE_VIEW_CONVERSATION_ACTION


class Conversation: AppCompatActivity(), Normalizable {
    private var broadcastReceiver: BroadcastReceiver? = null
    private val cipher: Cipher =  Cipher.getInstance(KEY_TRANSFORMATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val friendsName = intent.getStringExtra(FRIENDS_NAME)
        val friendsLogin = intent.getStringExtra(FRIENDS_LOGIN)!!
        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                addNewMessagesToUI(applicationContext, dialog_content, friendsLogin)
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter(UPDATE_VIEW_CONVERSATION_ACTION))
        val db = DataBase()
        val publicKey = db.createKeyFromJSON(friendsLogin, PUBLIC_KEY)
        if (publicKey != null) {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        }
        title = friendsName
        setContentView(R.layout.conversation)
        addAllMessagesFromStorage(this, friendsLogin, dialog_content)
        normalizeFont(this, conversation_container)
        message_input.setOnEditorActionListener { _, actionId, _ ->
            if ((actionId == EditorInfo.IME_ACTION_DONE) && (publicKey != null)) {
                val messageText = message_input.text.toString().trim()
                if (messageTextIsCorrect(messageText)) {
                    val cipheredMessage = cipher.doFinal(messageText.makeByteArray())
                    val cipheredDate = cipher.doFinal(todaysDate().makeByteArray())
                    var result = false
                    thread {
                        result = sendMessageGlobally(this, friendsLogin, cipheredMessage, cipheredDate)
                    }.join()
                    if (result) {
                        addAMessageToUI(this, messageText, dialog_content, 0)
                        db.saveMessage(friendsLogin, messageText)
                    } else {
                        Toast.makeText(applicationContext, MESSAGE_NOT_SENT_PHRASE, Toast.LENGTH_LONG).show()
                    }
                    message_input.text.clear()
                }
            } else {
                message_input.text.clear()
            }
            true
        }
    }

    override fun onBackPressed() {
        unregisterReceiver(broadcastReceiver)
        startActivity(Intent(this, Profile::class.java))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, IntentFilter(UPDATE_VIEW_CONVERSATION_ACTION))
    }
}