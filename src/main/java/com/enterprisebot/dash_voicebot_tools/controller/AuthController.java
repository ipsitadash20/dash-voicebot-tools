package com.enterprisebot.dash_voicebot_tools.controller;

import com.enterprisebot.dash_voicebot_tools.dto.VerifyAccountRequest;
import com.enterprisebot.dash_voicebot_tools.dto.VerifyOtpRequest;
import com.enterprisebot.dash_voicebot_tools.entity.Account;
import com.enterprisebot.dash_voicebot_tools.repository.AccountRepository;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/tools")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    @Qualifier("otpCache")
    private Cache<String, String> otpCache;

    @Autowired
    @Qualifier("sessionCache")
    private Cache<String, Boolean> sessionCache;

    @PostMapping("/verify-account")
    public ResponseEntity<Map<String, String>> verifyAccount(@RequestBody VerifyAccountRequest req) {
        log.info("TOOL_CALL: verify-account accountNumber={}", req.getAccountNumber());

        Optional<Account> accountOpt = accountRepository.findById(req.getAccountNumber());
        if (accountOpt.isEmpty()) {
            log.info("TOOL_RESULT: verify-account result=NOT_FOUND");
            return ResponseEntity.ok(Map.of(
                    "status", "ACCOUNT_NOT_FOUND",
                    "message", "Account details do not exist in our records."
            ));
        }

        Account account = accountOpt.get();
        String otp = String.format("%04d", new Random().nextInt(10000));
        otpCache.put(req.getAccountNumber(), otp);

        log.info("MOCK_SMS: OTP {} sent to {} for account {}", otp, maskMobile(account.getMobileNumber()), req.getAccountNumber());
        log.info("TOOL_RESULT: verify-account result=OTP_SENT");

        return ResponseEntity.ok(Map.of(
                "status", "OTP_SENT",
                "message", "OTP sent successfully to your registered mobile number. Please enter it to continue."
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody VerifyOtpRequest req) {
        log.info("TOOL_CALL: verify-otp accountNumber={} sessionId={}", req.getAccountNumber(), req.getSessionId());

        String cachedOtp = otpCache.getIfPresent(req.getAccountNumber());
        if (cachedOtp == null || !cachedOtp.equals(req.getOtp())) {
            log.info("TOOL_RESULT: verify-otp result=MISMATCH");
            return ResponseEntity.ok(Map.of(
                    "status", "OTP_MISMATCH",
                    "message", "OTP does not match. Please try again."
            ));
        }

        Account account = accountRepository.findById(req.getAccountNumber()).orElseThrow();
        sessionCache.put(req.getSessionId(), true);
        otpCache.invalidate(req.getAccountNumber());

        log.info("TOOL_RESULT: verify-otp result=SUCCESS auth_verified=true sessionId={}", req.getSessionId());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", String.format("OTP validated successfully. Your current account balance is Rs. %.2f.", account.getBalance())
        ));
    }

    @GetMapping("/session-status")
    public ResponseEntity<Map<String, Boolean>> sessionStatus(@RequestParam String sessionId) {
        Boolean verified = sessionCache.getIfPresent(sessionId);
        return ResponseEntity.ok(Map.of("auth_verified", verified != null && verified));
    }

    private String maskMobile(String mobile) {
        return "XXXXXX" + mobile.substring(mobile.length() - 4);
    }
}