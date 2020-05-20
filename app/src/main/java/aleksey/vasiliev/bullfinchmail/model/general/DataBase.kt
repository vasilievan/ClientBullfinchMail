package aleksey.vasiliev.bullfinchmail.model.general

import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeString
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.todaysDate
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.makeMessageNumber
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.Key
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DATE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.EXTRAS
import aleksey.vasiliev.bullfinchmail.model.general.Constants.FRIENDS_USERNAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.GRAVITY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.JSON_FORMAT
import aleksey.vasiliev.bullfinchmail.model.general.Constants.LEFT_GRAVITY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MESSAGE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MESSAGES
import aleksey.vasiliev.bullfinchmail.model.general.Constants.RIGHT_GRAVITY

class DataBase {
    fun saveKey(friendsLogin: String, passwordType: String, key: ByteArray) {
        File("${Constants.MAIN_DIR}/$friendsLogin/$MESSAGES").mkdirs()
        val storage = File("${Constants.MAIN_DIR}/$friendsLogin/$passwordType$JSON_FORMAT")
        if (!storage.exists()) storage.createNewFile()
        storage.writeText(createJSONKey(key, passwordType), Constants.DEFAULT_CHARSET)
    }

    fun createKeyFromJSON(login: String, keyType: String): Key? {
        val storage = File("${Constants.MAIN_DIR}/$login/$keyType$JSON_FORMAT")
        if (!storage.exists()) return null
        val jsonObject = JSONObject(storage.readText(Constants.DEFAULT_CHARSET))
        val jsonArray = jsonObject.getJSONArray(keyType)
        val byteArray = ByteArray(Constants.EXTENDED_KEY_LENGTH)
        for (i in 0 until jsonArray.length()) {
            byteArray[i] = jsonArray.getString(i).toByte()
        }
        return if (keyType == Constants.PUBLIC_KEY) {
            KeyFactory.getInstance(Constants.KEY_ALGORIGM).generatePublic(X509EncodedKeySpec(byteArray))
        } else {
            KeyFactory.getInstance(Constants.KEY_ALGORIGM).generatePrivate(PKCS8EncodedKeySpec(byteArray))
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
        val storage = File("${Constants.MAIN_DIR}/$friendsLogin/$EXTRAS$JSON_FORMAT")
        if (!storage.exists()) storage.createNewFile()
        val jsonObject = JSONObject()
        jsonObject.put(FRIENDS_USERNAME, friendsUsername)
        storage.writeText(jsonObject.toString(), Constants.DEFAULT_CHARSET)
    }

    fun saveMessage(login: String, message: String) {
        val jsonObject = JSONObject()
        jsonObject.put(GRAVITY, RIGHT_GRAVITY)
        jsonObject.put(DATE, todaysDate())
        jsonObject.put(MESSAGE, message)
        val myMessage = File("${Constants.MAIN_DIR}/$login/$MESSAGES/${makeMessageNumber(login)}")
        myMessage.createNewFile()
        myMessage.writeText(jsonObject.toString(), Constants.DEFAULT_CHARSET)
    }

    fun saveReceivedMessage(login: String, message: ByteArray, date: ByteArray) {
        val jsonObject = JSONObject()
        jsonObject.put(GRAVITY, LEFT_GRAVITY)
        val decipher = Cipher.getInstance(Constants.KEY_TRANSFORMATION)
        val key = createKeyFromJSON(login, Constants.PRIVATE_KEY) ?: return
        decipher.init(Cipher.DECRYPT_MODE, key)
        jsonObject.put(DATE, decipher.doFinal(date).makeString())
        jsonObject.put(MESSAGE, decipher.doFinal(message).makeString())
        val myMessage = File("${Constants.MAIN_DIR}/$login/$MESSAGES/${makeMessageNumber(login)}")
        myMessage.createNewFile()
        myMessage.writeText(jsonObject.toString(), Constants.DEFAULT_CHARSET)
    }
}