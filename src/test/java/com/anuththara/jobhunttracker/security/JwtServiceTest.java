package com.anuththara.jobhunttracker.security;

import com.anuththara.jobhunttracker.user.Role;
import com.anuththara.jobhunttracker.user.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3Rpbmctb25seS0xMjM0NTY3OA==";
    private static final long ONE_DAY_MS = 86_400_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", ONE_DAY_MS);
    }

    private User buildUser(String email) {
        return User.builder().email(email).password("hashed").role(Role.USER).build();
    }

    @Test
    void generateToken_validUser_returnsNonNullToken() {
        User user = buildUser("anu@test.com");

        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractEmail_fromGeneratedToken_returnsCorrectEmail() {
        User user = buildUser("anu@test.com");
        String token = jwtService.generateToken(user);

        String extractedEmail = jwtService.extractEmail(token);

        assertThat(extractedEmail).isEqualTo("anu@test.com");
    }

    @Test
    void isTokenValid_validTokenAndMatchingUser_returnsTrue() {
        User user = buildUser("anu@test.com");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_tokenForDifferentUser_returnsFalse() {
        User userA = buildUser("usera@test.com");
        User userB = buildUser("userb@test.com");
        String tokenForA = jwtService.generateToken(userA);

        assertThat(jwtService.isTokenValid(tokenForA, userB)).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        User user = buildUser("anu@test.com");
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String expiredToken = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(expiredToken, user)).isFalse();
    }

    @Test
    void extractEmail_tamperedToken_throwsJwtException() {
        assertThrows(JwtException.class, () -> jwtService.extractEmail("not.a.valid.jwt"));
    }
}