package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.userNameIsCorrect
import android.content.Context
import android.content.DialogInterface
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.getSharedPreferences
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.ACCEPT_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.NEW_USERNAME_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.USERNAME

object ProfileLogic {
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
                val exchanged = registrationLogic.exchangeKeysWithServer()
                if (exchanged) {
                    if(registrationLogic.changeUserNameGlobally(context, userName)) {
                        textView.text = userName
                        with(getSharedPreferences(context).edit()) {
                            putString(USERNAME, userName)
                            commit()
                        }
                        success = true
                    }
                } else {
                    registrationLogic.closeClientSocket()
                }
            }
        ))
        dialog.show()
        return success
    }
}