package aleksey.vasiliev.bullfinchmail.view

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.askForPermissions
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.checkIfConnectionIsAvailable
import aleksey.vasiliev.bullfinchmail.model.general.Normalizable
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.checkLoginAndPassword
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.saveLocalData
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.userNameIsCorrect
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.registration.*
import kotlin.concurrent.thread
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.REGISTRATION_SUCCESS_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.REGISTRATION_WARNING_PHRASE

class Registration : AppCompatActivity(), Normalizable {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
        normalizeFont(this, registration_root)
        askForPermissions(applicationContext, this)
    }

    fun signUp(view: View) {
        val login = registaration_login.text.toString()
        val password = registaration_password.text.toString()
        val userName = registaration_username.text.toString()
        if (checkIfConnectionIsAvailable(applicationContext) &&
            !(checkLoginAndPassword(applicationContext, login, password) &&
                    userNameIsCorrect(applicationContext, userName))) return
        var loginAndPasswordExchangeIndicator = false
        thread {
            val registrationLogic = RegistrationLogic()
            val helper = registrationLogic.exchangeKeysWithServer()
            if (helper) {
                loginAndPasswordExchangeIndicator = registrationLogic.signingUp(login, password, userName)
            }
        }.join()
        if (!loginAndPasswordExchangeIndicator) {
            Toast.makeText(this, REGISTRATION_WARNING_PHRASE, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(this, REGISTRATION_SUCCESS_PHRASE , Toast.LENGTH_LONG).show()
        saveLocalData(this, login, password, userName)
        startActivity(Intent(this, Profile::class.java))
    }
}