package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.view.Profile
import aleksey.vasiliev.bullfinchmail.view.Registration
import android.content.Context
import android.content.Intent
import aleksey.vasiliev.bullfinchmail.model.general.Constants.AUTHORISED
import aleksey.vasiliev.bullfinchmail.model.general.Constants.SHARED_PREFERENCES_NAME

object SplashscreenLogic {
    fun openProfileOrRegistration (context: Context): Intent {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(AUTHORISED, false)
        return if (!sharedPreferences) {
            Intent(context, Registration::class.java)
        } else {
            Intent(context, Profile::class.java)
        }
    }
}