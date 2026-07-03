package com.enterprisebot.dash_voicebot_tools.repository;

import com.enterprisebot.dash_voicebot_tools.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}