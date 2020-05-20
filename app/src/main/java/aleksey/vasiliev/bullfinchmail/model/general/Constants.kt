package aleksey.vasiliev.bullfinchmail.model.general

import android.os.Environment

object Constants {
    private val ROOT_DIR = Environment.getExternalStorageDirectory()
    val MAIN_DIR = "$ROOT_DIR/BullfinchMail"
    const val SHARED_PREFERENCES_NAME = "BullfinchMail"
    const val SERVER_IP = "simsim.ftp.sh"
    val POSSIBLE_PORTS = setOf(4051, 4052, 4053, 4054, 4055, 4056, 4057, 4058, 4059)
    val DEFAULT_CHARSET = Charsets.UTF_8
    const val KEY_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    const val PRIVATE_KEY = "privateKey"
    const val PUBLIC_KEY = "publicKey"
    const val EXTENDED_KEY_LENGTH = 2048
    const val KEY_ALGORIGM = "RSA"
    const val ALLOWED_STRING_LENGTH = 30
    const val BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    const val TEN_MINUTES = 1000 * 60 * 10L
    const val FONT_NAME = "consolas.ttf"
    const val UPDATE_VIEW_ACTION = "UPDATE_VIEW"
    const val UPDATE_VIEW_CONVERSATION_ACTION = "UPDATE_VIEW_CONVERSATION"
    const val FRIENDS_NAME = "friendsName"
    const val FRIENDS_USERNAME = "friendsUsername"
    const val GRAVITY = "gr"
    const val DATE = "date"
    const val MESSAGE = "message"
    const val FRIENDS_LOGIN = "friendsLogin"
    const val AUTHORISED = "authorised"
    const val LOGIN = "login"
    const val PASSWORD = "password"
    const val USERNAME = "userName"
    const val MESSAGES = "messages"
    const val JSON_FORMAT = ".json"
    const val EXTRAS = "extras"
    const val RIGHT_GRAVITY = 0
    const val LEFT_GRAVITY = 1
    const val APP_BUFFER_SIZE = 8198
}