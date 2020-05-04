package aleksey.vasiliev.bullfinchmail.model.specific

import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_LENGTH
import aleksey.vasiliev.bullfinchmail.model.general.Constants.PRIVATE_KEY
import aleksey.vasiliev.bullfinchmail.model.general.Constants.SHARED_PREFERENCES_NAME
import aleksey.vasiliev.bullfinchmail.model.general.Constants.keyTransformation
import aleksey.vasiliev.bullfinchmail.model.general.Constants.possiblePorts
import aleksey.vasiliev.bullfinchmail.model.general.Constants.serverIp
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.exchangeLoginAndPassword
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.keyGen
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeByteArray
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.makeString
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.readNext
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.saveKey
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.secureRandom
import aleksey.vasiliev.bullfinchmail.model.general.GlobalLogic.sendSomethingToServer
import android.content.Context
import android.widget.Toast
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class RegistrationLogic {

    companion object {
        fun loginIsIncorrect(login: String): Boolean = !login.matches(Regex("""[A-Za-z][A-Za-z\-_.\d]{3,64}"""))

        fun userNameIsCorrect(context: Context, userName: String): Boolean {
            if (!userName.matches(Regex("""[A-Za-z][A-Za-z\-_.\d]{3,64}"""))) {
                Toast.makeText(context, "Username has incorrect format. Use only letters, digits, -._." +
                        "It should contain at least three letters.", Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }
    }

    private var clientSocket: Socket? = null
    private var publicKey: ByteArray? = null
    private val cipher = Cipher.getInstance(keyTransformation)
    private var writer: OutputStream? = null
    private val data = ByteArray(8198)

    fun signingUp(login: String, password: String, userName: String): Boolean {
        sendSomethingToServer(writer!!, "I want to sign up.".makeByteArray())
        val everythingsFine = exchangeLoginAndPassword(login, password, cipher, writer, data, clientSocket)
        if (!everythingsFine) return false
        val cipheredUserName = cipher.doFinal(userName.makeByteArray())
        sendSomethingToServer(writer!!, cipheredUserName)
        val successIndicator = readNext(data, writer, clientSocket).makeString()
        if (successIndicator != "Success!") return false
        return true
    }


    fun closeClientSocket() {
        clientSocket?.getInputStream()?.close()
        writer?.close()
        clientSocket?.close()
    }

    fun exchangeKeysWithServer(): Boolean {
        try {
            clientSocket = Socket(serverIp, possiblePorts.random())
            writer = clientSocket!!.getOutputStream()
            sendSomethingToServer(writer!!, "I want to exchange keys.".makeByteArray())
            publicKey = readNext(data, writer, clientSocket)
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
        cipher!!.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKey)))
    }

    fun checkLoginAndPassword (context: Context, login: String, password: String): Boolean {
        if (loginIsIncorrect(login) || passwordIsIncorrect(password)) {
            Toast.makeText(context, "Login, or password has incorrect format. Use only letters, digits, -._." +
                    "Login should contain at least three letters, password - 8.", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun passwordIsIncorrect(password: String): Boolean = !password.matches(Regex("""[A-Za-z\-_.\d]{8,128}"""))

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

    fun changeUserName(login: String, password: String, userName: String): Boolean {
        sendSomethingToServer(writer!!, "I want to change a username.".makeByteArray())
        if (!autoriseMe(login, password)) return false
        val cipheredUsername = cipher.doFinal(userName.makeByteArray())
        sendSomethingToServer(writer, cipheredUsername)
        return true
    }

    fun sendRequest(login: String, password: String, userName: String): Boolean {
        sendSomethingToServer(writer!!, "I want to make friends.".makeByteArray())
        if (!autoriseMe(login, password)) return false
        val keysGenerator = keyGen
        keysGenerator.initialize(KEY_LENGTH, secureRandom)
        val keyPair = keysGenerator.genKeyPair()
        saveKey(userName, PRIVATE_KEY, keyPair.private.encoded)
        val cipheredUserName = cipher.doFinal(userName.makeByteArray())
        sendSomethingToServer(writer, cipheredUserName)
        if (readNext(data, writer, clientSocket).makeString() != "I know this user.") return false
        sendSomethingToServer(writer, keyPair.public.encoded)
        return true
    }

    private fun autoriseMe(login: String, password: String): Boolean = exchangeLoginAndPassword(login, password, cipher, writer, data, clientSocket)
}