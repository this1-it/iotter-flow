package it.thisone.iotter.integration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.Priority;

/**
 * Email utility.
 * 
 */

@Service
public class EmailService {
	private static Logger logger = LoggerFactory
			.getLogger(Constants.Notifications.LOG4J_CATEGORY);
	
	
//	private static Logger logger = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private VelocityEngine velocityEngine;

	@Autowired
	@Qualifier("mailMessages")
	private Properties mailMessages;

	@Autowired
	@Qualifier("javaMailProperties")
	private Properties javaMailProperties;

	@Autowired
	private JavaMailSender mailSender;

	private boolean DEBUG = true;

	
	
	/*
	 * Feature #2162 Export weekly data 
	 */
	public String forwardWeeklyExport(String[] emails, Locale locale, File[] attachments, Map<String, Object> params)
			throws IOException, MessagingException {
		return message(null, null, emails, "forward_weekly_export", Priority.NORMAL.getValue(), locale, attachments, params);
	}

	
	/*
	 * Feature #167 Export data from visualizations
	 */
	public String forwardVisualization(String to, Locale locale, File exported, Map<String, Object> params)
			throws IOException, MessagingException {
		File[] attachments = new File[] { exported };
		return message(to, null, null, "forward_visualization", Priority.NORMAL.getValue(), locale, attachments, params);
	}

	/**
	 * 
	 * @param to
	 * @param locale
	 * @param params
	 *            : name
	 */
	public String successResetPassword(String to, Locale locale, Map<String, Object> params)
			throws IOException, MessagingException {
		return message(to, null, null, "successresetpassword", Priority.NORMAL.getValue(), locale, null, params);
	}

	/**
	 * 
	 * @param to
	 * @param locale
	 * @param params
	 *            : name, url, username, password
	 */
	public String registration(String to, Locale locale, Map<String, Object> params)
			throws IOException, MessagingException {
		return message(to, null, null, "registration", Priority.NORMAL.getValue(), locale, null, params);
	}

	/**
	 * 
	 * @param to
	 * @param locale
	 * @param params
	 *            : name, username, url
	 * 
	 */
	public String resetPassword(String to, Locale locale, Map<String, Object> params)
			throws IOException, MessagingException {
		return message(to, null, null, "resetpassword", Priority.NORMAL.getValue(), locale, null, params);
	}

	public String alarmNotification(String[] emails, Priority priority, Locale locale, Map<String, Object> params)
			throws IOException, MessagingException {
		if (javaMailProperties.getProperty("mail.alert", "ENABLED").equalsIgnoreCase("DISABLED")) {
			logger.info("mail.alert is DISABLED");
			return null;
		}
		return message(null, null, emails, "alarm_notification", priority.getValue(), locale, null, params);
	}

	public String alarmReset(String[] emails, Priority priority, Locale locale, Map<String, Object> params)
			throws IOException, MessagingException {
		if (javaMailProperties.getProperty("mail.alert", "ENABLED").equalsIgnoreCase("DISABLED")) {
			logger.info("mail.alert is DISABLED");
			return null;
		}

		
		return message(null, null, emails, "alarm_reset", priority.getValue(), locale, null, params);
	}

	public String alarmInactivity(String[] emails, Priority priority, Locale locale, Map<String, Object> params)
			throws IOException, MessagingException {
		if (javaMailProperties.getProperty("mail.alert", "ENABLED").equalsIgnoreCase("DISABLED")) {
			logger.info("mail.alert is DISABLED");
			return null;
		}

		return message(null, null, emails, "alarm_inactivity", priority.getValue(), locale, null, params);
	}

	/**
	 * 
	 * @param to
	 * @param messageName
	 * @param priority
	 *            1 = high, 3 = normal, 5 = low
	 * @param locale
	 * @param files
	 * @param params
	 * @throws IOException
	 * @throws MessagingException
	 */
	public String message(String to, String[] cc, String[] bcc, String messageName, int priority, Locale locale,
			File[] attachments, Map<String, Object> params) throws IOException, MessagingException {

		// String subject = MessageFormat.format(
		// mailMessages.getProperty(messageName + ".subject"),
		// (Object[]) null, params);
		// String body = MessageFormat.format(
		// mailMessages.getProperty(messageName + ".body"),
		// (Object[]) null, params);

		String images = javaMailProperties.getProperty("mail.images","");
		params.put("images", images);
		
		String subject = mergeVelocityString(messageName, mailMessages.getProperty(messageName + ".subject", ""),
				params);
		String template = mailMessages.getProperty(messageName + ".template");
		String body = mergeVelocityTemplate(messageName, template, params);

//		String[] images = StringUtils.split(mailMessages.getProperty(messageName + ".images"), "");
//		File[] cids = new File[images.length];
//		for (int i = 0; i < images.length; i++) {
//			Resource resource = new ClassPathResource(images[i]);
//			if (resource.exists()) {
//				cids[i] = resource.getFile();
//			}
//		}

		return send(to, subject, body, cc, bcc, priority, attachments);
	}

