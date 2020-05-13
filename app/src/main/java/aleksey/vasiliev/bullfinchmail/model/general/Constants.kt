package aleksey.vasiliev.bullfinchmail.model.general

import android.os.Environment

object Constants {
    private val rootDir = Environment.getExternalStorageDirectory()
    val MAIN_DIR = "$rootDir/BullfinchMail"
    const val SHARED_PREFERENCES_NAME = "BullfinchMail"
    const val serverIp = "simsim.ftp.sh"
    val possiblePorts = setOf(4051, 4052, 4053, 4054, 4055, 4056, 4057, 4058, 4059)
    val DEFAULT_CHARSET = Charsets.UTF_8
    const val KEY_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    const val PRIVATE_KEY = "privateKey"
    const val PUBLIC_KEY = "publicKey"
    const val KEY_LENGTH = 1024
    const val EXTENDED_KEY_LENGTH = 2048
    const val KEY_ALGORIGM = "RSA"
}