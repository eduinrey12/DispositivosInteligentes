package com.uteq.dispositivos

import android.os.AsyncTask
import java.io.UnsupportedEncodingException
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Enviar_Correo(
    private val de: String,
    private val para: String,
    private val asunto: String,
    private val cuerpo: String
) : AsyncTask<Void?, Void?, Void?>() {

    override fun doInBackground(vararg voids: Void?): Void? {
        sendMail()
        return null
    }

    private fun sendMail() {
        val host = "smtp.gmail.com"
        val username = "ereyb@uteq.edu.ec"
        val password = "bofn xvtv cswq tdty"

        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = host
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message: Message = MimeMessage(session)
            message.setFrom(InternetAddress(de, "[Dispositivos]"))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para))
            message.subject = asunto
            message.setText(cuerpo)

            Transport.send(message)

            // Aquí puedes agregar código para manejar el éxito del envío de correo
        } catch (e: MessagingException) {
            // Aquí puedes agregar código para manejar errores
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }
}
