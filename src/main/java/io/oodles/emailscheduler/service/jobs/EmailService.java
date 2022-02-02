package io.oodles.emailscheduler.service.jobs;

import java.nio.charset.StandardCharsets;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class EmailService {

	@Autowired
	JavaMailSender mailSender;

	void sendMail(String fromEmail, String toEmail, String subject, String body) {
		try {
			// like SimpleMailMessage class
			// MimeBodyParts are contained in MimeMultipart objects. MimeBodyPart uses the
			// InternetHeaders class to parse and store the headers of that body part. ...
			// MIME allows non ASCII characters to be present in certain portions of certain
			// headers, by encoding those characters. RFC 2047 specifies the rules for doing
			// this.
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());

			messageHelper.setFrom(fromEmail);
			messageHelper.setSubject(subject);
			messageHelper.setText(body, true);
			messageHelper.setTo(toEmail);

			mailSender.send(message);
		}

		catch (MessagingException ex) {
			ex.printStackTrace();
			System.out.println("Failed to send email to {}" + toEmail);
		}
	}

}
