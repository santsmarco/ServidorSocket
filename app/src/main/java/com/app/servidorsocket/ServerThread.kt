package com.app.servidorsocket

import android.os.Handler
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

class ServerThread(private val handler: Handler) : Thread() {

    private val port = 2222
    private var serverSocket: ServerSocket? = null
    // Tempo limite para aceitar uma conexão em milissegundos
    private val CONNECTION_TIMEOUT = 15000 // 15 segundos

    override fun run() {
        try {
            serverSocket = ServerSocket(port)
            serverSocket?.soTimeout = CONNECTION_TIMEOUT

            while (true) {
                val clientSocket = serverSocket?.accept()
                val clientHandler = ClientHandler(clientSocket!!, handler)
                clientHandler.start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
        }
    }

    inner class ClientHandler(private val clientSocket: Socket, private val handler: Handler) : Thread() {
        override fun run() {
            try {
                val inputStream = clientSocket.getInputStream()
                clientSocket.receiveBufferSize = 20000
                val reader = BufferedReader(InputStreamReader(inputStream))
                val outputStream: OutputStream = clientSocket.getOutputStream()

                // Configurar um timeout de leitura (em milissegundos)
                val readTimeout = 15000 // 15 segundos
                clientSocket.soTimeout = readTimeout

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // Processar a mensagem recebida (XML)
                    val success = line?.let { processReceivedMessage(it) }

                    val response = if (success!!) "true" else "false"
                    outputStream.write(response.toByteArray())
                    outputStream.write("\n".toByteArray())
                    outputStream.flush()
                    outputStream.close() // Fechando o fluxo de saída para indicar o fim da mensagem

                    // Enviar a mensagem processada para a atividade principal (MainActivity)
                    handler.obtainMessage(0, line).sendToTarget()
                }

                clientSocket.close()
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                clientSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
                clientSocket.close()
            }
        }

        private fun processReceivedMessage(message: String): Boolean {
            // Implementar a lógica para processar a mensagem recebida (XML). Envio pro sdk comunicação fiscalFlow com sat
            // Se der certo o envio via sdk retorna true para o client
            return true // ou false, dependendo do resultado da operação
        }
    }

    fun cancel() {
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
