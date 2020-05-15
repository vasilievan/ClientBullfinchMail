package aleksey.vasiliev.bullfinchmail.view

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_TRANSFORMATION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
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
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.conversation.*
import javax.crypto.Cipher

class Conversation: AppCompatActivity(), Normalizable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val friendsName = intent.getStringExtra("friendsName")
        val friendsLogin = intent.getStringExtra("friendsLogin")!!
        val publicKey = createKeyFromJSON(friendsLogin, PUBLIC_KEY)
        val cipher =  Cipher.getInstance(KEY_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        title = friendsName
        setContentView(R.layout.conversation)
        addAllMessagesFromStorage(this, friendsLogin, dialog_content)
        normalizeFont(this, conversation_container)
        message_input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val messageText = message_input.text.toString()
                if (messageTextIsCorrect(messageText)) {
                    addAMessageToUI(this, messageText, dialog_content, 0)
                    val cipheredMessage = cipher.doFinal(messageText.makeByteArray())
                    val cipheredDate = cipher.doFinal(todaysDate().makeByteArray())
                    sendMessageGlobally(this, friendsLogin, cipheredMessage, cipheredDate)
                    saveMessage(friendsLogin, messageText, 0)
                    message_input.text.clear()
                }
            }
            true
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Profile::class.java))
    }
}