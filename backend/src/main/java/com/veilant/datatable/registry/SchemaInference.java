package com.veilant.datatable.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.veilant.datatable.model.ColumnDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Derives a flat column list from a sample of JSON records, and rejects
 * any dataset that isn't flat (i.e. any field whose value is itself a
 * JSON object or array).
 */
public final class SchemaInference {

    /** Only the first N records are inspected to keep registration fast. */
    private static final int SAMPLE_SIZE = 20;

    private static final String TYPE_STRING = "string";
    private static final String TYPE_NUMBER = "number";
    private static final String TYPE_BOOLEAN = "boolean";
    private static final String TYPE_UNKNOWN = "unknown";

    private SchemaInference() {
    }

    public static List<ColumnDefinition> inferColumns(JsonArray records) throws SchemaValidationException {
        // LinkedHashMap preserves the field order in which columns are first seen.
        Map<String, String> columnTypes = new LinkedHashMap<>();

        int sampleSize = Math.min(records.size(), SAMPLE_SIZE);
        for (int i = 0; i < sampleSize; i++) {
            JsonElement element = records.get(i);
            if (!element.isJsonObject()) {
                throw new SchemaValidationException(
                        "Array element at index " + i + " is not a JSON object");
            }

            JsonObject record = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : record.entrySet()) {
                String field = entry.getKey();
                JsonElement value = entry.getValue();

                if (value.isJsonObject() || value.isJsonArray()) {
                    throw new SchemaValidationException(
                            "Schema must be flat: field '" + field
                                    + "' contains a nested object or array");
                }

                String type = inferType(value);
                // Keep the first non-null type we see for a field; a later
                // null shouldn't downgrade an already-known type.
                columnTypes.merge(field, type,
                        (existing, incoming) -> TYPE_UNKNOWN.equals(existing) ? incoming : existing);
            }
        }

        List<ColumnDefinition> columns = new ArrayList<>(columnTypes.size());
        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            String type = TYPE_UNKNOWN.equals(entry.getValue()) ? TYPE_STRING : entry.getValue();
            columns.add(new ColumnDefinition(entry.getKey(), type));
        }
        return columns;
    }

    private static String inferType(JsonElement value) {
        if (value.isJsonNull()) {
            return TYPE_UNKNOWN;
        }
        var primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return TYPE_BOOLEAN;
        }
        if (primitive.isNumber()) {
            return TYPE_NUMBER;
        }
        return TYPE_STRING;
    }
}
