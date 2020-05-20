package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.secureRandom
import aleksey.vasiliev.bullfinchmail.model.general.DataBase
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.closeClientSocket
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.exchangeLoginAndPassword
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeByteArray
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeString
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.readNext
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.sendSomethingToServer
import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.AMOUNT_RECEIVED_RESPONSE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.CHANGE_USERNAME_RESPONSE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.CHECK_UPDATES_RESPONSE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.EXCHANGE_KEYS_RESPONSE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.INCORRECT_USERNAME_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.LOGIN_AND_PASSWORD_WARNING_PHRASE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.MAKE_FRIENDS_RESPONSE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.SEND_MESSAGE_RESPONSE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.SIGN_UP_RESPONSE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.STOP_IT_RESPONSE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.SUCCEED_RESPONSE
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.SUCCESS_COMMAND
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.VALID_USER_COMMAND
import aleksey.vasiliev.bullfinchmail.model.general.Constants.AUTHORISED
import aleksey.vasiliev.bullfinchmail.model.general.Constants.EXTENDED_KEY_LENGTH
import aleksey.vasiliev.bullfinchmail.model.general.Constants.JSON_FORMAT
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_ALGORIGM
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_TRANSFORMATION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.LOGIN
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PASSWORD
import aleksey.vasiliev.bullfinchmail.model.general.Constants.POSSIBLE_PORTS
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PRIVATE_KEY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PUBLIC_KEY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.SERVER_IP
import aleksey.vasiliev.bullfinchmail.model.general.Constants.SHARED_PREFERENCES_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.USERNAME

class RegistrationLogic {

    private var clientSocket: Socket? = null
    private var publicKey: ByteArray? = null
    private val cipher = Cipher.getInstance(KEY_TRANSFORMATION)
    private var writer: OutputStream? = null

