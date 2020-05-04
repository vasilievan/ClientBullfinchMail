package aleksey.vasiliev.bullfinchmail.view

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.general.Normalizable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.conversation.*

class Conversation: AppCompatActivity(), Normalizable {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.conversation)
        normalizeFont(this, conversation_container)
    }
}