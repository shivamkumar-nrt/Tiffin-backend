package com.tiffin.system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class KeepAliveScheduler {

    @Value("${RENDER_EXTERNAL_URL:#{null}}")
    private String renderExternalUrl;

    @Value("${APP_URL:#{null}}")
    private String customAppUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Pings the server's public URL every 10 minutes (600,000 ms)
     * to prevent Render/Cloud free-tier instances from spinning down / going into sleep mode.
     */
    @Scheduled(fixedRate = 600000, initialDelay = 120000)
    public void pingSelf() {
        String targetUrl = null;

        if (renderExternalUrl != null && !renderExternalUrl.trim().isEmpty()) {
            targetUrl = renderExternalUrl.trim();
        } else if (customAppUrl != null && !customAppUrl.trim().isEmpty()) {
            targetUrl = customAppUrl.trim();
        }

        if (targetUrl == null) {
            log.debug("KeepAliveScheduler: No RENDER_EXTERNAL_URL or APP_URL set. Self-ping skipped.");
            return;
        }

        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            targetUrl = "https://" + targetUrl;
        }

        if (targetUrl.endsWith("/")) {
            targetUrl = targetUrl.substring(0, targetUrl.length() - 1);
        }

        String healthEndpoint = targetUrl + "/api/health";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthEndpoint))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .header("User-Agent", "Tiffin-KeepAlive-Ping/1.0")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("KeepAlive ping to {} returned status: {}", healthEndpoint, response.statusCode());
        } catch (Exception e) {
            log.warn("KeepAlive ping to {} failed: {}", healthEndpoint, e.getMessage());
        }
    }
}