package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.Constants.EXTENDED_KEY_LENGTH
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_ALGORIGM
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_TRANSFORMATION
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PRIVATE_KEY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PUBLIC_KEY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.SHARED_PREFERENCES_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.possiblePorts
import aleksey.vasiliev.bullfinchmail.model.general.Constants.serverIp
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.closeClientSocket
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.exchangeLoginAndPassword
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeByteArray
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeString
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.readNext
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.saveExtras
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.saveKey
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.secureRandom
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.sendSomethingToServer
import aleksey.vasiliev.bullfinchmail.model.specific.ConversationLogic.saveMessage
import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class RegistrationLogic {

    companion object {
        fun checkLoginAndPassword (context: Context, login: String, password: String): Boolean {
            if (loginIsIncorrect(login) || passwordIsIncorrect(password)) {
                Toast.makeText(context, "Login, or password has incorrect format. Use only letters, digits, -._." +
                        "Login should contain at least three letters, password - 8.", Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }

        private fun passwordIsIncorrect(password: String): Boolean = !password.matches(Regex("""[A-Za-z\-_.\d]{8,128}"""))

        fun loginIsIncorrect(login: String): Boolean = !login.matches(Regex("""[A-Za-z][A-Za-z\-_.\d]{3,64}"""))

        fun userNameIsCorrect(context: Context, userName: String): Boolean {
            if (!userName.matches(Regex("""[A-Za-z][A-Za-z\-_.\d]{3,64}"""))) {
                Toast.makeText(context, "Username has incorrect format. Use only letters, digits, -._." +
                        "It should contain at least three letters.", Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }

        fun saveLocalData (context: Context, login: String, password: String, userName:String) {
            val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            with (sharedPreferences.edit()) {
                putBoolean("authorised", true)
                putString("login", login)
                putString("password", password)
                putString("userName", userName)
                commit()
            }
        }
    }

    private var clientSocket: Socket? = null
    private var publicKey: ByteArray? = null
    private val cipher = Cipher.getInstance(KEY_TRANSFORMATION)
    private var writer: OutputStream? = null
    private val data = ByteArray(8198)


    // ok
    fun signingUp(login: String, password: String, userName: String): Boolean {
        sendSomethingToServer(writer!!, "I want to sign up.".makeByteArray())
        val everythingsFine = exchangeLoginAndPassword(login, password, cipher, writer, data, clientSocket)
        if (!everythingsFine) {
            closeClientSocket()
            return false
        }
        val cipheredUserName = cipher.doFinal(userName.makeByteArray())
        sendSomethingToServer(writer!!, cipheredUserName)
        val successIndicator = readNext(data, clientSocket).makeString()
        if (successIndicator != "Success!") {
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
                clientSocket = Socket(serverIp, possiblePorts.random())
            } catch (e: IOException) {
                return false
            }
            if (clientSocket == null) return false
            writer = clientSocket?.getOutputStream()
            sendSomethingToServer(writer, "I want to exchange keys.".makeByteArray())
            publicKey = readNext(data, clientSocket)
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
        val login = sharedPreferences.getString("login", null)
        val password = sharedPreferences.getString("password", null)
        sendSomethingToServer(writer!!, "I want to change a username.".makeByteArray())
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
        sendSomethingToServer(writer!!, "I want to make friends.".makeByteArray())
        if (!authoriseMe(login, password)) {
            closeClientSocket()
            return false
        }
        val keysGenerator = KeyPairGenerator.getInstance(KEY_ALGORIGM)
        keysGenerator.initialize(EXTENDED_KEY_LENGTH, secureRandom)
        val keyPair = keysGenerator.genKeyPair()
        saveKey(userName, PRIVATE_KEY, keyPair.private.encoded)
        val cipheredUserName = cipher.doFinal(userName.makeByteArray())
        sendSomethingToServer(writer, cipheredUserName)
        if (readNext(data, clientSocket).makeString() != "I know this user.") {
            closeClientSocket()
            return false
        }
        sendSomethingToServer(writer, keyPair.public.encoded)
        closeClientSocket(writer, clientSocket)
        return true
    }

    fun checkForFriendRequestsAndNewMessages(context: Context): Boolean {
        sendSomethingToServer(writer!!, "I want to check for friends requests and new messages.".makeByteArray())
        var result = false
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val login = sharedPreferences.getString("login", null)
        val password = sharedPreferences.getString("password", null)
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
        val amountOfNewRequests = decipher.doFinal(readNext(data, clientSocket)).makeString().toInt()
        sendSomethingToServer(writer, "Amount received.".makeByteArray())
        if (amountOfNewRequests != 0) result = true
        for (i in 0 until amountOfNewRequests) {
            val friendsLogin = decipher.doFinal(readNext(data, clientSocket)).makeString()
            sendSomethingToServer(writer, "Succeed.".makeByteArray())
            val friendsUsername = decipher.doFinal(readNext(data, clientSocket)).makeString()
            sendSomethingToServer(writer, "Succeed.".makeByteArray())
            val friendsKey = readNext(data, clientSocket)
            saveKey(friendsLogin, PUBLIC_KEY, friendsKey)
            saveExtras(friendsLogin, friendsUsername)
            if (!File("$MAIN_DIR/$friendsLogin/privateKey.json").exists()) {
                val currentUserKeyPair = keyPairGenerator.genKeyPair()
                saveKey(friendsLogin, PRIVATE_KEY, currentUserKeyPair.private.encoded)
                sendSomethingToServer(writer, currentUserKeyPair.public.encoded)
            } else {
                sendSomethingToServer(writer, "Stop it.".makeByteArray())
            }
        }
        val amountOfNewMessages = decipher.doFinal(readNext(data, clientSocket)).makeString().toInt()
        if (amountOfNewMessages == 0) {
            closeClientSocket()
            return result
        }
        result = true
        for (i in 0 until amountOfNewMessages) {
            val friendsLogin = decipher.doFinal(readNext(data, clientSocket)).makeString()
            sendSomethingToServer(writer, "Succeed.".makeByteArray())
            val message = readNext(data, clientSocket).makeString()
            sendSomethingToServer(writer, "Succeed.".makeByteArray())
            val date = readNext(data, clientSocket).makeString()
            sendSomethingToServer(writer, "Succeed.".makeByteArray())
            saveMessage(friendsLogin, message, 1, date)
        }
        closeClientSocket(writer, clientSocket)
        return result
    }

    fun sendMessage(context: Context, receiver: String, messageText: ByteArray, cipheredDate: ByteArray): Boolean {
        sendSomethingToServer(writer!!, "I want to send a message.".makeByteArray())
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val login = sharedPreferences.getString("login", null)
        val password = sharedPreferences.getString("password", null)
        if (login == null || password == null || !authoriseMe(login, password)) {
            closeClientSocket(writer, clientSocket)
            return false
        }
        sendSomethingToServer(writer!!, cipher.doFinal(receiver.makeByteArray()))
        readNext(data, clientSocket)
        sendSomethingToServer(writer!!, cipheredDate)
        readNext(data, clientSocket)
        sendSomethingToServer(writer!!, messageText)
        readNext(data, clientSocket)
        closeClientSocket()
        return true
    }

    private fun authoriseMe(login: String, password: String): Boolean = exchangeLoginAndPassword(login, password, cipher, writer, data, clientSocket)
}