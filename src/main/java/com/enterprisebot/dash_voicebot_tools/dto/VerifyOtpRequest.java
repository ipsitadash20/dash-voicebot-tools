package com.enterprisebot.dash_voicebot_tools.dto;

public class VerifyOtpRequest {
    private String accountNumber;
    private String otp;
    private String sessionId;

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}