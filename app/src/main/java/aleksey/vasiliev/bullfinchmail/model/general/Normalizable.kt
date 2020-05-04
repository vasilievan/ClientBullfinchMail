package aleksey.vasiliev.bullfinchmail.model.general

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.iterator

interface Normalizable {
    fun <T: ViewGroup> normalizeFont(context: Context, container: T) {
        val typeface = Typeface.createFromAsset(context.assets, "consolas.ttf")
        for (element in container) {
            when (element) {
                is EditText -> element.typeface = typeface
                is Button -> element.typeface = typeface
                is TextView -> element.typeface = typeface
                is ViewGroup -> normalizeFont(context, element)
            }
        }
    }
}