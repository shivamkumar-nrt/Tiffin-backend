package com.tiffin.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiffin.system.service.WhatsAppApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WhatsAppApiServiceImpl implements WhatsAppApiService {

    @Value("${whatsapp.api.enabled:false}")
    private boolean enabled;

    @Value("${whatsapp.api.provider:generic}")
    private String provider; // meta, ultramsg, generic

    @Value("${whatsapp.api.url:#{null}}")
    private String apiUrl;

    @Value("${whatsapp.api.token:#{null}}")
    private String apiToken;

    @Value("${whatsapp.api.instance-id:#{null}}")
    private String instanceId;

    @Value("${whatsapp.api.phone-number-id:#{null}}")
    private String phoneNumberId;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    @Override
    public void sendAutoWhatsAppMessage(String phoneNumber, String messageText) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return;
        }

        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
        if (cleanPhone.length() == 10) {
            cleanPhone = "91" + cleanPhone;
        }

        // If credentials are not configured or disabled, log simulation
        if (!enabled || apiToken == null || apiToken.trim().isEmpty()) {
            log.info("[WHATSAPP AUTO-DISPATCH (SIMULATION)] To: +{}, Message: {}\n(To enable live background WhatsApp delivery, configure WHATSAPP_API_TOKEN in Render environment)",
                    cleanPhone, messageText.replace("\n", " "));
            return;
        }

        try {
            if ("meta".equalsIgnoreCase(provider) || (phoneNumberId != null && !phoneNumberId.trim().isEmpty())) {
                sendViaMetaCloudApi(cleanPhone, messageText);
            } else if ("ultramsg".equalsIgnoreCase(provider) || (instanceId != null && !instanceId.trim().isEmpty())) {
                sendViaUltraMsg(cleanPhone, messageText);
            } else {
                sendViaGenericGateway(cleanPhone, messageText);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-dispatch WhatsApp message to +{}: {}", cleanPhone, e.getMessage());
        }
    }

    private void sendViaMetaCloudApi(String phone, String text) throws Exception {
        String endpoint = "https://graph.facebook.com/v18.0/" + phoneNumberId + "/messages";
        
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", phone);
        body.put("type", "text");
        
        Map<String, String> textMap = new HashMap<>();
        textMap.put("preview_url", "false");
        textMap.put("body", text);
        body.put("text", textMap);

        String jsonPayload = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("WhatsApp message delivered to +{} via Meta Cloud API", phone);
        } else {
            log.warn("Meta Cloud API returned error (status {}): {}", response.statusCode(), response.body());
        }
    }

    private void sendViaUltraMsg(String phone, String text) throws Exception {
        String inst = instanceId != null ? instanceId : "instance";
        String endpoint = "https://api.ultramsg.com/" + inst + "/messages/chat";

        Map<String, String> body = new HashMap<>();
        body.put("token", apiToken);
        body.put("to", "+" + phone);
        body.put("body", text);

        String jsonPayload = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("WhatsApp message delivered to +{} via UltraMsg", phone);
        } else {
            log.warn("UltraMsg API returned error (status {}): {}", response.statusCode(), response.body());
        }
    }

    private void sendViaGenericGateway(String phone, String text) throws Exception {
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            log.warn("WhatsApp generic API URL is missing. Message to +{} not sent.", phone);
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("to", phone);
        body.put("phone", phone);
        body.put("message", text);

        String jsonPayload = objectMapper.writeValueAsString(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(15));

        if (apiToken != null && !apiToken.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + apiToken);
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("WhatsApp message delivered to +{} via Generic Gateway", phone);
        } else {
            log.warn("Generic Gateway returned error (status {}): {}", response.statusCode(), response.body());
        }
    }
}
