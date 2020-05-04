package aleksey.vasiliev.bullfinchmail.model.general

import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import aleksey.vasiliev.bullfinchmail.model.general.Constants.KEY_ALGORIGM
import aleksey.vasiliev.bullfinchmail.model.general.Constants.MAIN_DIR
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date
import javax.crypto.Cipher
import java.util.Calendar

object GlobalLogic {
    val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORIGM)
    val secureRandom = SecureRandom()

    fun exchangeLoginAndPassword(login: String, password: String, cipher: Cipher, writer: OutputStream?, data: ByteArray, clientSocket: Socket?): Boolean {
        val cipheredLogin = cipher.doFinal(login.makeByteArray())
        sendSomethingToServer(writer!!, cipheredLogin)
        val ifLoginIsCorrect = readNext(data, writer, clientSocket).makeString()
        if (ifLoginIsCorrect != "Login is correct.") return false
        val cipheredPassword = cipher.doFinal(password.makeByteArray())
        sendSomethingToServer(writer, cipheredPassword)
        val ifPasswordIsCorrect = readNext(data, writer, clientSocket).makeString()
        if (ifPasswordIsCorrect != "Password is correct.") return false
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
        Toast.makeText(context, "Network is unavailable.", Toast.LENGTH_LONG).show()
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

    private fun closeClientSocket(writer: OutputStream?, clientSocket: Socket?) {
        clientSocket?.getInputStream()?.close()
        writer?.close()
        clientSocket?.close()
    }

    fun sendSomethingToServer(writer: OutputStream?, whatToSend: ByteArray) {
        writer?.write(whatToSend)
        writer?.flush()
    }

    fun readNext(data: ByteArray, writer: OutputStream?, clientSocket: Socket?): ByteArray {
        val beginningTime = Date().time
        while (true) {
            if (Date().time - beginningTime > 1000) {
                closeClientSocket(writer, clientSocket)
                break
            }
            try {
                val count = clientSocket!!.getInputStream().read(data, 0, data.size)
                if (count > 0) {
                    return data.copyOfRange(0, count)
                } else if (count == -1) {
                    closeClientSocket(writer, clientSocket)
                    break
                }
            } catch (e: IOException) {
            }
        }
        return ByteArray(0)
    }

    fun saveKey(friendsLogin: String, passwordType: String, key: ByteArray) {
        File("$MAIN_DIR/$friendsLogin").mkdirs()
        val storage = File("$MAIN_DIR/$friendsLogin/$passwordType.json")
        if (!storage.exists()) storage.createNewFile()
        storage.writeText(createJSONKey(key, passwordType), DEFAULT_CHARSET)
    }

    private fun createJSONKey(key: ByteArray, passwordType: String): String {
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()
        for (element in key) {
            jsonArray.put(element)
        }
        jsonObject.put(passwordType, jsonArray)
        jsonObject.put("date", todaysDate())
        return jsonObject.toString()
    }

    private fun todaysDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year.$month.$day"
    }
}
