package com.loopins.core.config;

import com.midtrans.Midtrans;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Midtrans Payment Gateway integration
 */
@Slf4j
@Getter
@Configuration
public class MidtransConfig {

    @Value("${midtrans.server-key}")
    private String serverKey;

    @Value("${midtrans.client-key}")
    private String clientKey;

    @Value("${midtrans.is-production:false}")
    private boolean isProduction;

    @Value("${midtrans.merchant-id}")
    private String merchantId;

    @Value("${midtrans.callback-url}")
    private String callbackUrl;

    @PostConstruct
    public void init() {
        // Configure Midtrans global settings
        Midtrans.serverKey = serverKey;
        Midtrans.clientKey = clientKey;
        Midtrans.isProduction = isProduction;

        log.info("Midtrans initialized - Production: {}, Server Key: {}***",
                isProduction,
                serverKey != null && serverKey.length() > 10 ? serverKey.substring(0, 10) : "NOT_SET");
    }
}
