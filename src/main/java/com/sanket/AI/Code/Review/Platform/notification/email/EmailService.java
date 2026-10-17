package com.sanket.AI.Code.Review.Platform.notification.email;

import java.util.Map;

public interface EmailService {

    /**
     * Sends plain/HTML notification email to recipient.
     */
    void sendNotificationEmail(String toEmail, String subject, String content);

    /**
     * Renders template with dynamic variables and dispatches email.
     */
    void sendTemplatedEmail(String toEmail, String templateCode, Map<String, String> variables);
}