    companion object {
        fun checkLoginAndPassword (context: Context, login: String, password: String): Boolean {
            if (loginIsIncorrect(login) || passwordIsIncorrect(password)) {
                Toast.makeText(context, LOGIN_AND_PASSWORD_WARNING_PHRASE, Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }

        private fun passwordIsIncorrect(password: String): Boolean = !password.matches(Regex("""[A-Za-z\-_.\d]{8,128}"""))

        fun loginIsIncorrect(login: String): Boolean = !login.matches(Regex("""[A-Za-z][A-Za-z\-_.\d]{3,64}"""))

        fun userNameIsCorrect(context: Context, userName: String): Boolean {
            if (!userName.matches(Regex("""[A-Za-z][A-Za-z\-_.\d]{3,64}"""))) {
                Toast.makeText(context, INCORRECT_USERNAME_PHRASE, Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }

        fun saveLocalData (context: Context, login: String, password: String, userName:String) {
            val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            with (sharedPreferences.edit()) {
                putBoolean(AUTHORISED, true)
                putString(LOGIN, login)
                putString(PASSWORD, password)
                putString(USERNAME, userName)
                commit()
            }
        }
    }

    fun signingUp(login: String, password: String, userName: String): Boolean {
        sendSomethingToServer(writer!!, SIGN_UP_RESPONSE)
        val everythingsFine = exchangeLoginAndPassword(login, password, cipher, writer, clientSocket)
        if (!everythingsFine) {
            closeClientSocket()
            return false
        }
        val cipheredUserName = cipher.doFinal(userName.makeByteArray())
        sendSomethingToServer(writer!!, cipheredUserName)
        val successIndicator = readNext(clientSocket).makeString()
        if (successIndicator != SUCCESS_COMMAND) {
            closeClientSocket()
            return false
        }
        closeClientSocket()
        return true
    }


    fun closeClientSocket() {
        if (clientSocket != null) {
            writer!!.close()
            clientSocket!!.close()
        }
    }

    fun exchangeKeysWithServer(): Boolean {
        try {
            try {
                val port = POSSIBLE_PORTS.random()
                clientSocket = Socket(SERVER_IP, port)
            } catch (e: IOException) {
                return false
            }
            if (clientSocket == null) return false
            writer = clientSocket?.getOutputStream()
            sendSomethingToServer(writer, EXCHANGE_KEYS_RESPONSE)
            publicKey = readNext(clientSocket)
            if (publicKey == null) {
                return false
            }
            if (publicKey!!.isNotEmpty()) {
                initCipher()
                return true
            }
            return false
        } catch (e: IOException) {
        }
        return false
    }

    private fun initCipher() {
        cipher!!.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance(KEY_ALGORIGM).generatePublic(X509EncodedKeySpec(publicKey)))
    }

    fun changeUserName(context: Context, userName: String): Boolean {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val login = sharedPreferences.getString(LOGIN, null)
        val password = sharedPreferences.getString(PASSWORD, null)
        sendSomethingToServer(writer!!, CHANGE_USERNAME_RESPONSE)
        if (login == null || password == null || !authoriseMe(login, password)) {
            closeClientSocket()
            return false
        }
        val cipheredUsername = cipher.doFinal(userName.makeByteArray())
        sendSomethingToServer(writer, cipheredUsername)
        closeClientSocket()
        return true
    }

    fun sendRequest(login: String, password: String, userName: String): Boolean {
        sendSomethingToServer(writer!!, MAKE_FRIENDS_RESPONSE)
        if (!authoriseMe(login, password)) {
            closeClientSocket()
            return false
        }
        val keysGenerator = KeyPairGenerator.getInstance(KEY_ALGORIGM)
        keysGenerator.initialize(EXTENDED_KEY_LENGTH, secureRandom)
        val keyPair = keysGenerator.genKeyPair()
        val db = DataBase()
        db.saveKey(userName, PRIVATE_KEY, keyPair.private.encoded)
        val cipheredUserName = cipher.doFinal(userName.makeByteArray())
        sendSomethingToServer(writer, cipheredUserName)
        if (readNext(clientSocket).makeString() != VALID_USER_COMMAND) {
            closeClientSocket()
            return false
        }
        sendSomethingToServer(writer, keyPair.public.encoded)
        closeClientSocket(writer, clientSocket)
        return true
    }

    fun checkForFriendRequestsAndNewMessages(context: Context): Boolean {
        sendSomethingToServer(writer!!, CHECK_UPDATES_RESPONSE)
        var result = false
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val login = sharedPreferences.getString(LOGIN, null)
        val password = sharedPreferences.getString(PASSWORD, null)
        if (login == null || password == null || !authoriseMe(login, password)) {
            closeClientSocket(writer, clientSocket)
            return result
        }
        val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORIGM)
        keyPairGenerator.initialize(EXTENDED_KEY_LENGTH, secureRandom)
        val reverseKeys = keyPairGenerator.genKeyPair()
        val decipher = Cipher.getInstance(KEY_TRANSFORMATION)
        decipher.init(Cipher.DECRYPT_MODE, reverseKeys.private)
        sendSomethingToServer(writer, reverseKeys.public.encoded)
        val amountOfNewRequests = decipher.doFinal(readNext(clientSocket)).makeString().toInt()
        sendSomethingToServer(writer, AMOUNT_RECEIVED_RESPONSE)
        if (amountOfNewRequests != 0) result = true
        val db = DataBase()
        for (i in 0 until amountOfNewRequests) {
            val friendsLogin = decipher.doFinal(readNext(clientSocket)).makeString()
            sendSomethingToServer(writer, SUCCEED_RESPONSE)
            val friendsUsername = decipher.doFinal(readNext(clientSocket)).makeString()
            sendSomethingToServer(writer, SUCCEED_RESPONSE)
            val friendsKey = readNext(clientSocket)
            db.saveKey(friendsLogin, PUBLIC_KEY, friendsKey)
            db.saveExtras(friendsLogin, friendsUsername)
            if (!File("$MAIN_DIR/$friendsLogin/$PUBLIC_KEY$JSON_FORMAT").exists() ||
                !File("$MAIN_DIR/$friendsLogin/$PRIVATE_KEY$JSON_FORMAT").exists()) {
                val currentUserKeyPair = keyPairGenerator.genKeyPair()
                db.saveKey(friendsLogin, PRIVATE_KEY, currentUserKeyPair.private.encoded)
                sendSomethingToServer(writer, currentUserKeyPair.public.encoded)
            } else {
                sendSomethingToServer(writer, STOP_IT_RESPONSE)
            }
        }
        val amountOfNewMessages = decipher.doFinal(readNext(clientSocket)).makeString().toInt()
        if (amountOfNewMessages == 0) {
            closeClientSocket()
            return result
        }
        result = true
        for (i in 0 until amountOfNewMessages) {
            val friendsLogin = decipher.doFinal(readNext(clientSocket)).makeString()
            sendSomethingToServer(writer, SUCCEED_RESPONSE)
            val date = readNext(clientSocket)
            sendSomethingToServer(writer, SUCCEED_RESPONSE)
            val message = readNext(clientSocket)
            sendSomethingToServer(writer, SUCCEED_RESPONSE)
            db.saveReceivedMessage(friendsLogin, message, date)
        }
        closeClientSocket(writer, clientSocket)
        return result
    }

    fun sendMessage(context: Context, receiver: String, messageText: ByteArray, cipheredDate: ByteArray): Boolean {
        sendSomethingToServer(writer!!, SEND_MESSAGE_RESPONSE)
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val login = sharedPreferences.getString(LOGIN, null)
        val password = sharedPreferences.getString(PASSWORD, null)
        if (login == null || password == null || !authoriseMe(login, password)) {
            closeClientSocket(writer, clientSocket)
            return false
        }
        sendSomethingToServer(writer!!, cipher.doFinal(receiver.makeByteArray()))
        readNext(clientSocket)
        sendSomethingToServer(writer!!, cipheredDate)
        readNext(clientSocket)
        sendSomethingToServer(writer!!, messageText)
        readNext(clientSocket)
        closeClientSocket()
        return true
    }

    private fun authoriseMe(login: String, password: String): Boolean = exchangeLoginAndPassword(login, password, cipher, writer, clientSocket)
}