package com.veilant.datatable.model;

/**
 * A single column in a dynamically inferred schema.
 * "type" is one of "string", "number" or "boolean" and is derived from the
 * JSON values seen for this field while sampling the source dataset.
 */
public class ColumnDefinition {

    private final String field;
    private final String type;

    public ColumnDefinition(String field, String type) {
        this.field = field;
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public String getType() {
        return type;
    }
}
