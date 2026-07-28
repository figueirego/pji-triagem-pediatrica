package com.pji.triagem.service.impl;

import com.pji.triagem.mapper.OrientationMapper;
import com.pji.triagem.model.Assessment;
import com.pji.triagem.model.Child;
import com.pji.triagem.model.User;
import com.pji.triagem.repository.AssessmentRepository;
import com.pji.triagem.repository.AssessmentSymptomRepository;
import com.pji.triagem.repository.OrientationRepository;
import com.pji.triagem.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrientationServiceImplAccessTest {

    @Mock
    private OrientationRepository orientationRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private AssessmentSymptomRepository assessmentSymptomRepository;
    @Mock
    private OrientationMapper orientationMapper;
    @Mock
    private CurrentUserService currentUserService;

    @Test
    void getOrientationRejectsAssessmentOwnedByAnotherUser() {
        OrientationServiceImpl service = new OrientationServiceImpl(
                orientationRepository,
                assessmentRepository,
                assessmentSymptomRepository,
                orientationMapper,
                currentUserService
        );
        when(assessmentRepository.findById(1L)).thenReturn(Optional.of(assessmentOwnedBy(2L)));
        doThrow(new AccessDeniedException("Acesso negado"))
                .when(currentUserService)
                .assertCanAccessUser(2L);

        assertThrows(AccessDeniedException.class, () -> service.getOrientationByAssessmentId(1L));
        verifyNoInteractions(assessmentSymptomRepository, orientationRepository);
    }

    private Assessment assessmentOwnedBy(Long userId) {
        User user = new User();
        user.setId(userId);
        Child child = new Child();
        child.setId(10L);
        child.setUser(user);
        Assessment assessment = new Assessment();
        assessment.setId(1L);
        assessment.setChild(child);
        return assessment;
    }
}
