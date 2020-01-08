package org.openchs.service;

import org.openchs.dao.ConceptAnswerRepository;
import org.openchs.dao.ConceptRepository;
import org.openchs.dao.OrganisationRepository;
import org.openchs.domain.*;
import org.openchs.util.O;
import org.openchs.web.request.ConceptContract;
import org.openchs.web.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class ConceptService {
    private final Logger logger;
    private ConceptRepository conceptRepository;
    private ConceptAnswerRepository conceptAnswerRepository;
    private OrganisationRepository organisationRepository;
    private UserService userService;

    @Autowired
    public ConceptService(ConceptRepository conceptRepository, ConceptAnswerRepository conceptAnswerRepository, OrganisationRepository organisationRepository, UserService userService) {
        this.userService = userService;
        logger = LoggerFactory.getLogger(this.getClass());
        this.conceptRepository = conceptRepository;
        this.conceptAnswerRepository = conceptAnswerRepository;
        this.organisationRepository = organisationRepository;
    }

    private Concept fetchOrCreateConcept(String uuid) {
        Concept concept = conceptRepository.findByUuid(uuid);
        if (concept == null) {
            concept = createConcept(uuid);
        }
        return concept;
    }

    private Concept createConcept(String uuid) {
        Concept concept = new Concept();
        concept.setUuid(uuid);
        return concept;
    }

    private boolean conceptExistsWithSameNameAndDifferentUUID(ConceptContract conceptRequest) {
        Concept concept = conceptRepository.findByName(conceptRequest.getName());
        return concept != null && !concept.getUuid().equals(conceptRequest.getUuid());
    }

    private ConceptAnswer fetchOrCreateConceptAnswer(Concept concept, ConceptContract answerConceptRequest, double answerOrder) throws AnswerConceptNotFoundException {
        if (StringUtils.isEmpty(answerConceptRequest.getUuid())) {
            throw new ValidationException("UUID missing for answer");
        }
        ConceptAnswer conceptAnswer = concept.findConceptAnswerByConceptUUID(answerConceptRequest.getUuid());
        if (conceptAnswer == null) {
            conceptAnswer = new ConceptAnswer();
            conceptAnswer.assignUUID();
        }
        Concept answerConcept = conceptRepository.findByUuid(answerConceptRequest.getUuid());
        if (answerConcept == null) {
            String message = String.format("Answer concept not found for UUID:%s", answerConceptRequest.getUuid());
            logger.error(message);
            throw new AnswerConceptNotFoundException(message);
        }
        updateOrganisationIfNeeded(conceptAnswer, answerConceptRequest);
        if (!conceptAnswer.editableBy(userService.getCurrentUser().getOrganisationId())) {
            return conceptAnswer;
        }
        conceptAnswer.setAnswerConcept(answerConcept);
//        conceptAnswer.setAnswerConcept(map(answerConceptRequest));
        conceptAnswer.setVoided(answerConceptRequest.isVoided());
        Double providedOrder = answerConceptRequest.getOrder();
        conceptAnswer.setOrder(providedOrder == null ? answerOrder : providedOrder);
        conceptAnswer.setAbnormal(answerConceptRequest.isAbnormal());
        conceptAnswer.setUnique(answerConceptRequest.isUnique());
        return conceptAnswer;
    }

    private Concept createCodedConcept(Concept concept, ConceptContract conceptRequest) throws AnswerConceptNotFoundException {
        List<ConceptContract> answers = (List<ConceptContract>) O.coalesce(conceptRequest.getAnswers(), new ArrayList<>());
        AtomicInteger index = new AtomicInteger(0);
        List<ConceptAnswer> conceptAnswers = new ArrayList<>();
        for (ConceptContract answerContract : answers) {
            ConceptAnswer conceptAnswer = fetchOrCreateConceptAnswer(concept, answerContract, (short) index.incrementAndGet());
            conceptAnswers.add(conceptAnswer);
        }
        concept.addAll(conceptAnswers);
        return concept;
    }

    private Concept createNumericConcept(Concept concept, ConceptContract conceptRequest) {
        concept.setHighAbsolute(conceptRequest.getHighAbsolute());
        concept.setLowAbsolute(conceptRequest.getLowAbsolute());
        concept.setHighNormal(conceptRequest.getHighNormal());
        concept.setLowNormal(conceptRequest.getLowNormal());
        concept.setUnit(conceptRequest.getUnit());
        return concept;
    }

    private String getImpliedDataType(ConceptContract conceptContract, Concept concept) {
        if (conceptContract.getDataType() == null) {
            return concept.isNew() ? ConceptDataType.NA.toString() : concept.getDataType();
        }
        return conceptContract.getDataType();
    }

    private Concept map(@NotNull ConceptContract conceptRequest) throws AnswerConceptNotFoundException {
        Concept concept = fetchOrCreateConcept(conceptRequest.getUuid());

        concept.setName(conceptRequest.getName() != null ? conceptRequest.getName() : concept.getName());
        String impliedDataType = getImpliedDataType(conceptRequest, concept);
        concept.setDataType(impliedDataType);
        concept.setVoided(conceptRequest.isVoided());
        concept.setKeyValues(conceptRequest.getKeyValues());
        updateOrganisationIfNeeded(concept, conceptRequest);
        switch (ConceptDataType.valueOf(impliedDataType)) {
            case Coded:
                concept = createCodedConcept(concept, conceptRequest);
                break;
            case Numeric:
                concept = createNumericConcept(concept, conceptRequest);
                break;
        }
        return concept;
    }

    private <OAE extends OrganisationAwareEntity> OAE updateOrganisationIfNeeded(@NotNull OAE entity, @NotNull ConceptContract conceptRequest) {
        String organisationUuid = conceptRequest.getOrganisationUUID();
        Organisation organisation = organisationRepository.findByUuid(organisationUuid);
        if (organisationUuid != null && organisation == null) {
            throw new RuntimeException(String.format("Organisation not found with uuid :'%s'", organisationUuid));
        }
        if (organisation != null) {
            entity.setOrganisationId(organisation.getId());
        }
        return entity;
    }

    private Concept saveOrUpdate(ConceptContract conceptRequest) throws AnswerConceptNotFoundException {
        if (conceptRequest == null) return null;
        if (conceptExistsWithSameNameAndDifferentUUID(conceptRequest)) {
            throw new RuntimeException(String.format("Concept %s exists with different uuid", conceptRequest.getName()));
        }
        logger.info(String.format("Creating concept: %s", conceptRequest.toString()));

        Concept concept = map(conceptRequest);
        return conceptRepository.save(concept);
    }

    public void saveOrUpdateConcepts(List<ConceptContract> conceptRequests) {
        List<ConceptContract> failedDueToAnswerConceptNotFound = new ArrayList<>();
        for (ConceptContract conceptRequest : conceptRequests) {
            try {
                saveOrUpdate(conceptRequest);
            } catch (AnswerConceptNotFoundException answerConceptNotFoundException) {
                failedDueToAnswerConceptNotFound.add(conceptRequest);
            }
        }

        //Retry
        for (ConceptContract conceptRequest : failedDueToAnswerConceptNotFound) {
            List<ConceptContract> requestAnswers = conceptRequest.getAnswers();
            try {
                for (ConceptContract requestAnswer : requestAnswers) {
                    Optional<ConceptContract> answerConcept = failedDueToAnswerConceptNotFound.stream()
                            .filter(conceptContract -> conceptContract.getUuid().equals(requestAnswer.getUuid()))
                            .findFirst();
                    if (answerConcept.isPresent()) {
                        saveOrUpdate(answerConcept.get());
                    }
                }
                saveOrUpdate(conceptRequest);
            } catch (AnswerConceptNotFoundException answerConceptNotFoundException) {
                throw new ValidationException(answerConceptNotFoundException.getMessage());
            }

        }
    }

    public Concept get(String uuid) {
        return conceptRepository.findByUuid(uuid);
    }

    public ConceptAnswer getAnswer(String conceptUUID, String conceptAnswerUUID) {
        Concept concept = this.get(conceptUUID);
        Concept answerConcept = this.get(conceptAnswerUUID);
        return conceptAnswerRepository.findByConceptAndAnswerConcept(concept, answerConcept);
    }
}
