package aleksey.vasiliev.bullfinchmail.model.general

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import aleksey.vasiliev.bullfinchmail.model.general.Constants.APP_BUFFER_SIZE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.CORRECT_LOGIN_COMMAND
import aleksey.vasiliev.bullfinchmail.model.general.ProtocolPhrases.CORRECT_PASSWORD_COMMAND

object GlobalLogic {
    val secureRandom = SecureRandom()

    fun exchangeLoginAndPassword(login: String, password: String, cipher: Cipher, writer: OutputStream?, clientSocket: Socket?): Boolean {
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

    fun String.makeByteArray(): ByteArray = this.toByteArray(DEFAULT_CHARSET)

    fun ByteArray.makeString(): String = String(this, DEFAULT_CHARSET)

    fun checkIfConnectionIsAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val indicator = activeNetwork?.isConnectedOrConnecting ?: false
        if (indicator) {
            return true
        }
        return false
    }

    fun askForPermissions(context: Context, activity: Activity) {
        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(activity, arrayOf(
                Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), generateRequestCode())
        }
    }

    private fun generateRequestCode(): Int = (0..65535).random()

    fun closeClientSocket(writer: OutputStream?, clientSocket: Socket?) {
        writer?.close()
        clientSocket?.close()
    }

    fun sendSomethingToServer(writer: OutputStream?, whatToSend: ByteArray) {
        writer?.write(whatToSend)
        writer?.flush()
    }

    fun readNext(clientSocket: Socket?): ByteArray {
        val data = ByteArray(APP_BUFFER_SIZE)
        while (true) {
            try {
                val count = clientSocket!!.getInputStream().read(data, 0, APP_BUFFER_SIZE)
                if (count > 0) {
                    return data.copyOfRange(0, count)
                } else if (count == -1) {
                    break
                }
            } catch (e: IOException) {
            }
        }
        return ByteArray(0)
    }

    fun todaysDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return "$day.$month.$year $hour:$minute"
    }
}
