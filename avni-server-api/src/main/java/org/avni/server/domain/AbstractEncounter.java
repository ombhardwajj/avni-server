package org.avni.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.avni.server.common.dbSchema.ColumnNames;
import org.avni.server.framework.hibernate.ObservationCollectionUserType;
import org.avni.server.geo.Point;
import org.avni.server.geo.PointType;
import org.avni.server.util.DateTimeUtil;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
public class AbstractEncounter extends SyncAttributeEntity {
    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "encounter_type_id")
    private EncounterType encounterType;

    @Column
    private Instant earliestVisitDateTime;

    @Column
    private Instant maxVisitDateTime;

    @Column(name = ColumnNames.EncounterDateTime)
    private Instant encounterDateTime;

    @Column(name = ColumnNames.EncounterObservations)
    @Type(value = ObservationCollectionUserType.class)
    private ObservationCollection observations;

    @Column(name = ColumnNames.EncounterCancelDateTime)
    private Instant cancelDateTime;

    @Column(name = ColumnNames.EncounterCancelObservations)
    @Type(value = ObservationCollectionUserType.class)
    private ObservationCollection cancelObservations;

    @Type(value = PointType.class)
    @Column
    private Point encounterLocation;

    @Type(value = PointType.class)
    @Column
    private Point cancelLocation;

    @Column
    private String legacyId;

    @Column(name = "address_id")
    private Long addressId;

    @JoinColumn(name = "filled_by_id")
    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    private User filledBy;

    public EncounterType getEncounterType() {
        return encounterType;
    }
    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public DateTime getEncounterDateTime() {
        return DateTimeUtil.toJodaDateTime(encounterDateTime);
    }

    public void setEncounterDateTime(DateTime encounterDateTime, User currentUser) {
        if (this.encounterDateTime == null && encounterDateTime != null) {
            this.filledBy = currentUser;
        }
        this.encounterDateTime = DateTimeUtil.toInstant(encounterDateTime);
    }

    public ObservationCollection getObservations() {
        return observations;
    }

    public void setObservations(ObservationCollection observations) {
        this.observations = observations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getEarliestVisitDateTime() {
        return DateTimeUtil.toJodaDateTime(earliestVisitDateTime);
    }

    public void setEarliestVisitDateTime(DateTime earliestVisitDateTime) {
        this.earliestVisitDateTime = DateTimeUtil.toInstant(earliestVisitDateTime);
    }

    public DateTime getMaxVisitDateTime() {
        return DateTimeUtil.toJodaDateTime(maxVisitDateTime);
    }

    public void setMaxVisitDateTime(DateTime maxVisitDateTime) {
        this.maxVisitDateTime = DateTimeUtil.toInstant(maxVisitDateTime);
    }

    public DateTime getCancelDateTime() {
        return DateTimeUtil.toJodaDateTime(cancelDateTime);
    }

    public boolean isCancelled() {
        return cancelDateTime != null;
    }

    public void setCancelDateTime(DateTime cancelDateTime) {
        this.cancelDateTime = DateTimeUtil.toInstant(cancelDateTime);
    }

    public ObservationCollection getCancelObservations() {
        return cancelObservations;
    }

    public void setCancelObservations(ObservationCollection cancelObservations) {
        this.cancelObservations = cancelObservations;
    }

    public Point getEncounterLocation() {
        return encounterLocation;
    }

    public void setEncounterLocation(Point encounterLocation) {
        this.encounterLocation = encounterLocation;
    }

    public Point getCancelLocation() {
        return cancelLocation;
    }

    public void setCancelLocation(Point cancelLocation) {
        this.cancelLocation = cancelLocation;
    }

    public boolean isCompleted() {
        return getEncounterDateTime() != null;
    }

    public boolean matches(String encounterTypeName, String encounterName) {
        return Objects.equals(this.getEncounterType().getName(), encounterTypeName) && Objects.equals(this.getName(), encounterName);
    }

    public boolean dateFallsWithIn(DateTime encounterDateTime) {
        return encounterDateTime.isAfter(this.getEarliestVisitDateTime()) && encounterDateTime.isBefore(this.getMaxVisitDateTime());
    }

    public boolean isEncounteredOrCancelledBetween(DateTime startDate, DateTime endDate) {
        return (getEncounterDateTime() != null && isAfterOrEqual(getEncounterDateTime(), startDate) && isBeforeOrEqual(getEncounterDateTime(), endDate)) ||
                (getCancelDateTime() != null && isAfterOrEqual(getCancelDateTime(), startDate) && isBeforeOrEqual(getCancelDateTime(), endDate));
    }

    private boolean isAfterOrEqual(DateTime d1, DateTime d2) {
        return d1.equals(d2) || d1.isAfter(d2);
    }

    private boolean isBeforeOrEqual(DateTime d1, DateTime d2) {
        return d1.equals(d2) || d1.isBefore(d2);
    }

    public String getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(String legacyId) {
        this.legacyId = legacyId;
    }

    public void validate() throws ValidationException {
        if (encounterDateTime == null && earliestVisitDateTime == null) {
            throw new ValidationException("Both encounter datetime and earliest visit datetime cannot be null");
        }
    }

    @JsonIgnore
    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    @JsonIgnore
    public User getFilledBy() {
        return filledBy;
    }

    public void setFilledBy(User filledBy) {
        this.filledBy = filledBy;
    }
}
