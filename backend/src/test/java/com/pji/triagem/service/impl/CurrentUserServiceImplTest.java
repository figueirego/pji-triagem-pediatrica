package com.pji.triagem.service.impl;

import com.pji.triagem.dto.response.UserAuth;
import com.pji.triagem.model.TypeUser;
import com.pji.triagem.model.User;
import com.pji.triagem.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceImplTest {

    @Mock
    private UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void regularUserCannotAccessAnotherUserResource() {
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(userService);
        User currentUser = user(7L, TypeUser.USER);
        authenticate(currentUser);
        when(userService.find(7L)).thenReturn(currentUser);

        assertThrows(AccessDeniedException.class, () -> service.assertCanAccessUser(8L));
    }

    @Test
    void regularUserCanAccessOwnResource() {
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(userService);
        User currentUser = user(7L, TypeUser.USER);
        authenticate(currentUser);
        when(userService.find(7L)).thenReturn(currentUser);

        assertDoesNotThrow(() -> service.assertCanAccessUser(7L));
    }

    @Test
    void adminCanAccessAnyUserResource() {
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(userService);
        User currentUser = user(1L, TypeUser.ADMIN);
        authenticate(currentUser);
        when(userService.find(1L)).thenReturn(currentUser);

        assertDoesNotThrow(() -> service.assertCanAccessUser(99L));
    }

    private void authenticate(User user) {
        UserAuth principal = UserAuth.fromEntity(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private User user(Long id, TypeUser type) {
        User user = new User();
        user.setId(id);
        user.setCpf("52998224725");
        user.setPassword("password");
        user.setType(type);
        user.setAttemptsCount(0);
        user.setIsBlockedTemporary(false);
        user.setIsActive(true);
        user.setName("Cuidador");
        user.setEmail("cuidador@example.com");
        return user;
    }
}
