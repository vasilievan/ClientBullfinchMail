package aleksey.vasiliev.bullfinchmail.view

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.Normalizable
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.addAMessageToUI
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.addAllMessagesFromStorage
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.messageTextIsCorrect
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.saveMessage
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.conversation.*

class Conversation: AppCompatActivity(), Normalizable {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val friendsName = intent.getStringExtra("friendsName")
        val friendsLogin = intent.getStringExtra("friendsLogin")!!
        title = friendsName
        setContentView(R.layout.conversation)
        addAllMessagesFromStorage(this, friendsLogin, dialog_content)
        normalizeFont(this, conversation_container)
        message_input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val messageText = message_input.text.toString()
                if (messageTextIsCorrect(messageText)) {
                    addAMessageToUI(this, messageText, dialog_content)
                    saveMessage(friendsLogin, messageText)
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