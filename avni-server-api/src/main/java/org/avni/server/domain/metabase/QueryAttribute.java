package org.avni.server.domain.metabase;

public enum QueryAttribute {
    AGGREGATION("aggregation"),
    BREAKOUT("breakout"),
    FILTER("filter"),
    SOURCE_TABLE("source-table"),
    FIELD("field"),
    BASE_TYPE("base-type"),
    SOURCE_FIELD("source-field");

    private final String value;

    QueryAttribute(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
