package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.service.TotpService;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;

import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;

import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import org.springframework.stereotype.Service;

@Service
public class TotpServiceImpl implements TotpService {

    private final SecretGenerator secretGenerator =
            new DefaultSecretGenerator();

    private final TimeProvider timeProvider =
            new SystemTimeProvider();

    private final CodeVerifier codeVerifier =
            new DefaultCodeVerifier(
                    new DefaultCodeGenerator(
                            HashingAlgorithm.SHA1
                    ),
                    timeProvider
            );

    @Override
    public String generateSecretKey() {
        return secretGenerator.generate();
    }

    @Override
    public boolean verifyTotp(
            String secretKey,
            String totpCode
    ) {

        if (totpCode == null
                || totpCode.trim().length() != 6) {
            return false;
        }

        return codeVerifier.isValidCode(
                secretKey,
                totpCode.trim()
        );
    }
}
