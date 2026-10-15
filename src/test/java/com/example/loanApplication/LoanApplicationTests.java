package com.example.loanApplication;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LoanApplicationTests {

    @Test
    void contextLoads() {
        // The test profile uses an in-memory database, so MySQL is not required for this smoke test.
    }
}
