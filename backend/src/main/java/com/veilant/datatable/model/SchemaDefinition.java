package com.veilant.datatable.model;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * A registered data source: where it came from, its inferred (flat) schema,
 * and the cached records for it (up to {@code SchemaRegistry.MAX_RECORDS}).
 *
 * "records" is marked transient so Gson omits it when this object is
 * serialized for the schema-list / register endpoints - the dataset itself
 * is only returned by the dedicated /data endpoint.
 */
public class SchemaDefinition {

    private final String id;
    private final String name;
    private final String sourceUrl;
    private final List<ColumnDefinition> columns;
    private final int recordCount;
    private final transient List<JsonObject> records;

    public SchemaDefinition(String id, String name, String sourceUrl,
                             List<ColumnDefinition> columns, List<JsonObject> records) {
        this.id = id;
        this.name = name;
        this.sourceUrl = sourceUrl;
        this.columns = columns;
        this.records = records;
        this.recordCount = records.size();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public List<JsonObject> getRecords() {
        return records;
    }
}
