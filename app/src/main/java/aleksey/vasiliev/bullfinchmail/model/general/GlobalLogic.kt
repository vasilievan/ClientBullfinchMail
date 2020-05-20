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
import aleksey.vasiliev.bullfinchmail.model.general.Constants.APP_BUFFER_SIZE
import aleksey.vasiliev.bullfinchmail.model.general.Constants.DEFAULT_CHARSET
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object GlobalLogic {
    val secureRandom = SecureRandom()

    fun getSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(Constants.ENCYPTED_SP_KEY_SPECIFICATION)
        return EncryptedSharedPreferences.create(
            Constants.ENCRYPTED_SP_NAME,
            masterKeyAlias,
            context,
            Constants.ENCRYPTED_SP_KEY_SCHEME,
            Constants.ENCRYPTED_SP_VALUE_SCHEME
        )
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
