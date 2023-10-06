package com.app.servidorsocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Button
import android.widget.TextView
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

class MainActivity : AppCompatActivity() {

    private lateinit var serverThread: ServerThread
    private lateinit var txtXml: TextView
    private lateinit var txtIpServer: TextView
    private lateinit var btnLimpar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtXml = findViewById(R.id.txtXmlRecebido)
        txtIpServer = findViewById(R.id.txt_ipServer)
        btnLimpar = findViewById(R.id.btnLimpar)

        // Inicializar o servidor em uma thread separada
        serverThread = ServerThread(Handler(Looper.getMainLooper(), handleMessage))
        serverThread.start()

        // Capturar e exibir o endereço IP local
        val ipAddress = getLocalIpAddress()
        txtIpServer.text = "Endereço IP servidor: $ipAddress\nForneça esse endereço de IP no app Client"

        btnLimpar.setOnClickListener {
            txtXml.text = "Xml recebido do client"
        }
    }

    private val handleMessage = Handler.Callback { msg ->
        when (msg.what) {
            0 -> {
                val xmlData = msg.obj as String
                // Atualizar a interface do usuário com os dados XML recebidos
                txtXml.text = xmlData
            }
        }
        true
    }

    override fun onDestroy() {
        super.onDestroy()
        serverThread.cancel()
    }

    private fun getLocalIpAddress(): String {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is InetAddress) {
                        // IPv4
                        if (inetAddress.getHostAddress().matches(Regex("\\d+(\\.\\d+){3}"))) {
                            return inetAddress.hostAddress.toString()
                        }
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return "Endereço IP não encontrado"
    }
}