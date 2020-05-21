package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.secureRandom
import aleksey.vasiliev.bullfinchmail.model.general.DataBase
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.closeClientSocket
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
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.getSharedPreferences
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.checkIfConnectionIsAvailable
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.CORRECT_LOGIN_COMMAND
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.CORRECT_PASSWORD_COMMAND
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

    fun sendMessageGlobally(context: Context, receiver: String,  messageText: ByteArray, cipheredDate: ByteArray): Boolean {
        if (!checkIfConnectionIsAvailable(context)) {
            return false
        }
        val exchanged = exchangeKeysWithServer()
        if (!exchanged) {
            closeClientSocket()
            return false
        }
        return sendMessage(context, receiver, messageText, cipheredDate)
    }

    fun changeUserNameGlobally(context: Context, userName: String): Boolean {
        if (!checkIfConnectionIsAvailable(context)) return false
        val exchanged = exchangeKeysWithServer()
        if (!exchanged) return false
        return changeUserName(context, userName)
    }

    fun makeFriends(context: Context, userName: String): Boolean {
        if (!checkIfConnectionIsAvailable(context)) return false
        val sharedPreferences = getSharedPreferences(context)
        val login = sharedPreferences.getString(LOGIN, null)
        if (userName == login) return false
        with(File(MAIN_DIR)) {
            val alreadyBefriended = list() ?: Array(0){""}
            if (userName in alreadyBefriended) return false
            val password = sharedPreferences.getString(PASSWORD, null)
            val registrationLogic = RegistrationLogic()
            val exchanged = registrationLogic.exchangeKeysWithServer()
            if (!exchanged) return false
            return registrationLogic.sendRequest(login!!, password!!, userName)
        }
    }

    private fun exchangeLoginAndPassword(login: String, password: String, cipher: Cipher, writer: OutputStream?, clientSocket: Socket?): Boolean {
        val cipheredLogin = cipher.doFinal(login.makeByteArray())
        sendSomethingToServer(writer!!, cipheredLogin)
        val ifLoginIsCorrect = readNext(clientSocket).makeString()
        if (ifLoginIsCorrect != CORRECT_LOGIN_COMMAND) {
            closeClientSocket(writer, clientSocket)
            return false
        }
        val cipheredPassword = cipher.doFinal(password.makeByteArray())
        sendSomethingToServer(writer, cipheredPassword)
        val ifPasswordIsCorrect = readNext(clientSocket).makeString()
        if (ifPasswordIsCorrect != CORRECT_PASSWORD_COMMAND) {
            closeClientSocket(writer, clientSocket)
            return false
        }
        return true
    }

    private fun closeClientSocket() {
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

    private fun initCipher() = cipher!!.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance(KEY_ALGORIGM).generatePublic(X509EncodedKeySpec(publicKey)))


    private fun changeUserName(context: Context, userName: String): Boolean {
        val sharedPreferences = getSharedPreferences(context)
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

    private fun sendRequest(login: String, password: String, userName: String): Boolean {
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
        val sharedPreferences = getSharedPreferences(context)
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
            with(File("$MAIN_DIR/$friendsLogin/$PUBLIC_KEY$JSON_FORMAT")) {
                if (!exists()) {
                    with(File("$MAIN_DIR/$friendsLogin/$PRIVATE_KEY$JSON_FORMAT")) {
                        if (!exists()) {
                            val currentUserKeyPair = keyPairGenerator.genKeyPair()
                            db.saveKey(friendsLogin, PRIVATE_KEY, currentUserKeyPair.private.encoded)
                            sendSomethingToServer(writer, currentUserKeyPair.public.encoded)
                        }
                    }
                } else {
                    sendSomethingToServer(writer, STOP_IT_RESPONSE)
                }
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

    private fun sendMessage(context: Context, receiver: String, messageText: ByteArray, cipheredDate: ByteArray): Boolean {
        sendSomethingToServer(writer!!, SEND_MESSAGE_RESPONSE)
        val sharedPreferences = getSharedPreferences(context)
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