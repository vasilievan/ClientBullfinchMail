package aleksey.vasiliev.bullfinchmail.view

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.Normalizable
import aleksey.vasiliev.bullfinchmail.model.specific.ProfileLogic.createDialog
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.loginIsIncorrect
import aleksey.vasiliev.bullfinchmail.model.updates.UpdatesChecker
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.profile.*
import kotlin.concurrent.thread
import aleksey.vasiliev.bullfinchmail.model.general.DataBase
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.userNameValue
import aleksey.vasiliev.bullfinchmail.model.specific.DialogAdapter
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.REQUEST_SENT_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.REQUEST_WARNING_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.UPDATE_VIEW_ACTION

class Profile : AppCompatActivity(), Normalizable {
    private val db = DataBase()
    private val nameList = db.makeUserNameList()
    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)
        val dialogAdapter = DialogAdapter(applicationContext, nameList)
        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                db.updateUserNameList(nameList)
                dialogAdapter.notifyDataSetChanged()
            }
        }
        profile_username.text = userNameValue(this)
        profile_username.setOnLongClickListener {
            createDialog(this, it as TextView)
            true
        }
        normalizeFont(this, profile_container)
        registerReceiver(broadcastReceiver, IntentFilter(UPDATE_VIEW_ACTION))
        profile_friend_list.adapter = dialogAdapter
        startService(Intent(this, UpdatesChecker::class.java))
        find_user.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val userName = find_user.text.toString()
                find_user.text.clear()
                if (!loginIsIncorrect(userName)) {
                    var result = false
                    thread {
                        val registrationLogic = RegistrationLogic()
                        result = registrationLogic.makeFriends(applicationContext, userName)
                    }.join()
                    if (result) {
                        db.updateUserNameList(nameList)
                        dialogAdapter.notifyDataSetChanged()
                        Toast.makeText(applicationContext, REQUEST_SENT_PHRASE, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, REQUEST_WARNING_PHRASE, Toast.LENGTH_LONG).show()
                    }
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, IntentFilter(UPDATE_VIEW_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }
}