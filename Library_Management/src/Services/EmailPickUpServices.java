package Services;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailPickUpServices {

    private static final String FROM_EMAIL = "wong.brandon111@gmail.com";
    private static final String APP_PASSWORD = "xzaetmkcpuqscrqd"; // move to env later

    public static void sendPickupNotification(
            String toEmail,
            String memberName,
            String bookTitle,
            String bookLocation
    ) throws MessagingException {

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

        message.setSubject("Library System – Book Ready for Pickup");
        message.setText(
                "Hello " + memberName + ",\n\n" +
                        "Your requested book is now ready for pickup.\n\n" +
                        "Book Title : " + bookTitle + "\n" +
                        "Location   : " + bookLocation + "\n\n" +
                        "Please collect it during library operating hours.\n\n" +
                        "Thank you,\n" +
                        "Library System"
        );

        Transport.send(message);
    }

}
