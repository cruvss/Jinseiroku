package com.cruvs.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromAddress;

    public boolean sendReminderEmail(String toEmail, String title, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("🔔 Reminder: " + title);
            helper.setText(buildHtmlContent(title, body), true);

            mailSender.send(message);
            log.info("Reminder email sent to {} — subject: {}", toEmail, title);
            return true;
        } catch (MessagingException e) {
            log.error("Failed to send reminder email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private String buildHtmlContent(String title, String body) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#EBE0D2; font-family:'Segoe UI',Roboto,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#EBE0D2; padding:40px 20px;">
                <tr>
                  <td align="center">
                    <table width="560" cellpadding="0" cellspacing="0" style="background-color:#FCF8F2; border-radius:16px; border:1px solid #D4C5B5; box-shadow:3px 3px 0px #D4C5B5; overflow:hidden;">

                      <!-- Header -->
                      <tr>
                        <td style="background-color:#CC6654; padding:28px 32px;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td style="font-size:28px; color:#FCF8F2;">🔔</td>
                              <td style="padding-left:12px;">
                                <span style="font-size:20px; font-weight:700; color:#FCF8F2; letter-spacing:0.5px;">Jinseiroku</span><br/>
                                <span style="font-size:12px; color:rgba(255,255,255,0.8); text-transform:uppercase; letter-spacing:1px;">Reminder</span>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:32px;">
                          <h2 style="margin:0 0 16px 0; font-size:22px; color:#41372E; font-weight:700;">
                            %s
                          </h2>
                          <p style="margin:0 0 24px 0; font-size:15px; color:#8A7A6A; line-height:1.6;">
                            %s
                          </p>
                          <table cellpadding="0" cellspacing="0">
                            <tr>
                              <td style="background-color:#2A231E; border-radius:8px; box-shadow:2px 2px 0px #D4C5B5;">
                                <a href="http://localhost:4200/dashboard"
                                   style="display:inline-block; padding:12px 28px; color:#FCF8F2; text-decoration:none; font-weight:600; font-size:14px;">
                                  Open Dashboard →
                                </a>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- Divider -->
                      <tr>
                        <td style="padding:0 32px;">
                          <hr style="border:none; border-top:1px solid #D4C5B5; margin:0;"/>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="padding:20px 32px 28px; text-align:center;">
                          <p style="margin:0; font-size:12px; color:#8A7A6A;">
                            You're receiving this because you have an active reminder in Jinseiroku.<br/>
                            To stop these emails, dismiss the reminder from your dashboard.
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(title, body);
    }
}