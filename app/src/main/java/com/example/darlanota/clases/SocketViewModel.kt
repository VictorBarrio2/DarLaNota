package com.example.darlanota.clases

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.darlanota.modelos.PaginaActividadAlumno
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

private const val TAG = "SocketViewModel"

class SocketViewModel : ViewModel() {

    private var socket: Socket? = null
    private var outputStream: BufferedWriter? = null
    private var inputStream: BufferedReader? = null

    suspend fun conectarServidor(ip: String, puerto: Int) {
        // Use Dispatchers.IO for network operations
        withContext(Dispatchers.IO) {
            try {
                // Si el socket no está inicializado o está cerrado, se crea una nueva conexión
                if (socket == null || socket!!.isClosed) {
                    socket = Socket(ip, puerto)
                    outputStream = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream(), "UTF-8"))
                    inputStream = BufferedReader(InputStreamReader(socket!!.getInputStream(), "UTF-8"))
                    Log.d(TAG, "Conectado al servidor $ip en el puerto $puerto")
                }else{

                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al conectar con el servidor: ${e.message}")
            }
        }
    }

    suspend fun listaActividades(): List<Actividad> {
        return withContext(Dispatchers.IO) {
            try {
                if (socket == null || socket!!.isClosed) {
                    throw Exception("Socket no inicializado o cerrado")
                }

                // Enviar el número 3 al servidor
                outputStream!!.write("3\n")
                outputStream!!.flush()

                val cantidadActividadesStr = inputStream!!.readLine()?.trim()
                val cleanStr = cantidadActividadesStr?.replace("[^\\d+]".toRegex(), "")
                val numero: Int = cleanStr?.toIntOrNull() ?: 0
                Log.d(TAG, "Received string: $cantidadActividadesStr")

                // Mostrar Toast con el número recibido (para depuración)
                val numText = "El número es $numero"
                Log.d(TAG, numText) // Log para asegurar que se recibió correctamente

                // Verificar si el número es válido y mayor que cero para usarlo en un bucle
                if (numero > 0) {
                    // Crear una lista mutable para almacenar las actividades
                    val actividades = mutableListOf<Actividad>()

                    // Iterar desde 1 hasta el número recibido
                    repeat(numero) {
                        val actividad = Actividad()

                        // Leer datos de la actividad
                        actividad.id = inputStream!!.readLine()?.trim().toString()
                        actividad.descripcion = inputStream!!.readLine()?.trim().toString()
                        actividad.fechafin = inputStream!!.readLine()?.trim().toString()
                        actividad.id_profesor = inputStream!!.readLine()?.trim().toString()
                        actividad.instrumento = inputStream!!.readLine()?.trim().toString()
                        actividad.titulo = inputStream!!.readLine()?.trim().toString()

                        // Leer cantidad de entregas asociadas
                        val numEn = inputStream!!.readLine()?.trim()
                        val cleanEn = numEn?.replace("[^\\d+]".toRegex(), "")
                        val numEntregas: Int = cleanEn?.toIntOrNull() ?: 0

                        // Leer y agregar entregas asociadas a la actividad
                        repeat(numEntregas) {
                            val idActividad = inputStream!!.readLine()?.trim().toString()
                            val video = inputStream!!.readLine()?.trim().toString()
                            val cal = inputStream!!.readLine()?.trim()
                            val cleanCal = cal?.replace("[^\\d+]".toRegex(), "")
                            val calificacion: Int = cleanCal?.toIntOrNull() ?: 0
                            val idAlumno = inputStream!!.readLine()?.trim().toString()

                            // Crear y agregar entrega solo si el id de actividad coincide
                            if (idActividad == actividad.id) {
                                actividad.entregas.add(Entrega(idActividad, video, calificacion, idAlumno))
                            }
                        }

                        // Agregar la actividad a la lista
                        actividades.add(actividad)
                    }

                    // Devolver la lista de actividades construida
                    actividades
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                // Manejo de errores de conexión o lectura/escritura
                Log.e(TAG, "Error al leer datos del servidor con las actividades: ${e.message}")
                emptyList()  // Devolver una lista vacía en caso de error
            }
        }
    }
    suspend fun enviarDatosInicioSesion(nick: String, contra: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Enviando datos de inicio de sesión: nick=$nick, contra=$contra")
                outputStream!!.write("1\n")  // Enviar el número 1 seguido de salto de línea
                outputStream!!.write("$nick\n")  // Enviar el nick seguido de salto de línea
                outputStream!!.write("$contra\n")  // Enviar la contraseña seguido de salto de línea
                outputStream!!.flush()

                val respuesta = inputStream!!.readLine()?.trim() ?: ""
                Log.d(TAG, "Respuesta del servidor: $respuesta")

                // Comparar la respuesta de manera adecuada
                respuesta.length > 3
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar datos al servidor: ${e.message}")
                false
            }
        }
    }

    suspend fun enviarDatosRegistro(nick: String, contra: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Enviando datos de registro: nick=$nick, contra=$contra")
                outputStream!!.write("2\n")  // Enviar el número 1 seguido de salto de línea
                outputStream!!.write("$nick\n")  // Enviar el nick seguido de salto de línea
                outputStream!!.write("$contra\n")  // Enviar la contraseña seguido de salto de línea
                outputStream!!.flush()

                val res = inputStream!!.readLine()?.trim()
                val clearRes = res?.replace("[^\\d+]".toRegex(), "")
                val numRes: Int = clearRes?.toIntOrNull() ?: 0

                Log.d(TAG, "Respuesta del servidor: $numRes")

                numRes
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar datos al servidor: ${e.message}")
                4
            }
        }
    }

    override fun onCleared() {
        // Cerrar recursos cuando el ViewModel sea destruido
        try {
            outputStream?.close()
            inputStream?.close()
            socket?.close()
            Log.d(TAG, "Recursos cerrados correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar recursos: ${e.message}")
        }
        super.onCleared()
    }

    suspend fun devolverAlumno(nick: String): Alumno? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Pidiendo datos al servidor del usuario: $nick")
                outputStream!!.write("6\n")
                outputStream!!.write("$nick\n")
                outputStream!!.flush()

                val id = inputStream!!.readLine()?.trim() ?: return@withContext null
                val contra = inputStream!!.readLine()?.trim() ?: return@withContext null
                val cal = inputStream!!.readLine()?.trim() ?: return@withContext null
                val clearCal = cal.replace("[^\\d+]".toRegex(), "")
                val calificacion: Int = clearCal.toIntOrNull() ?: 0

                Alumno(id, nick, contra, calificacion)
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar datos al servidor: ${e.message}")
                null
            }
        }
    }

    suspend fun cambiarContrasena(nick: String, contraNueva: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {

                if(contraNueva.length < 5){
                    false
                }else{
                    Log.d(TAG, "Enviando solicitud de cambio de contraseña para el usuario: $nick")
                    outputStream!!.write("8\n")
                    outputStream!!.write("$nick\n")
                    outputStream!!.write("$contraNueva\n")
                    outputStream!!.flush()

                    val respuesta = inputStream!!.readLine()?.trim() ?: ""
                    Log.d(TAG, "Respuesta del servidor: $respuesta")

                    val res = inputStream!!.readLine()?.trim() ?: ""
                    val resCal = res.replace("[^\\d+]".toRegex(), "")
                    val respuestaFinal: Int = resCal.toIntOrNull() ?: 0
                    Log.d(TAG, "Respuesta del servidor: $respuestaFinal")
                    if(respuestaFinal == 1){
                        true
                    }else{
                        false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar datos al servidor: ${e.message}")
                false
            }
        }
    }
}
