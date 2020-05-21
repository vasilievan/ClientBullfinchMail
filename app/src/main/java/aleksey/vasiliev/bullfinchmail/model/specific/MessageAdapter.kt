package aleksey.vasiliev.bullfinchmail.model.specific

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val context: Context, private val list: MutableList<Message>): RecyclerView.Adapter<MessagesHolder>() {
    private val inflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesHolder = MessagesHolder(context, inflater, parent)
    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: MessagesHolder, position: Int) {
        val message = list[position]
        holder.bind(message)
    }
}