package Services;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailServices {

    private static final String FROM_EMAIL = "wong.brandon111@gmail.com";
    private static final String APP_PASSWORD = "xzaetmkcpuqscrqd"; // move to env later

    public static void sendTemporaryPassword(String toEmail, String tempPassword)
            throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
        );

        message.setSubject("Library System – Temporary Password");
        message.setText(
                "Hello,\n\n" +
                        "You requested a password reset.\n\n" +
                        "Temporary Password: " + tempPassword + "\n\n" +
                        "Please login and change your password immediately.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Library System"
        );

        Transport.send(message);
    }
}
