package aleksey.vasiliev.bullfinchmail.view

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.specific.SplashscreenLogic.openProfileOrRegistration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class Splashscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)
        startActivity(openProfileOrRegistration(applicationContext))
    }
}