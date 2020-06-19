package aleksey.vasiliev.bullfinchmail.view

import android.annotation.SuppressLint
import android.os.AsyncTask
import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.DataBase
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeByteArray
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.todaysDate
import aleksey.vasiliev.bullfinchmail.model.general.Normalizable
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.messageTextIsCorrect
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
import aleksey.vasiliev.bullfinchmail.model.specific.Message
import aleksey.vasiliev.bullfinchmail.model.specific.MessageAdapter
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.MESSAGE_NOT_SENT_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_LOGIN
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_TRANSFORMATION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PUBLIC_KEY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.UPDATE_VIEW_CONVERSATION_ACTION

class Conversation: AppCompatActivity(), Normalizable {
    private var broadcastReceiver: BroadcastReceiver? = null
    private val cipher: Cipher =  Cipher.getInstance(KEY_TRANSFORMATION)
    private val db = DataBase()
    private val messageList: MutableList<Message> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.conversation)
        normalizeFont(this, conversation_container)

        val friendsName = intent.getStringExtra(FRIENDS_NAME)
        val friendsLogin = intent.getStringExtra(FRIENDS_LOGIN)!!
        db.makeMessageList(friendsLogin)

        val messageAdapter = MessageAdapter(applicationContext, messageList!!)
        messages_list.adapter = messageAdapter

        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                db.updateMessageList(friendsLogin, messageList)
                messageAdapter.notifyDataSetChanged()
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter(UPDATE_VIEW_CONVERSATION_ACTION))
        val db = DataBase()
        val publicKey = db.createKeyFromJSON(friendsLogin, PUBLIC_KEY)
        if (publicKey != null) {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        }
        title = friendsName

        message_input.setOnEditorActionListener { _, actionId, _ ->
            if ((actionId == EditorInfo.IME_ACTION_DONE) && (publicKey != null)) {
                val messageText = message_input.text.toString().trim()
                if (messageTextIsCorrect(messageText)) {
                    val cipheredMessage = cipher.doFinal(messageText.makeByteArray())
                    val cipheredDate = cipher.doFinal(todaysDate().makeByteArray())
                    @SuppressLint("StaticFieldLeak")
                    val doTask = object: AsyncTask<Unit, Unit, Unit>() {
                        var success = false
                        override fun doInBackground(vararg params: Unit) {
                            val registrationLogic = RegistrationLogic()
                            if (registrationLogic.exchangeKeysWithServer())  {
                                success = registrationLogic.sendMessageGlobally(applicationContext, friendsLogin, cipheredMessage, cipheredDate)
                            } else {
                                registrationLogic.closeClientSocket()
                            }
                        }
                        override fun onPostExecute(result: Unit) {
                            if (success) {
                                message_input.text.clear()
                                db.saveMessage(friendsLogin, messageText)
                                db.updateMessageList(friendsLogin, messageList)
                                messageAdapter.notifyDataSetChanged()
                            } else {
                                Toast.makeText(applicationContext, MESSAGE_NOT_SENT_PHRASE, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    doTask.execute()
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
