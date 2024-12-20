package org.avni.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.avni.server.application.projections.BaseProjection;
import org.avni.server.framework.hibernate.DeclarativeRuleUserType;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.springframework.data.rest.core.config.Projection;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "encounter_type")
@JsonIgnoreProperties({"operationalEncounterTypes"})
@DynamicInsert
@BatchSize(size = 100)
public class EncounterType extends OrganisationAwareEntity implements NamedEntity {
    @NotNull
    @Column
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="concept_id")
    private Concept concept;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "encounterType")
    private Set<OperationalEncounterType> operationalEncounterTypes = new HashSet<>();

    @Column(name = "encounter_eligibility_check_rule")
    private String encounterEligibilityCheckRule;

    @Column(name = "encounter_eligibility_check_declarative_rule")
    @Type(value = DeclarativeRuleUserType.class)
    private DeclarativeRule encounterEligibilityCheckDeclarativeRule;

    private Boolean active;

    @Column(name = "is_immutable")
    private boolean isImmutable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public static EncounterType create(String name) {
        EncounterType encounterType = new EncounterType();
        encounterType.setName(name);
        return encounterType;
    }

    public Set<OperationalEncounterType> getOperationalEncounterTypes() {
        return operationalEncounterTypes;
    }

    public void setOperationalEncounterTypes(Set<OperationalEncounterType> operationalEncounterTypes) {
        this.operationalEncounterTypes = operationalEncounterTypes;
    }

    @JsonIgnore
    public String getOperationalEncounterTypeName() {
        return operationalEncounterTypes.stream()
                .map(OperationalEncounterType::getName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public String getEncounterEligibilityCheckRule() {
        return encounterEligibilityCheckRule;
    }

    public void setEncounterEligibilityCheckRule(String encounterEligibilityCheckRule) {
        this.encounterEligibilityCheckRule = encounterEligibilityCheckRule;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = Optional.ofNullable(active).orElse(true);
    }

    public DeclarativeRule getEncounterEligibilityCheckDeclarativeRule() {
        return encounterEligibilityCheckDeclarativeRule;
    }

    public void setEncounterEligibilityCheckDeclarativeRule(DeclarativeRule encounterEligibilityCheckDeclarativeRule) {
        this.encounterEligibilityCheckDeclarativeRule = encounterEligibilityCheckDeclarativeRule;
    }

    public boolean isImmutable() {
        return isImmutable;
    }

    public void setImmutable(boolean immutable) {
        isImmutable = immutable;
    }
    @Projection(name = "EncounterTypeProjection", types = {EncounterType.class})
    public interface EncounterTypeProjection extends BaseProjection {
        String getName();

        String getOperationalEncounterTypeName();
    }

}
