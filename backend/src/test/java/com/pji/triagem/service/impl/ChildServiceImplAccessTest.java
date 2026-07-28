package com.pji.triagem.service.impl;

import com.pji.triagem.mapper.ChildMapper;
import com.pji.triagem.repository.AssessmentRepository;
import com.pji.triagem.repository.ChildRepository;
import com.pji.triagem.repository.UserRepository;
import com.pji.triagem.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ChildServiceImplAccessTest {

    @Mock
    private ChildRepository childRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ChildMapper childMapper;

    @Test
    void findChildrenForHomeRejectsAnotherUserIdBeforeQueryingData() {
        ChildServiceImpl service = new ChildServiceImpl(
                childRepository,
                assessmentRepository,
                userRepository,
                currentUserService,
                childMapper
        );
        doThrow(new AccessDeniedException("Acesso negado"))
                .when(currentUserService)
                .assertCanAccessUser(2L);

        assertThrows(AccessDeniedException.class, () -> service.findChildrenForHomeByUserId(2L));
        verifyNoInteractions(userRepository, childRepository, assessmentRepository);
    }
}
