/**
 * ***********************************
 * AVR SOLUTIONS            *
 * ***********************************
 * <p>
 * This class send email
 * is link with Template class and EmailAccount class
 *
 * @author David Barbarin
 * @version 1.0
 */

package com.avr.apps.docgen.common;

import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.common.utils.RegexUtils;
import com.avr.apps.docgen.exception.AvrException;
import com.avr.apps.docgen.exception.EmailException;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.common.base.Preconditions;
import com.sun.mail.smtp.SMTPTransport;
import groovy.transform.NotYetImplemented;
import org.jboss.resteasy.spi.NotImplementedYetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The type Email.
 */
public class Email {

    private static final int SMTP = 1;
    private static final int POP = 2;
    private static final int IMAP = 3;
    private static final char SEPARATOR = '$';
    private static final String SEPARATOR_ADDRESS = ",|\\||;";
    private final String host;
    private final Integer port;
    private final String username;
    private final String password;
    private final String email_from;
    private final String email_to;
    private final String[] email_to_cc;
    private final String subject;
    private final SMTPTransport smtpTransport;
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Map<String, String> parameters;
    private final Message message;

    private String codeLang = "fr";
    private String content;
    private Multipart multipart = null;
    private String response;
    private Object entity = null;

    /**
     * Instantiates a new Email.
     *
     * @param template     the template
     * @param emailAccount the email account
     * @throws NoSuchProviderException the no such provider exception
     * @throws AvrException            the avr exception
     */
    public Email(Template template, EmailAccount emailAccount) throws NoSuchProviderException, AvrException {
        Properties properties = System.getProperties();
        parameters = new HashMap<>();
        username = emailAccount.getLogin();
        password = emailAccount.getPassword();
        host = emailAccount.getHost();
        port = emailAccount.getPort();
        email_from = emailAccount.getFromAddress();
        subject = template.getSubject();
        email_to_cc =
            ObjectUtils.notEmpty(template.getCcRecipients()) ? template.getCcRecipients().split(SEPARATOR_ADDRESS) : null;
        content = template.getContent();
        email_to = template.getToRecipients();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", emailAccount.getHost());
        properties.put("mail.smtp.port", emailAccount.getPort());

        if (emailAccount.getSecuritySelect().equals(EmailAccountRepository.SECURITY_STARTTLS)) properties.put(
            "mail.smtp.starttls.enable",
            "true"
        ); else if (emailAccount.getSecuritySelect().equals(EmailAccountRepository.SECURITY_SSL)) properties.put(
            "mail.smtp.ssl.enable",
            "true"
        );

        Session session = Session.getInstance(properties, null);
        message = new MimeMessage(session);

        switch (emailAccount.getServerTypeSelect()) {
            case SMTP:
                smtpTransport = (SMTPTransport) session.getTransport("smtp");
                break;
            case POP:
                smtpTransport = (SMTPTransport) session.getTransport("pop");
                break;
            case IMAP:
                smtpTransport = (SMTPTransport) session.getTransport("imap");
                break;
            default:
                throw new AvrException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    "Type serveur inconnue dans la configuration des comptes emails"
                );
        }
    }

    /**
     * Send.
     *
     * @throws MessagingException the messaging exception
     * @throws AvrException       the avr exception
     * @throws EmailException     the email exception
     */
    public void send() throws MessagingException, AvrException, EmailException {
        if (ObjectUtils.isEmpty(this.content)) throw new EmailException("No content in model template");

        if (ObjectUtils.notEmpty(entity)) {
            this.content = replaceToData(this.content);
        } else if (ObjectUtils.notEmpty(parameters)) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                try {
                    this.content =
                        this.content.replace(String.format("%s%s%s", SEPARATOR, entry.getKey(), SEPARATOR), entry.getValue());
                } catch (NullPointerException ignored) {}
            }
        } else {
            throw new AvrException(TraceBackRepository.CATEGORY_NO_VALUE, "No value found");
        }

        if (ObjectUtils.isEmpty(email_to)) {
            throw new AvrException(TraceBackRepository.CATEGORY_NO_VALUE, "email_to empty into mail template");
        }

        String strRecipients = replaceToData(email_to);
        if (ObjectUtils.isEmpty(strRecipients)) throw new AvrException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            "mail 'to' not found"
        );

        String[] recipients = strRecipients.split(SEPARATOR_ADDRESS);
        if (ObjectUtils.notEmpty(recipients)) {
            Address[] addresses = Arrays
                .stream(recipients)
                .map(
                    it -> {
                        try {
                            return new InternetAddress(it);
                        } catch (AddressException e) {
                            return null;
                        }
                    }
                )
                .toArray(InternetAddress[]::new);
            message.setRecipients(RecipientType.TO, addresses);
        }

        message.setFrom(new InternetAddress(email_from));
        message.setSubject(replaceToData(subject));

        if (multipart != null) {
            BodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDataHandler(new DataHandler(new HTMLDataSource(content)));
            multipart.addBodyPart(mimeBodyPart);
            message.setContent(multipart);
        } else message.setDataHandler(new DataHandler(new HTMLDataSource(content)));

        if (email_to_cc != null) {
            for (String cc : email_to_cc) {
                message.addRecipient(RecipientType.CC, new InternetAddress(cc));
            }
        }

        smtpTransport.connect(host, port, username, password);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        this.response = smtpTransport.getLastServerResponse();
        smtpTransport.close();
    }

    /**
     * With file email.
     *
     * @param file the file
     * @return the email
     * @throws MessagingException the messaging exception
     */
    public Email withFile(File file) throws MessagingException {
        if (multipart == null) multipart = new MimeMultipart();
        BodyPart mimeBodyPart = new MimeBodyPart();
        DataSource dataSource = new FileDataSource(file.getAbsolutePath());
        mimeBodyPart.setDataHandler(new DataHandler(dataSource));
        mimeBodyPart.setFileName(file.getName());
        multipart.addBodyPart(mimeBodyPart);
        return this;
    }

    /**
     * Put parameters email.
     *
     * @param find    the find
     * @param replace the replace
     * @return the email
     */
    public Email putParameters(String find, String replace) {
        parameters.put(find, replace);
        return this;
    }

    /**
     * With object email.
     *
     * @param obj the obj
     * @return the email
     */
    public Email withObject(Model obj) {
        entity = obj;
        return this;
    }

    /**
     * With object email.
     *
     * @param obj the obj
     * @return the email
     */
    public <T extends Model> Email withObject(Model obj, Class<T> klass) {
        T model = Query.of(klass).filter("self.id = ?", obj.getId()).fetchOne();
        ObjectUtils.isRequiredNotChecked(model, "model %s not found", klass.getSimpleName());
        return withObject(model);
    }

    /**
     * Gets response.
     *
     * @return the response
     */
    public String getResponse() {
        return response;
    }

    private String replaceToData(String content) {
        List<String> groups = RegexUtils.find(content, "\\$([^$]*)\\$");
        for (String gp : groups) {
            String[] fieldFormat = gp.split(";");
            String[] fields = fieldFormat[0].split(Pattern.quote("."));
            fields = Arrays.copyOfRange(fields, 1, fields.length);
            Object entityCurrent = this.entity;
            Mapper mapper = Mapper.of(entityCurrent.getClass());
            for (String fdCurr : fields) {
                Preconditions.checkNotNull(mapper, String.format("entity %s not found", entityCurrent.getClass().getName()));
                Property property = mapper.getProperty(fdCurr);
                Preconditions.checkNotNull(property, String.format("field %s not found", fdCurr));
                try {
                    entityCurrent = property.get(entityCurrent);
                    mapper = Mapper.of(entityCurrent.getClass());
                } catch (NullPointerException e) {
                    entityCurrent = null;
                    break;
                }
            }

            if (entityCurrent == null) entityCurrent = "";
            content = content.replace(String.format("%s%s%s", SEPARATOR, gp, SEPARATOR), entityCurrent.toString());
        }
        return content;
    }

    @NotYetImplemented
    public String replaceFormat(String content, String format) {
        /*TODO: not implemented yet*/

        //		if (format == null) return content;
        //
        //		String[] formatSplitted = format.split("=");
        //
        //		switch (formatSplitted[0].trim().toUpperCase()) {
        //			case "FORMAT":
        //				return DateFormatter.transformDate();
        //		}
        //
        throw new NotImplementedYetException();
    }

    /**
     * The type Html data source.
     */
    static class HTMLDataSource implements DataSource {

        private final String html;

        /**
         * Instantiates a new Html data source.
         *
         * @param htmlString the html string
         */
        public HTMLDataSource(String htmlString) {
            html = htmlString;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (html == null) throw new IOException("html message is null!");
            return new ByteArrayInputStream(html.getBytes());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        @Override
        public String getContentType() {
            return "text/html";
        }

        @Override
        public String getName() {
            return "HTMLDataSource";
        }
    }
}
