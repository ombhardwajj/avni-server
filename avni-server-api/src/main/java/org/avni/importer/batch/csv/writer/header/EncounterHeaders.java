package org.avni.importer.batch.csv.writer.header;

import org.avni.domain.EncounterType;

public class EncounterHeaders extends CommonEncounterHeaders implements Headers {
    public final static String subjectId = "Subject Id";

    public EncounterHeaders(EncounterType encounterType) {
        super(encounterType);
    }

    @Override
    public String[] getAllHeaders() {
        return new String[]{id, encounterTypeHeaderName, subjectId, visitDate, earliestVisitDate, maxVisitDate, encounterLocation, cancelLocation};
    }
}
