package com.veilant.datatable.handler;

import com.google.gson.JsonSyntaxException;
import com.veilant.datatable.model.SchemaDefinition;
import com.veilant.datatable.registry.SchemaRegistry;
import com.veilant.datatable.registry.SchemaValidationException;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;

/**
 * Routes for /api/schemas:
 *
 *   GET  /api/schemas            -> list registered schemas (id, name, columns)
 *   POST /api/schemas            -> register a new schema from {"url": "...", "name": "..."}
 *   GET  /api/schemas/{id}/data  -> the cached records for that schema
 */
public class SchemasHandler {

    private final SchemaRegistry registry;

    public SchemasHandler(SchemaRegistry registry) {
        this.registry = registry;
    }

    public void register(Javalin app) {
        app.get("/api/schemas", ctx -> ctx.json(registry.listAll()));
        app.post("/api/schemas", this::handleRegister);
        app.get("/api/schemas/{id}/data", this::handleGetData);
    }

    private void handleRegister(Context ctx) throws SchemaValidationException {
        RegisterRequest body;
        try {
            body = ctx.bodyAsClass(RegisterRequest.class);
        } catch (JsonSyntaxException | IllegalStateException e) {
            throw new SchemaValidationException("Request body must be a JSON object with a 'url' field");
        }

        if (body == null || body.url() == null || body.url().isBlank()) {
            throw new SchemaValidationException("Request must include a non-empty 'url' field");
        }

        SchemaDefinition schema = registry.register(body.url().trim(), body.name());
        ctx.status(201).json(schema);
    }

    private void handleGetData(Context ctx) {
        String id = ctx.pathParam("id");
        SchemaDefinition schema = registry.get(id);
        if (schema == null) {
            ctx.status(404).json(Map.of("error", "Unknown schema id: " + id));
            return;
        }
        ctx.json(Map.of("records", schema.getRecords()));
    }

    private record RegisterRequest(String url, String name) {
    }
}
