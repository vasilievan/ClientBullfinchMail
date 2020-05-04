package aleksey.vasiliev.bullfinchmail.view

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.Normalizable
import aleksey.vasiliev.bullfinchmail.model.specific.FriendsFindingLogic.makeConversationView
import aleksey.vasiliev.bullfinchmail.model.specific.FriendsFindingLogic.makeFriends
import aleksey.vasiliev.bullfinchmail.model.specific.ProfileLogic.addConversationsToLayout
import aleksey.vasiliev.bullfinchmail.model.specific.ProfileLogic.createDialog
import aleksey.vasiliev.bullfinchmail.model.specific.ProfileLogic.userNameValue
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.loginIsIncorrect
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.profile.*

class Profile : AppCompatActivity(), Normalizable {
    private val registrationLogic = RegistrationLogic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)
        addConversationsToLayout(this, profile_container)
        normalizeFont(this, profile_container)
        profile_username.text = userNameValue(this)
        profile_username.setOnLongClickListener {
            createDialog(this, it as TextView, registrationLogic)
            true
        }
        find_user.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val userName = find_user.text.toString()
                if (!loginIsIncorrect(userName)) {
                    val result = makeFriends(this, userName, registrationLogic)
                    if (result) {
                        profile_container.addView(makeConversationView(this, userName))
                        Toast.makeText(this, "You sent a friend request.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Either user doesn't exist, or connection is unavailable. Try again.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            true
        }
    }
}