package aleksey.vasiliev.bullfinchmail.view

import android.annotation.SuppressLint
import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.DataBase
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.askForPermissions
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.checkIfConnectionIsAvailable
import aleksey.vasiliev.bullfinchmail.model.general.Normalizable
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.checkLoginAndPassword
import aleksey.vasiliev.bullfinchmail.model.specific.RegistrationLogic.Companion.userNameIsCorrect
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.registration.*
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
        @SuppressLint("StaticFieldLeak")
        val doTask = object: AsyncTask<Unit, Unit, Unit>() {
            var success = false
            override fun doInBackground(vararg params: Unit) {
                val registrationLogic = RegistrationLogic()
                if (registrationLogic.exchangeKeysWithServer())  {
                    success = registrationLogic.signingUp(login, password, userName)
                } else {
                    registrationLogic.closeClientSocket()
                }
            }
            override fun onPostExecute(result: Unit) {
                if (success) {
                    Toast.makeText(applicationContext, REGISTRATION_SUCCESS_PHRASE , Toast.LENGTH_LONG).show()
                    val db = DataBase()
                    db.saveLocalData(applicationContext, login, password, userName)
                    startActivity(Intent(applicationContext, Profile::class.java))
                } else {
                    Toast.makeText(applicationContext, REGISTRATION_WARNING_PHRASE, Toast.LENGTH_LONG).show()
                }
            }
        }
        doTask.execute()
    }
}