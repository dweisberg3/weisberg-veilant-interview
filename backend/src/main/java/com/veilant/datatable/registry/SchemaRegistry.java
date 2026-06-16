package com.veilant.datatable.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.veilant.datatable.model.ColumnDefinition;
import com.veilant.datatable.model.SchemaDefinition;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry of data sources. Fetches a URL, validates the response
 * is a flat JSON array, infers its schema and caches up to {@link #MAX_RECORDS}
 * records. Everything lives only for the lifetime of the running process.
 */
public class SchemaRegistry {

    /** Maximum number of records cached per registered schema. */
    public static final int MAX_RECORDS = 200;

    private final Map<String, SchemaDefinition> schemas = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public SchemaDefinition register(String url, String name) throws SchemaValidationException {
        JsonArray records = fetchRecords(url);

        if (records.isEmpty()) {
            throw new SchemaValidationException("API response is an empty array - nothing to register");
        }

        List<ColumnDefinition> columns = SchemaInference.inferColumns(records);

        List<JsonObject> stored = new ArrayList<>();
        int limit = Math.min(records.size(), MAX_RECORDS);
        for (int i = 0; i < limit; i++) {
            stored.add(records.get(i).getAsJsonObject());
        }

        String id = UUID.randomUUID().toString();
        String resolvedName = (name == null || name.isBlank()) ? deriveNameFromUrl(url) : name.trim();

        SchemaDefinition schema = new SchemaDefinition(id, resolvedName, url, columns, stored);
        schemas.put(id, schema);
        return schema;
    }

    public Collection<SchemaDefinition> listAll() {
        return schemas.values();
    }

    public SchemaDefinition get(String id) {
        return schemas.get(id);
    }

    private JsonArray fetchRecords(String url) throws SchemaValidationException {
        URI uri;
        try {
            uri = URI.create(url);
            if (uri.getScheme() == null || !uri.getScheme().startsWith("http")) {
                throw new IllegalArgumentException("URL must start with http:// or https://");
            }
        } catch (IllegalArgumentException e) {
            throw new SchemaValidationException("Invalid URL: " + e.getMessage());
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new SchemaValidationException("Failed to reach URL: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SchemaValidationException("Request was interrupted");
        }

        if (response.statusCode() != 200) {
            throw new SchemaValidationException("API responded with HTTP " + response.statusCode());
        }

        JsonElement root;
        try {
            root = JsonParser.parseString(response.body());
        } catch (JsonSyntaxException e) {
            throw new SchemaValidationException("API response is not valid JSON");
        }

        if (!root.isJsonArray()) {
            throw new SchemaValidationException(
                    "API response must be a JSON array of flat objects, e.g. [{\"id\":1,\"name\":\"a\"}, ...]");
        }

        return root.getAsJsonArray();
    }

    private String deriveNameFromUrl(String url) {
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            if (path == null || path.isBlank() || path.equals("/")) {
                return uri.getHost();
            }
            String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
            int lastSlash = trimmed.lastIndexOf('/');
            return lastSlash >= 0 ? trimmed.substring(lastSlash + 1) : trimmed;
        } catch (Exception e) {
            return "schema-" + System.currentTimeMillis();
        }
    }
}
