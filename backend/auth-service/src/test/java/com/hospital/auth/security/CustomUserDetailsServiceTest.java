package com.hospital.auth.security;

import com.hospital.auth.entity.Role;
import com.hospital.auth.entity.User;
import com.hospital.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Test
    void loads_user_with_role_authority() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByEmail("a@b.c"))
                .thenReturn(Optional.of(User.builder().id(1L).email("a@b.c").password("hash").role(Role.ADMIN).build()));

        UserDetails details = new CustomUserDetailsService(repo).loadUserByUsername("a@b.c");

        assertThat(details.getUsername()).isEqualTo("a@b.c");
        assertThat(details.getPassword()).isEqualTo("hash");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void throws_when_user_missing() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByEmail("nope@x.com")).thenReturn(Optional.empty());

        CustomUserDetailsService service = new CustomUserDetailsService(repo);

        assertThatThrownBy(() -> service.loadUserByUsername("nope@x.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("nope@x.com");
    }
}
