package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.specific.FriendsFindingLogic.makeConversationView
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.userNameIsCorrect
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import org.json.JSONObject
import java.io.File
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.Constants.EXTRAS
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_USERNAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.JSON_FORMAT
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.Constants.USERNAME
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.getSharedPreferences
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.ACCEPT_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.NEW_USERNAME_PHRASE

object ProfileLogic {
    fun userNameValue(context: Context): String? = getSharedPreferences(context).getString(USERNAME, null)

    fun createDialog(context: Context, textView: TextView): Boolean {
        val dialog = AlertDialog.Builder(context).create()
        val newUserName = EditText(context)
        var success = false
        newUserName.gravity = 1
        dialog.setTitle(NEW_USERNAME_PHRASE)
        dialog.setView(newUserName)
        dialog.setButton(
            DialogInterface.BUTTON_POSITIVE, ACCEPT_PHRASE, DialogInterface.OnClickListener(
            fun(_: DialogInterface, _: Int) {
                val userName = newUserName.text.toString()
                if (!userNameIsCorrect(context, userName)) return
                val registrationLogic = RegistrationLogic()
                if(registrationLogic.changeUserNameGlobally(context, userName)) {
                    textView.text = userName
                    with(getSharedPreferences(context).edit()) {
                        putString(USERNAME, userName)
                        commit()
                    }
                    success = true
                }
            }
        ))
        dialog.show()
        return success
    }

    fun addConversationsToLayout(context: Context, container: ViewGroup) {
        val friendsList = File(MAIN_DIR).list()
        if ((friendsList != null) && (friendsList.isNotEmpty())) {
            for (element in friendsList) {
                with(File("$MAIN_DIR/$element/$EXTRAS$JSON_FORMAT")) {
                    if (exists()) {
                        val jsonObject = JSONObject(readText(DEFAULT_CHARSET))
                        container.addView(makeConversationView(context, element, jsonObject.getString(FRIENDS_USERNAME)))
                    } else {
                        container.addView(makeConversationView(context, element))
                    }
                }
            }
        }
    }

    fun addNewConversationsToLayout(context: Context, container: ViewGroup) {
        val alreadyPresented = mutableSetOf<String>()
        for (element in container.children) {
            if (element is TextView) {
                alreadyPresented.add(element.text.toString())
            }
        }
        with(File(MAIN_DIR)) {
            val friendsList = list()
            if ((friendsList != null) && (friendsList.isNotEmpty())) {
                for (element in friendsList.toSet().subtract(alreadyPresented)) {
                    with(File("$MAIN_DIR/$element/$EXTRAS$JSON_FORMAT")) {
                        if (exists()) {
                            val jsonObject = JSONObject(readText(DEFAULT_CHARSET))
                            val localUserName = jsonObject.getString(FRIENDS_USERNAME)
                            container.addView(makeConversationView(context, element, localUserName))
                        } else {
                            container.addView(makeConversationView(context, element))
                        }
                    }
                }
            }
        }
    }
}