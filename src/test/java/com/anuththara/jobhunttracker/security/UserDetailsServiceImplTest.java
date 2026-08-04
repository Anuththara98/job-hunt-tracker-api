package com.anuththara.jobhunttracker.security;

import com.anuththara.jobhunttracker.user.Role;
import com.anuththara.jobhunttracker.user.User;
import com.anuththara.jobhunttracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_existingEmail_returnsUserWithCorrectUsername() {
        User user = User.builder()
                .id(1L)
                .email("anu@test.com")
                .firstName("Anuththara")
                .lastName("Kavindi")
                .password("hashed-password")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("anu@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("anu@test.com");

        assertThat(result).isEqualTo(user);
        assertThat(result.getUsername()).isEqualTo("anu@test.com");
    }

    @Test
    void loadUserByUsername_unknownEmail_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,

                () -> userDetailsService.loadUserByUsername("unknown@test.com"));
    }
}