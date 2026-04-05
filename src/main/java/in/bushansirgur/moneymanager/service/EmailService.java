package in.bushansirgur.moneymanager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${BREVO_API_KEY}")
    private String apiKey;
    @Value("${BREVO_SENDER_EMAIL}")
    private String senderEmail;
    public void sendEmail(String toEmail, String subject, String body) {


        try {
            System.out.println("STEP 2: ABOUT TO SEND EMAIL");

            String url = "https://api.brevo.com/v3/smtp/email";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonBody = String.format("""
        {
          "sender": {"email": "%s"},
          "to": [{"email": "%s"}],
          "subject": "%s",
          "htmlContent": "%s"
        }
        """, senderEmail, toEmail, subject, body);

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            System.out.println("BREVO RESPONSE: " + response.getBody());

        } catch (Exception e) {
            System.out.println("❌ EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String filename) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        helper.addAttachment(filename, new ByteArrayResource(attachment));
        mailSender.send(message);
    }
}
