package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.R
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.transformTextForAMessage
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FONT_NAME
import android.widget.LinearLayout

class MessagesHolder(val context: Context, inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder(inflater.inflate(
    R.layout.message_pattern, parent, false)) {
    private val date: TextView = itemView.findViewById(R.id.date_message)
    private val messageContent: TextView = itemView.findViewById(R.id.message_content)

    fun bind(message: Message) {
        val messageGravity = if (message.gravity == 0) {
            Gravity.END
        } else {
            Gravity.START
        }

        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = messageGravity
        layoutParams.setMargins(25, 0,  25, 25)

        val typeface = Typeface.createFromAsset(context.assets, FONT_NAME)
        date.text = message.date
        date.layoutParams = layoutParams
        date.typeface = typeface

        messageContent.text = transformTextForAMessage(message.content)
        messageContent.layoutParams = layoutParams
        messageContent.typeface = typeface
    }
}