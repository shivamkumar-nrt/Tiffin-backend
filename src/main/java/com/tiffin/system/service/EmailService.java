package com.tiffin.system.service;

import com.tiffin.system.entity.Payment;
import com.tiffin.system.entity.TiffinRequest;
import com.tiffin.system.entity.User;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:#{null}}")
    private String senderEmail;

    @Async
    public void sendEmail(String toEmail, String subject, String htmlBody) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            return;
        }

        if (mailSender == null || senderEmail == null || senderEmail.trim().isEmpty()) {
            log.info("[EMAIL NOTIFICATION (MOCK/SIMULATION)] To: {}, Subject: {}\n(Set SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD to send real Gmail emails)", toEmail, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "Tiffin Service System");
            helper.setTo(toEmail.trim());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Email successfully sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendRequestStatusEmail(User user, TiffinRequest request) {
        if (user == null || user.getEmail() == null) return;

        boolean isApproved = "APPROVED".equalsIgnoreCase(request.getStatus().name());
        String statusColor = isApproved ? "#059669" : "#dc2626";
        String statusText = isApproved ? "APPROVED" : "REJECTED";

        String html = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;\">"
                + "<div style=\"background: linear-gradient(135deg, #059669, #0d9488); padding: 15px 20px; border-radius: 8px; color: #ffffff; text-align: center;\">"
                + "<h2 style=\"margin: 0; font-size: 20px;\">🍱 Tiffin Service Update</h2>"
                + "</div>"
                + "<p style=\"font-size: 15px; color: #334155; margin-top: 20px;\">Hello <b>" + user.getFullName() + "</b>,</p>"
                + "<p style=\"color: #475569;\">Your Tiffin Request for <b>" + request.getServiceDate() + "</b> has been updated.</p>"
                + "<div style=\"background: #f8fafc; border-left: 4px solid " + statusColor + "; padding: 15px; border-radius: 6px; margin: 20px 0;\">"
                + "<p style=\"margin: 0 0 8px 0;\"><b>Status:</b> <span style=\"color: " + statusColor + "; font-weight: bold;\">" + statusText + "</span></p>"
                + "<p style=\"margin: 0 0 8px 0;\"><b>Meal Type:</b> " + request.getTiffinType() + "</p>"
                + (request.getRejectionReason() != null ? "<p style=\"margin: 0; color: #dc2626;\"><b>Reason:</b> " + request.getRejectionReason() + "</p>" : "")
                + "</div>"
                + "<p style=\"font-size: 12px; color: #94a3b8; text-align: center;\">Tiffin Management System &bull; Automatic Notification</p>"
                + "</div>";

        sendEmail(user.getEmail(), "Tiffin Request " + statusText + " - " + request.getServiceDate(), html);
    }

    @Async
    public void sendPaymentReminderEmail(User user, BigDecimal outstandingBalance, String upiId) {
        if (user == null || user.getEmail() == null) return;

        String html = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;\">"
                + "<div style=\"background: linear-gradient(135deg, #0284c7, #0369a1); padding: 15px 20px; border-radius: 8px; color: #ffffff; text-align: center;\">"
                + "<h2 style=\"margin: 0; font-size: 20px;\">💳 Monthly Payment Reminder</h2>"
                + "</div>"
                + "<p style=\"font-size: 15px; color: #334155; margin-top: 20px;\">Hello <b>" + user.getFullName() + "</b>,</p>"
                + "<p style=\"color: #475569;\">This is a friendly reminder to settle your monthly tiffin balance before month end.</p>"
                + "<div style=\"background: #f0fdf4; border: 1px solid #bbf7d0; padding: 15px; border-radius: 8px; margin: 20px 0; text-align: center;\">"
                + "<p style=\"margin: 0; color: #166534; font-size: 14px;\">Outstanding Balance Due:</p>"
                + "<h1 style=\"margin: 5px 0 10px 0; color: #15803d; font-size: 28px;\">Rs. " + outstandingBalance + "</h1>"
                + "<p style=\"margin: 0; font-size: 13px; color: #334155;\">Payee: <b>Shivam Kumar</b> | Primary UPI: <b>" + (upiId != null ? upiId : "shivamstm01@kotak") + "</b></p>"
                + "</div>"
                + "<p style=\"color: #475569; font-size: 13px;\">After making the UPI payment, please log into your customer portal and submit your 12-digit UTR/Reference number for instant verification.</p>"
                + "<p style=\"font-size: 12px; color: #94a3b8; text-align: center;\">Tiffin Management System &bull; Automatic Notification</p>"
                + "</div>";

        sendEmail(user.getEmail(), "Tiffin Monthly Payment Reminder (Due: Rs. " + outstandingBalance + ")", html);
    }

    @Async
    public void sendPaymentApprovalEmail(User user, Payment payment) {
        if (user == null || user.getEmail() == null) return;

        String html = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;\">"
                + "<div style=\"background: linear-gradient(135deg, #059669, #10b981); padding: 15px 20px; border-radius: 8px; color: #ffffff; text-align: center;\">"
                + "<h2 style=\"margin: 0; font-size: 20px;\">✅ Payment Verified & Approved</h2>"
                + "</div>"
                + "<p style=\"font-size: 15px; color: #334155; margin-top: 20px;\">Hello <b>" + user.getFullName() + "</b>,</p>"
                + "<p style=\"color: #475569;\">We have successfully verified and received your tiffin payment settlement.</p>"
                + "<div style=\"background: #f8fafc; border: 1px solid #e2e8f0; padding: 15px; border-radius: 8px; margin: 20px 0;\">"
                + "<p style=\"margin: 0 0 6px 0;\"><b>Amount Received:</b> Rs. " + payment.getAmount() + "</p>"
                + "<p style=\"margin: 0 0 6px 0;\"><b>UTR / Transaction Ref:</b> " + (payment.getTransactionRef() != null ? payment.getTransactionRef() : "N/A") + "</p>"
                + "<p style=\"margin: 0 0 6px 0;\"><b>Payment App:</b> " + (payment.getPaymentApp() != null ? payment.getPaymentApp() : "UPI") + "</p>"
                + "<p style=\"margin: 0;\"><b>Status:</b> <span style=\"color: #059669; font-weight: bold;\">VERIFIED & COMPLETED</span></p>"
                + "</div>"
                + "<p style=\"color: #475569; font-size: 13px;\">Your customer account balance has been updated accordingly. Thank you for your payment!</p>"
                + "<p style=\"font-size: 12px; color: #94a3b8; text-align: center;\">Tiffin Management System &bull; Official Receipt</p>"
                + "</div>";

        sendEmail(user.getEmail(), "Payment Approved (Rs. " + payment.getAmount() + ") - Receipt", html);
    }

    @Async
    public void sendNewRequestAlertToAdmin(String adminEmail, User user, TiffinRequest request) {
        if (adminEmail == null || adminEmail.trim().isEmpty()) return;

        String html = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;\">"
                + "<div style=\"background: linear-gradient(135deg, #059669, #0d9488); padding: 15px 20px; border-radius: 8px; color: #ffffff; text-align: center;\">"
                + "<h2 style=\"margin: 0; font-size: 20px;\">🔔 New Tiffin Order Received</h2>"
                + "</div>"
                + "<p style=\"font-size: 15px; color: #334155; margin-top: 20px;\">Hello <b>Admin</b>,</p>"
                + "<p style=\"color: #475569;\">A new tiffin request has been submitted by customer <b>" + (user != null ? user.getFullName() : "Customer") + "</b>.</p>"
                + "<div style=\"background: #f8fafc; border-left: 4px solid #059669; padding: 15px; border-radius: 6px; margin: 20px 0;\">"
                + "<p style=\"margin: 0 0 6px 0;\"><b>Customer Name:</b> " + (user != null ? user.getFullName() : "N/A") + "</p>"
                + "<p style=\"margin: 0 0 6px 0;\"><b>Phone:</b> " + (user != null && user.getPhone() != null ? user.getPhone() : "N/A") + "</p>"
                + "<p style=\"margin: 0 0 6px 0;\"><b>Service Date:</b> " + request.getServiceDate() + "</p>"
                + "<p style=\"margin: 0 0 6px 0;\"><b>Meal Type:</b> " + request.getTiffinType() + "</p>"
                + (request.getSpecialInstructions() != null ? "<p style=\"margin: 0;\"><b>Instructions:</b> " + request.getSpecialInstructions() + "</p>" : "")
                + "</div>"
                + "<p style=\"color: #475569; font-size: 13px;\">Please log into the Admin Console to review and approve this order.</p>"
                + "<p style=\"font-size: 12px; color: #94a3b8; text-align: center;\">Tiffin Management System &bull; Admin Alert</p>"
                + "</div>";

        sendEmail(adminEmail, "🔔 New Tiffin Request: " + (user != null ? user.getFullName() : "Customer") + " (" + request.getServiceDate() + ")", html);
    }

    @Async
    public void sendNewPaymentAlertToAdmin(String adminEmail, User user, Payment payment) {
        if (adminEmail == null || adminEmail.trim().isEmpty()) return;

        String html = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;\">"
                + "<div style=\"background: linear-gradient(135deg, #0284c7, #0369a1); padding: 15px 20px; border-radius: 8px; color: #ffffff; text-align: center;\">"
                + "<h2 style=\"margin: 0; font-size: 20px;\">💰 New Payment Submitted (Pending Verification)</h2>"
                + "</div>"
                + "<p style=\"font-size: 15px; color: #334155; margin-top: 20px;\">Hello <b>Admin</b>,</p>"
                + "<p style=\"color: #475569;\">A payment settlement has been submitted by customer <b>" + (user != null ? user.getFullName() : "Customer") + "</b>.</p>"
                + "<div style=\"background: #f8fafc; border-left: 4px solid #0284c7; padding: 15px; border-radius: 6px; margin: 20px 0;\">"
                + "<p style=\"margin: 0 0 6px 0;\"><b>Amount Paid:</b> Rs. " + payment.getAmount() + "</p>"
                + "<p style=\"margin: 0 0 6px 0;\"><b>UTR / Transaction Ref:</b> " + (payment.getTransactionRef() != null ? payment.getTransactionRef() : "N/A") + "</p>"
                + "<p style=\"margin: 0 0 6px 0;\"><b>Payment App:</b> " + (payment.getPaymentApp() != null ? payment.getPaymentApp() : "UPI") + "</p>"
                + "<p style=\"margin: 0;\"><b>Customer:</b> " + (user != null ? user.getFullName() + " (" + user.getEmail() + ")" : "N/A") + "</p>"
                + "</div>"
                + "<p style=\"color: #475569; font-size: 13px;\">Please check bank account and verify/approve in Admin Console.</p>"
                + "<p style=\"font-size: 12px; color: #94a3b8; text-align: center;\">Tiffin Management System &bull; Admin Alert</p>"
                + "</div>";

        sendEmail(adminEmail, "💰 New Payment Submitted: Rs. " + payment.getAmount() + " from " + (user != null ? user.getFullName() : "Customer"), html);
    }
}