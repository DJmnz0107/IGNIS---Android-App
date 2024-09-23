package N.J.L.F.S.Q.ignis_ptc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

suspend fun enviarCorreo(receptor: String, sujeto: String, mensaje: String) = withContext(Dispatchers.IO) {
    // Configuración de propiedades para la conexión SMTP
    val props = Properties().apply {
        put("mail.smtp.host", "smtp.gmail.com") // Host del servidor SMTP
        put("mail.smtp.socketFactory.port", "465") // Puerto para la conexión SSL
        put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory") // Clase para la conexión SSL
        put("mail.smtp.auth", "true") // Autenticación requerida
        put("mail.smtp.port", "465") // Puerto SMTP
    }

    // Creación de la sesión con autenticación
    val session = Session.getInstance(props, object : javax.mail.Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            // Autenticación con el correo y la contraseña
            return PasswordAuthentication("ignissoftwaredevelopers@gmail.com", "kllj pgxb dmrr zhgn")
        }
    })

    try {
        // Creación del mensaje de correo
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress("ignissoftwaredevelopers@gmail.com")) // Remitente
            addRecipient(Message.RecipientType.TO, InternetAddress(receptor)) // Destinatario
            subject = sujeto // Asunto del correo
            setText(mensaje) // Cuerpo del correo
        }
        // Envío del mensaje
        Transport.send(message)
        println("Correo enviado satisfactoriamente")
    } catch (e: MessagingException) {
        e.printStackTrace() // Imprime el error en caso de fallo
        println("No se pudo enviar el correo, error: ${e.message}") // Mensaje de error
    }
}
