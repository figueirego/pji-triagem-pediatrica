package com.pji.triagem.service.impl;

import com.pji.triagem.dto.request.UpdateUserProfileRequest;
import com.pji.triagem.exception.InformationFoundExeption;
import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.model.TypeUser;
import com.pji.triagem.model.User;
import com.pji.triagem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void registerClientUserRejectsDuplicateCpf() {
        UserServiceImpl service = new UserServiceImpl(userRepository);
        when(userRepository.existsByLoginAndType("52998224725", TypeUser.USER)).thenReturn(true);

        assertThrows(
                InformationFoundExeption.class,
                () -> service.registerClientUser(
                        "529.982.247-25",
                        "peditriagem123",
                        TypeUser.USER,
                        "cuidador@example.com",
                        "Cuidador"
                )
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfileTrimsAndPersistsEditableFields() {
        UserServiceImpl service = new UserServiceImpl(userRepository);
        User user = new User("52998224725", "hash", TypeUser.USER, "old@example.com", "Nome Antigo");
        user.setId(1L);

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setName("  Nome Novo  ");
        request.setEmail("  novo@example.com  ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = service.updateProfile(1L, request);

        assertEquals("Nome Novo", updated.getName());
        assertEquals("novo@example.com", updated.getEmail());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void updateProfileRejectsBlankProvidedField() {
        UserServiceImpl service = new UserServiceImpl(userRepository);
        User user = new User("52998224725", "hash", TypeUser.USER, "old@example.com", "Nome Antigo");
        user.setId(1L);

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setName(" ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> service.updateProfile(1L, request));
        verify(userRepository, never()).save(any());
    }
}