	private String mergeVelocityString(String messageName, String templateStr, Map<String, Object> params) {
		/**
		 * Prepare context data
		 */
		VelocityContext context = new VelocityContext(params);
		StringWriter swOut = new StringWriter();
		/**
		 * Merge data and template
		 */
		velocityEngine.evaluate(context, swOut, messageName, templateStr);
		return swOut.toString();

	}

	private String mergeVelocityTemplate(String messageName, String template, Map<String, Object> params) {
		if (template == null) {
			return "";
		}
		VelocityContext context = new VelocityContext(params);
		Template templateResource = velocityEngine.getTemplate(template, "UTF-8");
		StringWriter swOut = new StringWriter();
		templateResource.merge(context, swOut);
		return swOut.toString();
	}

	/**
	 * Sends email.
	 * 
	 * @param smtpHost
	 *            the SMTP host
	 * @param to
	 *            target email address
	 * @param from
	 *            from email address
	 * @param subject
	 *            the email subject
	 * @param body
	 *            the email body
	 * @return
	 * @throws MessagingException
	 */
	public String send(String to, String subject, String htmlContent, String[] cc, String[] bcc, int priority,
			File[] attachments) throws MessagingException {

		if (to == null && cc == null && bcc == null) {
			return null;
		}
		if (mailSender.createMimeMessage() == null) {
			return null;
		}

		((JavaMailSenderImpl) mailSender).getSession().setDebug(DEBUG);
		String noreply = javaMailProperties.getProperty("mail.no-reply",
				((JavaMailSenderImpl) mailSender).getUsername());

		// Text part
		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setHeader("MIME-Version", "1.0");
		textPart.setHeader("Content-Type", textPart.getContentType());
		textPart.setText("");

		// HTML part
		final MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setHeader("MIME-Version", "1.0");
		htmlPart.setHeader("Content-Type", htmlPart.getContentType());


		htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");

		final Multipart multiPartContent = new MimeMultipart("alternative");
		multiPartContent.addBodyPart(textPart);
		multiPartContent.addBodyPart(htmlPart);

		if (attachments != null) {
			for (int i = 0; i < attachments.length; i++) {
				if (attachments[i] != null) {
					MimeBodyPart fileContent = new MimeBodyPart();
					FileDataSource fds = new FileDataSource(attachments[i].getPath());
					fileContent.setDataHandler(new DataHandler(fds));
					fileContent.setFileName(fds.getName());
					multiPartContent.addBodyPart(fileContent);
				}
			}
		}

//		if (cids != null) {
//			for (int i = 0; i < cids.length; i++) {
//				try {
//					MimeBodyPart imagePart = new MimeBodyPart();
//					imagePart.attachFile(cids[i]);
//					imagePart.setContentID("<" + cids[i].getName() + ">");
//					imagePart.setDisposition(MimeBodyPart.INLINE);
//					multiPartContent.addBodyPart(imagePart);
//		imagePart.addHeader("Content-Type", "image/png"); 
//				} catch (IOException e) {
//				}
//			}
//		}

		final MimeMessage message = mailSender.createMimeMessage();
		message.setHeader("MIME-Version", "1.0");
		message.setHeader("Content-Type", multiPartContent.getContentType());
		message.setHeader("X-Mailer", "iotter");
		message.setHeader("X-Priority", String.valueOf(priority));

		// http://www.coderanch.com/t/274372/java/java/Setting-Priorities-Mail

		message.setSentDate(new Date());
		message.setFrom(new InternetAddress(noreply));

		if (to != null) {
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
		}
		if (cc != null) {
			String others = distributionList(cc);
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(others, false));
		}
		if (bcc != null) {
			String others = distributionList(bcc);
			message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(others, false));
		}

		message.setSubject(subject);
		message.setContent(multiPartContent);
		
		
		message.saveChanges(); 
		// Ensure the message is finalized & Message-ID is set 
		String messageId = message.getMessageID(); 

		// mailSender.send(message);
		// logger.debug("sending Message-ID {}",messageId);
		return messageId;
		
		
	}

	private String distributionList(String[] addresses) {
		ClassPathResource resource = new ClassPathResource("smtp.properties");	
		if (!resource.exists()) {
			return javaMailProperties.getProperty("mail.catch-all", "");
		}
		return StringUtils.join(addresses, ",");
	}

	/**
	 * Inner class to act as a JAF data-source to send HTML e-mail content.
	 */
	static class HTMLDataSource implements DataSource {
		/** The HTML content. */
		private final String htmlContent;

		/**
		 * Default constructor for setting the source HTML.
		 * 
		 * @param htmlContent
		 *            the HTML content
		 */
		public HTMLDataSource(final String htmlContent) {
			this.htmlContent = htmlContent;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(htmlContent.getBytes());
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getContentType() {
			return "text/html";
		}

		@Override
		public String getName() {
			return "JAF text/html dataSource to send e-mail.";
		}
	}

	

	
}
