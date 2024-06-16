package com.example.darlanota.clases

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class SocketViewModel : ViewModel() {
    lateinit var socket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: BufferedWriter

    suspend fun conectarServidor(ip: String, port: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket = Socket(ip, port)
                reader = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
                writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), "UTF-8"))
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error al conectar con el servidor: ${e.message}")
                false
            }
        }
    }

    suspend fun enviarDatosInicioSesion(nick: String, contra: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                writer.write("1\n")  // Enviar el número 1 seguido de salto de línea
                writer.write("$nick\n")  // Enviar el nick seguido de salto de línea
                writer.write("$contra\n")  // Enviar la contraseña seguido de salto de línea
                writer.flush()

                val respuesta = reader.readLine()
                Log.d(TAG, "Respuesta del servidor: $respuesta")

                respuesta.trim() == "0"
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar datos al servidor: ${e.message}")
                false
            }
        }
    }

    fun desconectarServidor() {
        try {
            reader.close()
            writer.close()
            socket.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar la conexión: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SocketViewModel"
    }
}
