package com.example.loanApplication.serviceImpl;
import com.example.loanApplication.dto.*;
import com.example.loanApplication.entity.Customer;
import com.example.loanApplication.entity.Role;
import com.example.loanApplication.entity.User;
import com.example.loanApplication.repository.CustomerRepository;
import com.example.loanApplication.repository.RoleRepository;
import com.example.loanApplication.repository.UserRepository;
import com.example.loanApplication.security.JwtUtils;
import com.example.loanApplication.service.AuthService;
import com.example.loanApplication.service.EmailService;
import com.example.loanApplication.service.TotpService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final TotpService totpService;
    private final InMemoryOtpService inMemoryOtpService;

    @Value("${google.client.id}")
    private String googleClientId;

    // =========================================================
    // 1. Customer Registration Flow
    // =========================================================

    @Override
    @Transactional
    public String registerCustomer(RegisterRequestDto request) {

        // Validation:
        // Monthly investment cannot exceed 30% of gross monthly salary
        BigDecimal maxAllowedInvestment =
                request.getMonthlyIncome()
                        .multiply(new BigDecimal("0.30"));

        if (request.getMonthlyInvestment()
                .compareTo(maxAllowedInvestment) > 0) {

            throw new IllegalArgumentException(
                    "Invalid Data: Monthly investment cannot exceed "
                            + "30% of gross monthly salary."
            );
        }

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {

            throw new IllegalArgumentException(
                    "Email ID is already registered."
            );
        }

        // Save Customer Record
        Customer customer =
                modelMapper.map(request, Customer.class);

        customer.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        customer.setIsEmailVerified(false);

        Customer savedCustomer =
                customerRepository.save(customer);

        // Generate 2FA Secret Key for Google Authenticator
        String totpSecret =
                totpService.generateSecretKey();

        // Fetch User / Customer Role from DB
        Role userRole =
                roleRepository.findByRoleName("User")
                                        .orElseThrow(() ->
                                                new IllegalStateException(
                                                        "Default 'User' or "
                                                                + "'Customer' Role "
                                                                + "not found in database."
                                                )
                        );

        // Create and Save User Record for Security
        User user = User.builder()
                .role(userRole)
                .customer(savedCustomer)
                .firstName(savedCustomer.getFirstName())
                .lastName(savedCustomer.getLastName())
                .email(savedCustomer.getEmail())
                .mobile(savedCustomer.getMobileNo())
                .password(savedCustomer.getPassword())
                .isTokenRevoked(false)
                .secretKey(totpSecret)
                .build();

        userRepository.save(user);

        // Generate and Save OTP in Memory
        // ConcurrentHashMap with 5-minute expiry
        String generatedOtp =
                inMemoryOtpService.generateAndSaveOtp(
                        savedCustomer.getEmail()
                );

        // Send Email via SMTP
        emailService.sendOtpEmail(
                savedCustomer.getEmail(),
                generatedOtp
        );

        return "Registration successful. OTP sent to "
                + savedCustomer.getEmail()
                + ". Authenticator Secret Key: "
                + totpSecret;
    }

    // =========================================================
    // 2. Email OTP Verification Flow
    // =========================================================


    @Override
    @Transactional
    public boolean verifyEmailOtp(VerifyEmailOtpDto request) {
        Customer customer =
                customerRepository.findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Customer record not found for email: "
                                                + request.getEmail()
                                )
                        );

        if (Boolean.TRUE.equals(
                customer.getIsEmailVerified()
        )) {

            throw new IllegalStateException(
                    "Email is already verified."
            );
        }

        // Validate OTP against In-Memory ConcurrentHashMap
        boolean isValid =
                inMemoryOtpService.validateOtp(
                        request.getEmail(),
                        request.getOtpCode()
                );

        if (!isValid) {

            throw new IllegalArgumentException(
                    "Invalid or expired OTP code."
            );
        }

        // Mark Email as Verified in DB
        customer.setIsEmailVerified(true);

        customerRepository.save(customer);

        return true;
    }


    // =========================================================
    // 3. Primary Login
    //    Phase 1: Credentials Check
    // =========================================================

    @Override
    public AuthResponseDto login(
            LoginRequestDto request
    ) {

        User user =
                userRepository.findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid Email ID or Password."
                                )
                        );

        // Verify Password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "Invalid Email ID or Password."
            );
        }

        // Verify Customer Email
        if (user.getCustomer() != null
                && !Boolean.TRUE.equals(
                user.getCustomer()
                        .getIsEmailVerified()
        )) {

            throw new IllegalStateException(
                    "Email ID is not verified. "
                            + "Please complete Email OTP verification first."
            );
        }

        // Phase 1 Success:
        // Issue Pre-Auth JWT for Phase 2 (2FA)
        String preAuthToken =
                jwtUtils.generatePreAuthToken(
                        user.getEmail(),
                        user.getRole().getRoleName()
                );

        return AuthResponseDto.builder()
                .preAuthToken(preAuthToken)
                .email(user.getEmail())
                .roleName(user.getRole().getRoleName())
                .build();
    }

    // =========================================================
    // 4. Google OAuth Login
    //    Alternative Phase 1
    // =========================================================

    @Override
    public AuthResponseDto googleLogin(
            GoogleLoginDto request
    ) {

        try {

            // 1. Verify Google ID Token cryptographically
            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(),
                            new GsonFactory()
                    )
                            .setAudience(
                                    Collections.singletonList(googleClientId)
                            )
                            .build();

            GoogleIdToken idToken =
                    verifier.verify(request.getIdToken());

            if (idToken == null) {
                throw new IllegalArgumentException(
                        "Invalid or forged Google ID Token."
                );
            }

            // 2. Extract verified email from Google payload
            String googleEmail =
                    idToken.getPayload().getEmail();

            // 3. Strict Check:
            // Email MUST exist in DB
            User user =
                    userRepository.findByEmail(googleEmail)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "No account registered with email: "
                                                    + googleEmail
                                                    + ". Please complete registration first."
                                    )
                            );

            // 4. Verify if email OTP verification was completed
            if (user.getCustomer() != null
                    && !Boolean.TRUE.equals(
                    user.getCustomer()
                            .getIsEmailVerified()
            )) {

                throw new IllegalStateException(
                        "Email is not verified. "
                                + "Please complete email verification first."
                );
            }

            // 5. Phase 1 Success:
            // Issue Pre-Auth Token for Phase 2 2FA
            String preAuthToken =
                    jwtUtils.generatePreAuthToken(
                            user.getEmail(),
                            user.getRole().getRoleName()
                    );

            return AuthResponseDto.builder()
                    .preAuthToken(preAuthToken)
                    .email(user.getEmail())
                    .roleName(user.getRole().getRoleName())
                    .build();

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Google Authentication failed: "
                            + e.getMessage()
            );
        }
    }

