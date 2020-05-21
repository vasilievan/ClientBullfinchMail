package aleksey.vasiliev.bullfinchmail.model.specific

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DialogAdapter(private val context: Context, private val list: MutableList<Dialog>): RecyclerView.Adapter<DialogsHolder>() {
    private val inflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogsHolder = DialogsHolder(context, inflater, parent)
    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: DialogsHolder, position: Int) {
        val dialog = list[position]
        holder.bind(dialog)
    }
}

