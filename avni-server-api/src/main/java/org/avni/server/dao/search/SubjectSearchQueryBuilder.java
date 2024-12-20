package org.avni.server.dao.search;

import org.avni.server.domain.SubjectType;
import org.avni.server.web.request.webapp.search.DateRange;
import org.avni.server.web.request.webapp.search.SubjectSearchRequest;
import org.springframework.stereotype.Component;

@Component
public class SubjectSearchQueryBuilder extends BaseSubjectSearchQueryBuilder<SubjectSearchQueryBuilder> implements SearchBuilder {
    public static final String SubjectTypeColumn = "                st.name as \"subjectTypeName\",\n";
    public static final String HardCodedSubjectTypeColumn = "                '$SubjectTypeName' as \"subjectTypeName\",\n";
    public static final String SubjectTypeJoin = "         left outer join subject_type st on i.subject_type_id = st.id and st.is_voided is false\n";

    public SqlQuery build(SubjectType subjectType) {
        String baseQuery = "select i.id as \"id\",\n" +
                "                i.first_name as \"firstName\",\n" +
                "                i.last_name as \"lastName\",\n" +
                "                i.profile_picture as \"profilePicture\",\n" +
                "                cast(concat_ws(' ',i.first_name,i.middle_name,i.last_name)as text) as \"fullName\",\n" +
                "                i.uuid as \"uuid\",\n" +
                "                i.address_id as \"addressId\",\n" +
                "$SubjectTypeColumn" +
                "                gender.name as \"gender\",\n" +
                "                i.date_of_birth as \"dateOfBirth\" $CUSTOM_FIELDS\n" +
                "from individual i\n" +
                "         left outer join gender on i.gender_id = gender.id\n" +
                "$SubjectTypeJoin";

        if (subjectType == null) {
            baseQuery = baseQuery.replace("$SubjectTypeColumn", SubjectTypeColumn);
            baseQuery = baseQuery.replace("$SubjectTypeJoin", SubjectTypeJoin);
        } else {
            baseQuery = baseQuery.replace("$SubjectTypeColumn", HardCodedSubjectTypeColumn.replace("$SubjectTypeName", subjectType.getName()));
            baseQuery = baseQuery.replace("$SubjectTypeJoin", "");
        }
        return super.buildUsingBaseQuery(baseQuery, "");
    }

    public SubjectSearchQueryBuilder withSubjectSearchFilter(SubjectSearchRequest request, SubjectType subjectType) {
        return this
                .withNameFilter(request.getName())
                .withConceptsFilter(request.getConcept())
                .withSearchAll(request.getSearchAll())
                .withSubjectTypeFilter(subjectType)
                .withGenderFilter(request.getGender())
                .withAgeFilter(request.getAge())
                .withRegistrationDateFilter(request.getRegistrationDate())
                .withEncounterDateFilter(request.getEncounterDate())
                .withProgramEnrolmentDateFilter(request.getProgramEnrolmentDate())
                .withProgramEncounterDateFilter(request.getProgramEncounterDate())
                .withAddressIdsFilter(request.getAddressIds())
                .withIncludeVoidedFilter(request.getIncludeVoided())
                .withPaginationFilters(request.getPageElement())
                .withCustomFields(request.getSubjectType());
    }

    public SubjectSearchQueryBuilder withSubjectTypeFilter(SubjectType subjectType) {
        if (subjectType == null) return this;
        whereClauses.add("i.subject_type_id = :subjectTypeId");
        addParameter("subjectTypeId", subjectType.getId());
        return this;
    }

    public SubjectSearchQueryBuilder withEncounterDateFilter(DateRange encounterDateRange) {
        if (encounterDateRange == null || encounterDateRange.isEmpty()) return this;
        return withRangeFilter(encounterDateRange,
                        "encounterDate",
                        "e.encounter_date_time >= cast(:rangeParam as date)",
                        "e.encounter_date_time <= cast(:rangeParam as date)", ENCOUNTER_FILTER);
    }

    public SubjectSearchQueryBuilder withProgramEncounterDateFilter(DateRange dateRange) {
        if (dateRange == null || dateRange.isEmpty()) return this;
        return withRangeFilter(dateRange,
                        "programEncounterDate",
                        "pe.encounter_date_time >= cast(:rangeParam as date)",
                        "pe.encounter_date_time <= cast(:rangeParam as date)", PROGRAM_ENCOUNTER_FILTER);
    }

    public SubjectSearchQueryBuilder withProgramEnrolmentDateFilter(DateRange dateRange) {
        if (dateRange == null || dateRange.isEmpty()) return this;
        return withRangeFilter(dateRange,
                        "programEnrolmentDate",
                        "penr.enrolment_date_time >= cast(trim(:rangeParam) as date)",
                        "penr.enrolment_date_time <= cast(trim(:rangeParam) as date)", PROGRAM_ENROLMENT_FILTER);
    }

    @Override
    public SqlQuery getSQLResultQuery(SubjectSearchRequest searchRequest, SubjectType subjectType) {
        return this.withSubjectSearchFilter(searchRequest, subjectType).build(subjectType);
    }

    @Override
    public SqlQuery getSQLCountQuery(SubjectSearchRequest searchRequest, SubjectType subjectType) {
        return this.withSubjectSearchFilter(searchRequest, subjectType).forCount().build(subjectType);
    }
}
