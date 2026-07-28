package com.pji.triagem.service.impl;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.base.service.impl.BaseServiceImpl;
import com.pji.triagem.dto.request.CreateChildRequest;
import com.pji.triagem.dto.response.ChildHomeResponse;
import com.pji.triagem.dto.response.ChildResponse;
import com.pji.triagem.exception.ResourceNotFoundException;
import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.mapper.ChildMapper;
import com.pji.triagem.model.Child;
import com.pji.triagem.model.User;
import com.pji.triagem.repository.AssessmentRepository;
import com.pji.triagem.repository.ChildRepository;
import com.pji.triagem.repository.UserRepository;
import com.pji.triagem.service.ChildService;
import com.pji.triagem.service.CurrentUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ChildServiceImpl extends BaseServiceImpl<Child> implements ChildService {

    private static final String DEFAULT_AVATAR_EMOJI = "🌸";
    private static final Set<String> ALLOWED_AVATAR_EMOJIS = Set.of("🌸", "🌱", "⭐", "🐻", "🦊", "🌈");

    private final ChildRepository childRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ChildMapper childMapper;

    @Override
    protected BaseRepository<Child, Long> getRepository() {
        return childRepository;
    }

    @Override
    protected String getResourceName() {
        return "Criança";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChildHomeResponse> findChildrenForHomeByUserId(Long userId) {
        requireAccessibleUser(userId);

        return childRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Child::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Child::getId))
                .map(this::toHomeResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChildResponse createChild(Long userId, CreateChildRequest request) {
        User user = requireAccessibleUser(userId);
        validateRequest(request);

        Child child = childMapper.toEntity(request);
        child.setUser(user);
        child.setName(request.getName().trim());
        child.setCpf(blankToNull(request.getCpf()));
        child.setAvatarEmoji(resolveAvatarEmoji(request.getAvatarEmoji()));

        return toChildResponse(save(child));
    }

    @Override
    @Transactional
    public ChildResponse updateChild(Long userId, Long childId, CreateChildRequest request) {
        requireAccessibleUser(userId);
        validateRequest(request);

        Child child = findByIdAndUserIdEntity(childId, userId);
        child.setName(request.getName().trim());
        child.setCpf(blankToNull(request.getCpf()));
        child.setBirthDate(request.getBirthDate());
        child.setWeightKg(request.getWeightKg());
        child.setAvatarEmoji(resolveAvatarEmoji(request.getAvatarEmoji()));

        return toChildResponse(save(child));
    }

    @Override
    @Transactional
    public void deleteChild(Long userId, Long childId) {
        requireAccessibleUser(userId);
        Child child = findByIdAndUserIdEntity(childId, userId);
        childRepository.delete(child);
    }

    @Override
    @Transactional(readOnly = true)
    public ChildResponse findByIdAndUserId(Long childId, Long userId) {
        requireAccessibleUser(userId);
        return toChildResponse(findByIdAndUserIdEntity(childId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Child findAccessibleEntity(Long id) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUserService.isAdmin(currentUser)) {
            return childRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado", getResourceName()));
        }

        return childRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado", getResourceName()));
    }

    private Child findByIdAndUserIdEntity(Long childId, Long userId) {
        return childRepository.findByIdAndUserId(childId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado", getResourceName()));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado", "Usuário"));
    }

    private User requireAccessibleUser(Long userId) {
        currentUserService.assertCanAccessUser(userId);
        return requireUser(userId);
    }

    private void validateRequest(CreateChildRequest request) {
        if (request == null) {
            throw new ValidationException("Dados da criança são obrigatórios");
        }
        if (request.getBirthDate() == null) {
            throw new ValidationException("Data de nascimento da criança é obrigatória");
        }
        if (request.getBirthDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Data de nascimento não pode estar no futuro");
        }
        if (Period.between(request.getBirthDate(), LocalDate.now()).getYears() > 12) {
            throw new ValidationException("Criança deve ter até 12 anos");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String resolveAvatarEmoji(String avatarEmoji) {
        String resolved = avatarEmoji == null || avatarEmoji.isBlank() ? DEFAULT_AVATAR_EMOJI : avatarEmoji.trim();
        if (!ALLOWED_AVATAR_EMOJIS.contains(resolved)) {
            throw new ValidationException("Avatar emoji inválido");
        }
        return resolved;
    }

    private ChildResponse toChildResponse(Child child) {
        ChildResponse response = childMapper.toResponse(child);
        populateAgeFields(response, child.getBirthDate());
        return response;
    }

    private ChildHomeResponse toHomeResponse(Child child) {
        ChildHomeResponse response = childMapper.toHomeResponse(child);
        populateAgeFields(response, child.getBirthDate());
        response.setTotalAssessments(assessmentRepository.countByChildId(child.getId()));
        response.setLastAssessmentAt(
                assessmentRepository.findLastAssessmentDateByChildId(child.getId()).orElse(null)
        );
        return response;
    }

    private void populateAgeFields(ChildResponse response, LocalDate birthDate) {
        int ageInMonths = calculateAgeInMonths(birthDate);
        response.setAgeInMonths(ageInMonths);
        response.setAge(formatAge(ageInMonths));
    }

    private void populateAgeFields(ChildHomeResponse response, LocalDate birthDate) {
        int ageInMonths = calculateAgeInMonths(birthDate);
        response.setAgeInMonths(ageInMonths);
        response.setAge(formatAge(ageInMonths));
    }

    private int calculateAgeInMonths(LocalDate birthDate) {
        Period period = Period.between(birthDate, LocalDate.now());
        return (period.getYears() * 12) + period.getMonths();
    }

    private String formatAge(int totalMonths) {
        int years = totalMonths / 12;
        int months = totalMonths % 12;

        if (years <= 0) {
            return months + (months == 1 ? " mês" : " meses");
        }

        if (months == 0) {
            return years + (years == 1 ? " ano" : " anos");
        }

        return years + (years == 1 ? " ano" : " anos")
                + " e "
                + months + (months == 1 ? " mês" : " meses");
    }
}
