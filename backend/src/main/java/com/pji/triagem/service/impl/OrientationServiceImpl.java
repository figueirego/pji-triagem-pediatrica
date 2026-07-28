package com.pji.triagem.service.impl;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.base.service.impl.BaseServiceImpl;
import com.pji.triagem.dto.response.OrientationItemResponse;
import com.pji.triagem.dto.response.OrientationResponse;
import com.pji.triagem.dto.response.OrientationSymptomResponse;
import com.pji.triagem.exception.ResourceNotFoundException;
import com.pji.triagem.mapper.OrientationMapper;
import com.pji.triagem.model.Assessment;
import com.pji.triagem.model.AssessmentSymptom;
import com.pji.triagem.model.Classification;
import com.pji.triagem.model.Orientation;
import com.pji.triagem.model.OrientationType;
import com.pji.triagem.repository.AssessmentRepository;
import com.pji.triagem.repository.AssessmentSymptomRepository;
import com.pji.triagem.repository.OrientationRepository;
import com.pji.triagem.service.CurrentUserService;
import com.pji.triagem.service.OrientationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class OrientationServiceImpl extends BaseServiceImpl<Orientation> implements OrientationService {

    private static final String DEFAULT_DISCLAIMER = "Este aplicativo não substitui avaliação médica profissional.";

    private final OrientationRepository orientationRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentSymptomRepository assessmentSymptomRepository;
    private final OrientationMapper orientationMapper;
    private final CurrentUserService currentUserService;

    @Override
    protected BaseRepository<Orientation, Long> getRepository() {
        return orientationRepository;
    }

    @Override
    protected String getResourceName() {
        return "Orientação";
    }

    @Override
    @Transactional(readOnly = true)
    public OrientationResponse getOrientationByAssessmentId(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado", "Avaliação"));
        currentUserService.assertCanAccessUser(assessment.getChild().getUser().getId());

        List<AssessmentSymptom> assessmentSymptoms = assessmentSymptomRepository.findByAssessmentIdOrderByIdAsc(assessmentId);
        List<Long> symptomIds = assessmentSymptoms.stream()
                .map(assessmentSymptom -> assessmentSymptom.getSymptom().getId())
                .distinct()
                .toList();

        List<Orientation> generalOrientations = orientationRepository
                .findByActiveTrueAndClassificationAndSymptomIsNullOrderByOrderAsc(assessment.getFinalClassification());
        List<Orientation> specificOrientations = symptomIds.isEmpty()
                ? List.of()
                : orientationRepository.findByActiveTrueAndClassificationAndSymptomIdInOrderByOrderAsc(
                        assessment.getFinalClassification(),
                        symptomIds
                );

        List<Orientation> orientations = Stream.concat(generalOrientations.stream(), specificOrientations.stream())
                .sorted(Comparator.comparing(Orientation::getOrder).thenComparing(Orientation::getId))
                .toList();

        Orientation summary = findFirstByType(orientations, OrientationType.SUMMARY);
        Orientation disclaimer = findFirstByType(orientations, OrientationType.DISCLAIMER);

        return OrientationResponse.builder()
                .assessmentId(assessment.getId())
                .childId(assessment.getChild().getId())
                .childName(assessment.getChild().getName())
                .finalClassification(assessment.getFinalClassification())
                .title(resolveSummaryTitle(summary, assessment.getFinalClassification()))
                .description(resolveSummaryDescription(summary, assessment.getFinalClassification()))
                .mainActions(toItems(orientations, OrientationType.MAIN_ACTION))
                .warningSigns(toItems(orientations, OrientationType.WARNING_SIGN))
                .homeCare(toItems(orientations, OrientationType.HOME_CARE))
                .whenSeekHelp(toItems(orientations, OrientationType.WHEN_SEEK_HELP))
                .symptoms(toSymptomResponses(assessmentSymptoms))
                .disclaimer(resolveDisclaimer(disclaimer))
                .build();
    }

    private List<OrientationItemResponse> toItems(List<Orientation> orientations, OrientationType type) {
        return orientations.stream()
                .filter(orientation -> type.equals(orientation.getType()))
                .map(orientationMapper::toItemResponse)
                .toList();
    }

    private List<OrientationSymptomResponse> toSymptomResponses(List<AssessmentSymptom> assessmentSymptoms) {
        return assessmentSymptoms.stream()
                .map(orientationMapper::toSymptomResponse)
                .toList();
    }

    private Orientation findFirstByType(List<Orientation> orientations, OrientationType type) {
        return orientations.stream()
                .filter(orientation -> type.equals(orientation.getType()))
                .findFirst()
                .orElse(null);
    }

    private String resolveSummaryTitle(Orientation summary, Classification classification) {
        if (summary != null && summary.getTitle() != null && !summary.getTitle().isBlank()) {
            return summary.getTitle();
        }
        return switch (classification) {
            case LOW -> "Baixo risco";
            case MOD -> "Risco moderado";
            case HIGH -> "Alto risco";
        };
    }

    private String resolveSummaryDescription(Orientation summary, Classification classification) {
        if (summary != null && summary.getDescription() != null && !summary.getDescription().isBlank()) {
            return summary.getDescription();
        }
        return switch (classification) {
            case LOW -> "Observe a evolução dos sintomas em casa.";
            case MOD -> "Acompanhe de perto e considere procurar atendimento.";
            case HIGH -> "Procure atendimento imediatamente.";
        };
    }

    private String resolveDisclaimer(Orientation disclaimer) {
        if (disclaimer != null && disclaimer.getDescription() != null && !disclaimer.getDescription().isBlank()) {
            return disclaimer.getDescription();
        }
        return DEFAULT_DISCLAIMER;
    }
}
