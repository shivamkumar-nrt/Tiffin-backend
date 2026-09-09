package com.tiffin.system.service;

public interface WhatsAppApiService {
    /**
     * Send automatic background WhatsApp message to recipient phone number.
     * @param phoneNumber Clean 10 or 12 digit phone number (e.g. 919876543210)
     * @param messageText Formatted text message body
     */
    void sendAutoWhatsAppMessage(String phoneNumber, String messageText);
}
