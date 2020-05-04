package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.Constants
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
import java.io.File
import kotlin.concurrent.thread

object ProfileLogic {
    fun userNameValue(context: Context): String? = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString("userName", null)

    fun createDialog(context: Context, textView: TextView, registrationLogic: RegistrationLogic) {
        val dialog = AlertDialog.Builder(context).create()
        val newUserName = EditText(context)
        newUserName.gravity = 1
        dialog.setTitle("Input new username.")
        dialog.setView(newUserName)
        dialog.setButton(
            DialogInterface.BUTTON_POSITIVE, "Accept", DialogInterface.OnClickListener(
            fun(_: DialogInterface, _: Int) {
                val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                val userName = newUserName.text.toString()
                if (!userNameIsCorrect(context, userName)) return
                if(changeUserNameGlobally(context, sharedPreferences, registrationLogic, userName)) {
                    textView.text = userName
                    with(sharedPreferences.edit()) {
                        putString("userName", userName)
                        commit()
                    }
                }
            }
        ))
        dialog.show()
    }

    private fun changeUserNameGlobally(context: Context, sharedPreferences: SharedPreferences, registrationLogic: RegistrationLogic, userName: String): Boolean {
        if (!checkIfConnectionIsAvailable(context)) {
            Toast.makeText(context, "Sorry, connection is anavailable. Try again later.", Toast.LENGTH_LONG).show()
            return false
        }
        val login = sharedPreferences.getString("login", null)
        val password = sharedPreferences.getString("password", null)
        var nameWasChanged = false
        thread {
            val exchanged = registrationLogic.exchangeKeysWithServer()
            if (exchanged) {
                nameWasChanged = registrationLogic.changeUserName(login!!, password!!, userName)
            }
        }.join()
        return nameWasChanged
    }

    fun addConversationsToLayout(context: Context, container: ViewGroup) {
        val friendsList = File(MAIN_DIR).list()
        if ((friendsList != null) && (friendsList.isNotEmpty())) {
            for (element in friendsList) {
                container.addView(makeConversationView(context, element))
            }
        }
    }
}