package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.getSharedPreferences
import aleksey.vasiliev.bullfinchmail.view.Profile
import aleksey.vasiliev.bullfinchmail.view.Registration
import android.content.Context
import android.content.Intent
import aleksey.vasiliev.bullfinchmail.model.general.Constants.AUTHORISED

object SplashscreenLogic {
    fun openProfileOrRegistration (context: Context): Intent {
        val authorised = getSharedPreferences(context).getBoolean(AUTHORISED, false)
        return if (!authorised) {
            Intent(context, Registration::class.java)
        } else {
            Intent(context, Profile::class.java)
        }
    }
}