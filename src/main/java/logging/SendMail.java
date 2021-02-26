package logging;

import javax.mail.Session;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

import config.Configuration;
import objects.Admin;

/**
 *
 * Class for handling sending of E-mail to users who have lost their password.
 *
 * @author Sindre Thomassen
 * @author Joergen Bele Reinfjell
 * @date 16.04.2018
 * @see app.users.AdminObject
 * @see app.users.UserObject
 */
public class SendMail {
	private static Session session;

	/*
	private static String recipient = "recieveroftest@gmail.com";
	private static String sender = "elbiketest@gmail.com";
	private static String username = "elbiketest@gmail.com";
	private static String password = "testemailpassword";
	*/

	private static final String recipient = Configuration.getProperties().getProperty(Configuration.EMAIL_RECIPIENT_PROPERTY_ENTRY);
	private static final String sender = Configuration.getProperties().getProperty(Configuration.EMAIL_SENDER_PROPERTY_ENTRY);
	private static final String username = Configuration.getProperties().getProperty(Configuration.EMAIL_USERNAME_PROPERTY_ENTRY);
	private static final String password = Configuration.getProperties().getProperty(Configuration.EMAIL_PASSWORD_PROPERTY_ENTRY);

	private static Properties props;

	static {
		props = System.getProperties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", "smtp.gmail.com");
		props.setProperty("mail.user", username);
		props.setProperty("mail.password", password);

		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "587");
	}

	public void initialize() {

	}

	/**
	 * Sends a new password to a user.
	 * For now this is for testing only. On a possible release, the email will be changed to match the users email.
	 * @param user
	 * @param newPass
	 */
	public static boolean sendNewPass(String user, String newPass) {
		session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(sender));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			message.setSubject("Password Recovery.");
			message.setText("User " + user + " has requested a new password.\nIf you did not request your" +
					" password to be changed, please ignore this email.\nYour new password is:\n" + newPass);
			Transport.send(message);

			System.err.println("New password sent to: " + user);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sends a new password to a admin.
	 * Sends a new password to the email an administrator uses to log in to their account.
	 * @param user
	 * @param newPass
	 * @param admin
	 */
	public static boolean sendNewPass(String user, String newPass, Admin admin) {
	session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(sender));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(admin.getUsername()));
			message.setSubject("Password Recovery.");
			message.setText("Admin " + user + " has requested a new password.\nIf you did not request your" +
					" password to be changed, please ignore this email.\nYour new password is:\n" + newPass);
			Transport.send(message);

			System.err.println("New password sent to: " + user);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sends a new users username and password to a test email account.
	 * For now this is for testing only. On a possible release, the email will be changed to match the users email.
	 * @param user
	 * @param pass
	 */
	public static boolean newUserMail(String user, String pass) {
		session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(sender));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			message.setSubject("Confirmation Of Registration.");
			message.setText("New user registered.\nUsername: " + user + "\nPassword: " + pass);
			Transport.send(message);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean newAdmin(Admin admin) {
		session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(sender));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(admin.getUsername()));
			message.setSubject("Password Recovery.");
			message.setText("New admin registered.\nUsername: " + admin.getUsername() + "\nPassword: " + admin.getPassword());
			Transport.send(message);

			System.err.println("New password sent to: " + admin.getUsername());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
