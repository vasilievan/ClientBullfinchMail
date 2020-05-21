package aleksey.vasiliev.bullfinchmail.model.specific

import java.lang.StringBuilder
import aleksey.vasiliev.bullfinchmail.model.general.Constants.ALLOWED_STRING_LENGTH

object ConversationLogic {
    fun transformTextForAMessage(message: String): String {
        val trimmed = message.trim()
        val sb = StringBuilder()
        for (element in trimmed.windowed(ALLOWED_STRING_LENGTH, ALLOWED_STRING_LENGTH)) {
            sb.append(element)
            sb.append("\n")
        }
        val lastSymbols = trimmed.substring(trimmed.length - trimmed.length % ALLOWED_STRING_LENGTH)
        sb.append(lastSymbols)
        return sb.toString()
    }

    fun messageTextIsCorrect(messageText: String): Boolean = messageText.matches(Regex(""".{0,2047}"""))
}