package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.Constants
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.checkIfConnectionIsAvailable
import aleksey.vasiliev.bullfinchmail.model.specific.FriendsFindingLogic.makeConversationView
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.userNameIsCorrect
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject
import java.io.File
import kotlin.concurrent.thread

object ProfileLogic {
    fun userNameValue(context: Context): String? = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString("userName", null)

    fun createDialog(context: Context, textView: TextView): Boolean {
        val dialog = AlertDialog.Builder(context).create()
        val newUserName = EditText(context)
        var success = false
        newUserName.gravity = 1
        dialog.setTitle("Input new username.")
        dialog.setView(newUserName)
        dialog.setButton(
            DialogInterface.BUTTON_POSITIVE, "Accept", DialogInterface.OnClickListener(
            fun(_: DialogInterface, _: Int) {
                val userName = newUserName.text.toString()
                if (!userNameIsCorrect(context, userName)) return
                if(changeUserNameGlobally(context, userName)) {
                    textView.text = userName
                    with(context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()) {
                        putString("userName", userName)
                        commit()
                    }
                    success = true
                }
            }
        ))
        dialog.show()
        return success
    }

    private fun changeUserNameGlobally(context: Context, userName: String): Boolean {
        if (!checkIfConnectionIsAvailable(context)) return false
        val registrationLogic = RegistrationLogic()
        var nameWasChanged = false
        thread {
            val exchanged = registrationLogic.exchangeKeysWithServer()
            if (exchanged) {
                nameWasChanged = registrationLogic.changeUserName(context, userName)
            }
        }.join()
        return nameWasChanged
    }

    fun addConversationsToLayout(context: Context, container: ViewGroup) {
        val friendsList = File(MAIN_DIR).list()
        if ((friendsList != null) && (friendsList.isNotEmpty())) {
            for (element in friendsList) {
                val extras= File("$MAIN_DIR/$element/extras.json")
                if (extras.exists()) {
                    val jsonObject = JSONObject(extras.readText(DEFAULT_CHARSET))
                    container.addView(makeConversationView(context, jsonObject.getString("friendsUsername")))
                } else {
                    container.addView(makeConversationView(context, element))
                }
            }
        }
    }
}