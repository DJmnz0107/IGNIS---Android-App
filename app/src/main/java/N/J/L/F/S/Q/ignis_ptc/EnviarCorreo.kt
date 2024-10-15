import N.J.L.F.S.Q.ignis_ptc.activity_contrasena.numAleatorio.codigoRecu
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
        // Aquí definimos el contenido HTML con estilo CSS embebido
        val htmlMessage = """
            <html>
            <body style="background-color: #fefefe; font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #FF7043; text-align: center;">Recuperación de Contraseña</h2>
                    <p style="color: #555; font-size: 16px;">
                         <strong>$mensaje</strong>,<br><br>
                        Te saluda el equipo de Ignis Software Developers.<br>
                        Aquí tienes tu código de recuperación:
                    </p>
                    <div style="background-color: #FF7043; color: white; font-size: 24px; text-align: center; padding: 15px; border-radius: 5px;">
                        $codigoRecu
                    </div>
                    <p style="color: #555; font-size: 14px;">
                        Si no has solicitado este código, por favor ignora este mensaje.<br><br>
                        Gracias,<br>
                        El equipo de Ignis.
                    </p>
                </div>
            </body>
            </html>
        """.trimIndent()

        // Creación del mensaje de correo en formato HTML
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress("ignissoftwaredevelopers@gmail.com")) // Remitente
            addRecipient(Message.RecipientType.TO, InternetAddress(receptor)) // Destinatario
            subject = sujeto // Asunto del correo
            setContent(htmlMessage, "text/html; charset=utf-8") // Contenido en HTML
        }

        // Envío del mensaje
        Transport.send(message)
        println("Correo enviado satisfactoriamente")
    } catch (e: MessagingException) {
        e.printStackTrace() // Imprime el error en caso de fallo
        println("No se pudo enviar el correo, error: ${e.message}") // Mensaje de error
    }
}
