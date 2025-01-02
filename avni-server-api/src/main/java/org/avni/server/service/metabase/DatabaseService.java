package org.avni.server.service.metabase;

import org.avni.server.dao.metabase.DatabaseRepository;
import org.avni.server.domain.metabase.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DatabaseService implements QuestionCreationService{

    private final DatabaseRepository databaseRepository;
    private final MetabaseService metabaseService;

    private static final String ADDRESS_TABLE = "Address";

    @Autowired
    public DatabaseService(DatabaseRepository databaseRepository, MetabaseService metabaseService) {
        this.databaseRepository = databaseRepository;
        this.metabaseService = metabaseService;
    }

    public Database getGlobalDatabase() {
        return metabaseService.getGlobalDatabase();
    }

    public CollectionInfoResponse getGlobalCollection() {
        return metabaseService.getGlobalCollection();
    }

    public SyncStatus getInitialSyncStatus() {
        DatabaseSyncStatus databaseSyncStatus = databaseRepository.getInitialSyncStatus(getGlobalDatabase());
        String status = databaseSyncStatus.getInitialSyncStatus();
        return SyncStatus.fromString(status);
    }

    private void ensureSyncComplete() {
        SyncStatus syncStatus = getInitialSyncStatus();
        if (syncStatus != SyncStatus.COMPLETE) {
            throw new RuntimeException("Database sync is not complete. Cannot create questions.");
        }
    }

    private List<String> filterOutExistingQuestions(List<String> entityNames) {
        Set<String> existingItemNames = databaseRepository.getExistingCollectionItems(getGlobalCollection().getIdAsInt()).stream()
                .map(item -> item.getName().trim().toLowerCase().replace(" ", "_"))
                .collect(Collectors.toSet());

        return entityNames.stream()
                .filter(entityName -> !existingItemNames.contains(entityName.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public void createQuestionForTable(TableDetails tableDetails, TableDetails addressTableDetails, FieldDetails addressFieldDetails, FieldDetails tableFieldDetails) {
        Database database = getGlobalDatabase();
        databaseRepository.createQuestionForTable(database, tableDetails, addressTableDetails, addressFieldDetails, tableFieldDetails);
    }

    @Override
    public void createQuestionForTable(String tableName) {
        Database database = getGlobalDatabase();

        TableDetails tableDetails = new TableDetails(tableName);
        TableDetails fetchedTableDetails = databaseRepository.findTableDetailsByName(database, tableDetails);

        databaseRepository.createQuestionForASingleTable(database, fetchedTableDetails);
    }

    public List<String> getSubjectTypeNames() {
        TableDetails fetchedMetadataTable = databaseRepository.findTableDetailsByName(getGlobalDatabase(), new TableDetails("table_metadata"));

        DatasetResponse datasetResponse = databaseRepository.findAll(fetchedMetadataTable, getGlobalDatabase());
        List<List<String>> rows = datasetResponse.getData().getRows();

        List<String> subjectTypeNames = new ArrayList<>();

        for (List<String> row : rows) {
            String type = row.get(DatasetColumn.TYPE.getIndex());
            String schemaName = row.get(DatasetColumn.SCHEMA_NAME.getIndex());
            if (schemaName.equalsIgnoreCase(getGlobalDatabase().getName()) &&
                    (type.equalsIgnoreCase(TableType.INDIVIDUAL.getTypeName()) ||
                            type.equalsIgnoreCase(TableType.HOUSEHOLD.getTypeName()) ||
                            type.equalsIgnoreCase(TableType.GROUP.getTypeName()) ||
                            type.equalsIgnoreCase(TableType.PERSON.getTypeName()))) {
                subjectTypeNames.add(row.get(DatasetColumn.NAME.getIndex()));
            }
        }

        return subjectTypeNames;
    }

    public List<String> getProgramAndEncounterNames() {
        TableDetails fetchedMetadataTable = databaseRepository.findTableDetailsByName(getGlobalDatabase(), new TableDetails("table_metadata"));

        DatasetResponse datasetResponse = databaseRepository.findAll(fetchedMetadataTable, getGlobalDatabase());
        List<List<String>> rows = datasetResponse.getData().getRows();

        List<String> programAndEncounterNames = new ArrayList<>();

        for (List<String> row : rows) {
            String type = row.get(DatasetColumn.TYPE.getIndex());
            String schemaName = row.get(DatasetColumn.SCHEMA_NAME.getIndex());
            if (schemaName.equalsIgnoreCase(getGlobalDatabase().getName()) &&
                    (type.equalsIgnoreCase(TableType.PROGRAM_ENCOUNTER.getTypeName()) ||
                            type.equalsIgnoreCase(TableType.ENCOUNTER.getTypeName()) ||
                            type.equalsIgnoreCase(TableType.PROGRAM_ENROLMENT.getTypeName()))) {
                programAndEncounterNames.add(row.get(DatasetColumn.NAME.getIndex()));
            }
        }

        return programAndEncounterNames;
    }

    private void createQuestionsForEntities(List<String> entityNames, FieldDetails addressFieldDetails, FieldDetails entityFieldDetails) {
        ensureSyncComplete();
        TableDetails fetchedAddressTableDetails = databaseRepository.findTableDetailsByName(getGlobalDatabase(), new TableDetails(ADDRESS_TABLE));

        List<String> filteredEntities = filterOutExistingQuestions(entityNames);

        for (String entityName : filteredEntities) {
            TableDetails entityTableDetails = new TableDetails(entityName);
            TableDetails fetchedEntityTableDetails = databaseRepository.findTableDetailsByName(getGlobalDatabase(), entityTableDetails);
            createQuestionForTable(fetchedEntityTableDetails, fetchedAddressTableDetails, addressFieldDetails, entityFieldDetails);
        }
    }

    public void createQuestionsForSubjectTypes() {
        List<String> subjectTypeNames = getSubjectTypeNames();
        FieldDetails addressFieldDetails = new FieldDetails("id");
        FieldDetails subjectFieldDetails = new FieldDetails("address_id");
        createQuestionsForEntities(subjectTypeNames, addressFieldDetails, subjectFieldDetails);
    }

    public void createQuestionsForProgramsAndEncounters() {
        List<String> programAndEncounterNames = getProgramAndEncounterNames();
        FieldDetails addressFieldDetails = new FieldDetails("id");
        FieldDetails programOrEncounterFieldDetails = new FieldDetails("address_id");
        createQuestionsForEntities(programAndEncounterNames, addressFieldDetails, programOrEncounterFieldDetails);
    }

    public void createQuestionsForIndividualTables() {
        ensureSyncComplete();
        List<String> individualTables = Arrays.asList("address", "media", "sync_telemetry");

        List<String> filteredTables = filterOutExistingQuestions(individualTables); 

        for (String tableName : filteredTables) {
            createQuestionForTable(tableName);
        }
    }

    public void createAdvancedQuestion() {
        QuestionConfig config = new QuestionConfig()
                .withAggregation(AggregationType.COUNT)
                .withBreakout("name", "subject_type_id")
                .withFilters(
                        new FilterCondition(ConditionType.EQUAL, databaseRepository.getFieldDetailsByName(metabaseService.getGlobalDatabase(), new TableDetails("individual"), new FieldDetails("is_voided")).getId() , FieldType.BOOLEAN.getTypeName(), false),
                        new FilterCondition(ConditionType.BETWEEN, databaseRepository.getFieldDetailsByName(metabaseService.getGlobalDatabase(), new TableDetails("individual"), new FieldDetails("created_date_time")).getId() , FieldType.DATETIME.getTypeName(), new String[]{"2024-11-01", "2024-12-19"})
                )
                .withVisualization(VisualizationType.BAR, VisualizationAttributes.GRAPH_DIMENSIONS, VisualizationAttributes.GRAPH_METRICS);
        MetabaseQuery query = databaseRepository.createAdvancedQuery("individual", "subject_type", config, metabaseService.getGlobalDatabase());
        databaseRepository.postQuestion(
                "Individual, Count, Grouped by Subject Type → Name, Filtered by Is Voided is false and Created Date Time is Nov 1 – Dec 19, 2024",
                query,
                config,
                metabaseService.getGlobalCollection().getIdAsInt()
        );
    }

    public void createQuestions() {
        createQuestionsForSubjectTypes();

        createQuestionsForProgramsAndEncounters();

        createQuestionsForIndividualTables();

        createAdvancedQuestion();
    }
}