/*    @Override
    public AuthResponseDto googleLogin(
            GoogleLoginDto request
    ) {

        String googleEmail = request.getEmail();

        if (googleEmail == null
                || googleEmail.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Google Email is required."
            );
        }

        User user =
                userRepository.findByEmail(googleEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No account registered with email: "
                                                + googleEmail
                                                + ". Please complete registration first."
                                )
                        );

        // Generate Pre-Auth JWT
        String preAuthToken =
                jwtUtils.generatePreAuthToken(
                        user.getEmail(),
                        user.getRole().getRoleName()
                );

        return AuthResponseDto.builder()
                .preAuthToken(preAuthToken)
                .email(user.getEmail())
                .roleName(user.getRole().getRoleName())
                .build();
    }*/

    // =========================================================
    // 5. Verify 2FA TOTP
    //    Phase 2
    // =========================================================

    @Override
    @Transactional
    public AuthResponseDto verify2FA(
            Verify2faDto request
    ) {

        /*
         * Validate:
         * - Pre-Auth JWT signature
         * - Token expiry
         * - Token type = PRE_AUTH_2FA
         */
        if (!jwtUtils.validateToken(
                request.getPreAuthToken()
        )
                || !"PRE_AUTH_2FA".equals(
                jwtUtils.getClaimFromToken(
                        request.getPreAuthToken(),
                        "type"
                )
        )) {

            throw new IllegalArgumentException(
                    "Invalid or expired 2FA pre-authentication session."
            );
        }

        // Extract email from Pre-Auth Token
        String email =
                jwtUtils.getEmailFromToken(
                        request.getPreAuthToken()
                );

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found."
                                )
                        );

        // Verify 6-digit TOTP code
        // using secret key stored for the user
        boolean isTotpValid =
                totpService.verifyTotp(
                        user.getSecretKey(),
                        request.getTotpCode()
                );

        if (!isTotpValid) {

            throw new IllegalArgumentException(
                    "Invalid 2FA Code. "
                            + "Please check your Google Authenticator app."
            );
        }

        /*
         * Phase 2 Success:
         * Issue Final ACCESS and REFRESH Tokens
         */
        String accessToken =
                jwtUtils.generateAccessToken(
                        user.getEmail(),
                        user.getRole().getRoleName()
                );

        String refreshToken =
                UUID.randomUUID().toString();

        user.setRefreshToken(refreshToken);
        user.setIsTokenRevoked(false);

        userRepository.save(user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .roleName(user.getRole().getRoleName())
                .email(user.getEmail())
                .build();
    }

    // =========================================================
    // 6. Refresh Access Token Flow
    // =========================================================

    @Override
    @Transactional
    public AuthResponseDto refreshToken(
            String refreshTokenStr
    ) {

        User user =
                userRepository.findByRefreshToken(
                                refreshTokenStr
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid Refresh Token."
                                )
                        );

        // Check whether Refresh Token is revoked
        if (Boolean.TRUE.equals(
                user.getIsTokenRevoked()
        )) {

            throw new IllegalStateException(
                    "Refresh Token has been revoked. "
                            + "Please log in again."
            );
        }

        // Generate new Access Token
        String newAccessToken =
                jwtUtils.generateAccessToken(
                        user.getEmail(),
                        user.getRole().getRoleName()
                );


        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenStr)
                .roleName(user.getRole().getRoleName())
                .email(user.getEmail())
                .build();
    }

    // =========================================================
    // 7. Logout & Revoke Session
    // =========================================================

    @Override
    @Transactional
    public void logout(
            String refreshTokenStr
    ) {

        User user =
                userRepository.findByRefreshToken(
                                refreshTokenStr
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid Refresh Token."
                                )
                        );

        user.setIsTokenRevoked(true);

        userRepository.save(user);
    }
}