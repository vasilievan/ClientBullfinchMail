package aleksey.vasiliev.bullfinchmail.model.general

import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeString
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.todaysDate
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.Key
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import aleksey.vasiliev.bullfinchmail.model.general.Constants.AUTHORISED
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DATE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.Constants.ENCRYPTED_SP_KEY_SCHEME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.ENCRYPTED_SP_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.ENCRYPTED_SP_VALUE_SCHEME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.ENCYPTED_SP_KEY_SPECIFICATION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.EXTENDED_KEY_LENGTH
import aleksey.vasiliev.bullfinchmail.model.general.Constants.EXTRAS
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_USERNAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.GRAVITY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.JSON_FORMAT
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_ALGORIGM
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_TRANSFORMATION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.LEFT_GRAVITY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.LOGIN
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MESSAGE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MESSAGES
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PASSWORD
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PRIVATE_KEY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PUBLIC_KEY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.RIGHT_GRAVITY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.USERNAME

class DataBase {
    fun saveKey(friendsLogin: String, passwordType: String, key: ByteArray) {
        with(File("${MAIN_DIR}/$friendsLogin/$MESSAGES")) {
            mkdirs()
        }
        with(File("${MAIN_DIR}/$friendsLogin/$passwordType$JSON_FORMAT")) {
            if (!this.exists()) createNewFile()
            writeText(createJSONKey(key, passwordType), DEFAULT_CHARSET)
        }
    }

    fun makeMessageNumber(login: String): Int {
        with(File("$MAIN_DIR/$login/$MESSAGES")) {
            val messages = list()
            return if ((messages != null) && (messages.isNotEmpty())) {
                val currentNum = messages.maxBy { it.toInt() }!!.toInt()
                currentNum + 1
            } else {
                0
            }
        }
    }

    fun createKeyFromJSON(login: String, keyType: String): Key? {
        with(File("${MAIN_DIR}/$login/$keyType$JSON_FORMAT")) {
            if (!this.exists()) return null
            val jsonObject = JSONObject(readText(DEFAULT_CHARSET))
            val jsonArray = jsonObject.getJSONArray(keyType)
            val byteArray = ByteArray(EXTENDED_KEY_LENGTH)
            for (i in 0 until jsonArray.length()) {
                byteArray[i] = jsonArray.getString(i).toByte()
            }
            return if (keyType == PUBLIC_KEY) {
                KeyFactory.getInstance(KEY_ALGORIGM).generatePublic(X509EncodedKeySpec(byteArray))
            } else {
                KeyFactory.getInstance(KEY_ALGORIGM).generatePrivate(PKCS8EncodedKeySpec(byteArray))
            }
        }
    }

    private fun createJSONKey(key: ByteArray, passwordType: String): String {
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()
        for (element in key) {
            jsonArray.put(element)
        }
        jsonObject.put(passwordType, jsonArray)
        return jsonObject.toString()
    }

    fun saveExtras(friendsLogin: String, friendsUsername: String) {
        with (File("${MAIN_DIR}/$friendsLogin/$EXTRAS$JSON_FORMAT")) {
            if (!exists()) createNewFile()
            val jsonObject = JSONObject()
            jsonObject.put(FRIENDS_USERNAME, friendsUsername)
            writeText(jsonObject.toString(), DEFAULT_CHARSET)
        }
    }

    fun saveMessage(login: String, message: String) {
        val jsonObject = JSONObject()
        jsonObject.put(GRAVITY, RIGHT_GRAVITY)
        jsonObject.put(DATE, todaysDate())
        jsonObject.put(MESSAGE, message)
        with(File("${MAIN_DIR}/$login/$MESSAGES/${makeMessageNumber(login)}")) {
            createNewFile()
            writeText(jsonObject.toString(), DEFAULT_CHARSET)
        }
    }

    fun saveReceivedMessage(login: String, message: ByteArray, date: ByteArray) {
        val jsonObject = JSONObject()
        jsonObject.put(GRAVITY, LEFT_GRAVITY)
        val decipher = Cipher.getInstance(KEY_TRANSFORMATION)
        val key = createKeyFromJSON(login, PRIVATE_KEY) ?: return
        decipher.init(Cipher.DECRYPT_MODE, key)
        jsonObject.put(DATE, decipher.doFinal(date).makeString())
        jsonObject.put(MESSAGE, decipher.doFinal(message).makeString())
        with(File("${MAIN_DIR}/$login/$MESSAGES/${makeMessageNumber(login)}")) {
            createNewFile()
            writeText(jsonObject.toString(), DEFAULT_CHARSET)
        }
    }

    fun saveLocalData (context: Context, login: String, password: String, userName:String) {
        val masterKeyAlias = MasterKeys.getOrCreate(ENCYPTED_SP_KEY_SPECIFICATION)
        val sharedPreferences = EncryptedSharedPreferences.create(
            ENCRYPTED_SP_NAME,
            masterKeyAlias,
            context,
            ENCRYPTED_SP_KEY_SCHEME,
            ENCRYPTED_SP_VALUE_SCHEME
        )
        with(sharedPreferences.edit()) {
            putBoolean(AUTHORISED, true)
            putString(LOGIN, login)
            putString(PASSWORD, password)
            putString(USERNAME, userName)
            commit()
        }
    }
}