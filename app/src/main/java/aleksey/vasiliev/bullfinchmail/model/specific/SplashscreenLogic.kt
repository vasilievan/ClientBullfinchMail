package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.Constants.SHARED_PREFERENCES_NAME
import aleksey.vasiliev.bullfinchmail.view.Profile
import aleksey.vasiliev.bullfinchmail.view.Registration
import android.content.Context
import android.content.Intent

object SplashscreenLogic {
    fun openProfileOrRegistration (context: Context): Intent {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean("authorised", false)
        return if (!sharedPreferences) {
            Intent(context, Registration::class.java)
        } else {
            Intent(context, Profile::class.java)
        }
    }
}