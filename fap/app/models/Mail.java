package models;

import java.util.*;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.data.validation.*;
import org.joda.time.DateTime;
import models.*;
import messages.Messages;
import validation.*;
import audit.Auditable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===
import javax.mail.internet.InternetAddress;
import play.exceptions.MailException;
import play.templates.TemplateLoader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

// === IMPORT REGION END ===

@Entity
@Table(name = "email")
public class Mail extends FapModel {
	// Código de los atributos

	public String idMail;

	public String bcc;

	public String cc;

	@Column(columnDefinition = "LONGTEXT")
	public String content;

	public String footer;

	public String sendTo;

	public String sender;

	public String subject;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	private Mail render(Map<String, Object> args) {
		Mail mail = new Mail();
		if ((cc != null) && (!cc.isEmpty()))
			mail.cc = TemplateLoader.loadString(cc).render(args);
		if ((bcc != null) && (!bcc.isEmpty()))
			mail.bcc = TemplateLoader.loadString(bcc).render(args);
		if ((sender != null) && (!sender.isEmpty()))
			mail.sender = TemplateLoader.loadString(sender).render(args);
		if ((sendTo != null) && (!sendTo.isEmpty()))
			mail.sendTo = TemplateLoader.loadString(sendTo).render(args);
		mail.content = TemplateLoader.loadString(content).render(args);
		mail.footer = TemplateLoader.loadString(footer).render(args);
		mail.subject = TemplateLoader.loadString(subject).render(args);
		return mail;
	}

	public void send(Map<String, Object> args) {
		Mail render = render(args);
		try {
			SimpleEmail emailTo = new SimpleEmail();
			SimpleEmail emailBcc = new SimpleEmail();

			emailBcc.setCharset("utf-8");
			emailTo.setCharset("utf-8");

			emailBcc.updateContentType("text/plain");
			emailTo.updateContentType("text/plain");

			if (render.sender != null) {
				try {
					InternetAddress iAddress = new InternetAddress(render.sender);
					emailBcc.setFrom(iAddress.getAddress(), iAddress.getPersonal());
					emailTo.setFrom(iAddress.getAddress(), iAddress.getPersonal());
				} catch (Exception e) {
					emailBcc.setFrom(render.sender);
					emailTo.setFrom(render.sender);
				}

			}

			if (render.sendTo != null) {
				for (String recipient : StringUtils.split(render.sendTo, ",")) {
					try {
						InternetAddress iAddress = new InternetAddress(recipient);
						emailTo.addTo(iAddress.getAddress(), iAddress.getPersonal());
					} catch (Exception e) {
						emailTo.addTo(recipient.toString());
					}
				}
			}

			if (render.cc != null) {
				for (String recipient : StringUtils.split(render.cc, ",")) {
					try {
						InternetAddress iAddress = new InternetAddress(recipient);
						emailTo.addTo(iAddress.getAddress(), iAddress.getPersonal());
					} catch (Exception e) {
						emailTo.addTo(recipient.toString());
					}
				}
			}

			if (render.bcc != null) {
				for (String recipient : StringUtils.split(render.bcc, ",")) {
					try {
						InternetAddress iAddress = new InternetAddress(recipient);
						emailBcc.addTo(iAddress.getAddress(), iAddress.getPersonal());
					} catch (Exception e) {
						emailBcc.addTo(recipient.toString());
					}
				}
			}

			emailBcc.setSubject(render.subject);
			emailTo.setSubject(render.subject);

			emailBcc.setMsg(render.content + "\n\n\n" + render.footer);
			emailTo.setMsg(render.content + "\n\n\n" + render.footer);

			if (render.sendTo != null)
				play.libs.Mail.send(emailTo);
			if (render.bcc != null)
				play.libs.Mail.send(emailBcc);
		} catch (EmailException e) {
			throw new MailException("Cannot send email", e);
		}
	}
	// === MANUAL REGION END ===

}
