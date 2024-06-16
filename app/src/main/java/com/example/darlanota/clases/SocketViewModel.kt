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

    suspend fun listaActividades(c: Context): List<Actividad> {
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(c, numText, Toast.LENGTH_SHORT).show()
                }

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
                        val numEntregas = inputStream!!.readLine()?.trim()?.toIntOrNull() ?: 0

                        // Leer y agregar entregas asociadas a la actividad
                        repeat(numEntregas) {
                            val idActividad = inputStream!!.readLine()?.trim().toString()
                            val video = inputStream!!.readLine()?.trim().toString()
                            val calificacion = inputStream!!.readLine()?.trim()?.toIntOrNull() ?: 0
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
                    // Manejar el caso donde el número recibido no es válido
                    withContext(Dispatchers.Main) {
                        Toast.makeText(c, "No se recibió un número válido de actividades", Toast.LENGTH_SHORT).show()
                    }
                    emptyList()
                }
            } catch (e: Exception) {
                // Manejo de errores de conexión o lectura/escritura
                Log.e(TAG, "Error al leer datos del servidor con las actividades: ${e.message}")
                emptyList()  // Devolver una lista vacía en caso de error
            }
        }
    }





    private fun leerEntero(input: String?): Int {
        return try {
            val cleanedInput = input?.trim()?.replace("\uFEFF", "") ?: "0" // Remover BOM si está presente
            cleanedInput.toInt()
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Error al convertir cadena a entero: ${e.message}")
            0
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
}
