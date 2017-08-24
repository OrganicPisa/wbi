package com.broadcom.wbi.util;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailUtil {

    public static boolean isEmailFormat(String valueToValidate) throws IOException {
        // Regex  
        String regexExpression = "([A-Za-z0-9\\.\\_\\-]+[\\.\\_\\-]*[A-Za-z0-9\\.\\_\\-]*)+@([A-Za-z0-9\\.\\_\\-]+[\\.]*[A-Za-z0-9\\.\\_\\-]+)+\\.[A-Za-z]+";
        Pattern regexPattern = Pattern.compile(regexExpression);
        boolean valid = false;
        if (valueToValidate != null) {
            if (valueToValidate.indexOf("@") <= 0) {
                return false;
            }
            Matcher matcher = regexPattern.matcher(valueToValidate);
            valid = matcher.matches();
        } else { // The case of empty Regex expression must be accepted
            Matcher matcher = regexPattern.matcher("");
            valid = matcher.matches();
        }
        return valid;
    }

    public static void sendEmail(String from, String to, String title, String content) {
        Properties prop = System.getProperties();
        String host = "smtphost.sj.broadcom.com";
        prop.setProperty("mail.smtp.host", host);
        Session session = Session.getInstance(prop, null);
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(title);

            msg.setContent(content, "text/html");

            Transport.send(msg);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    public void sendAttachmentEmail(String from, String to, String title, String fname, String content) {
        Properties prop = System.getProperties();
        String host = "smtphost.sj.broadcom.com";
        prop.setProperty("mail.smtp.host", host);
        String contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (fname.endsWith("pptx")) {
            contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (fname.endsWith("ppt")) {
            contentType = "application/vnd.ms-powerpoint";
        } else if (fname.endsWith("xls")) {
            contentType = "application/vnd.ms-excel";
        } else if (fname.endsWith("xlsx")) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fname.endsWith("mpx")) {
            contentType = "application/x-project";
        } else if (fname.endsWith("mpp")) {
            contentType = "application/vnd.ms-project";
        }
        Session session = Session.getInstance(prop, null);
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(from));
            msg.setSubject(title);
            if (fname != null) {
                Multipart multiPart = new MimeMultipart();

                BodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(content, "text/html");
                multiPart.addBodyPart(htmlPart);
                BodyPart attachmentPart = new MimeBodyPart();
                File file = new File(fname);
                try {
                    FileInputStream is = new FileInputStream(file);
                    DataSource source = new ByteArrayDataSource(is, contentType);
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(file.getName());
                    multiPart.addBodyPart(attachmentPart);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                msg.setContent(multiPart);
            }
            Transport.send(msg);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
