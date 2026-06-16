package com.veilant.datatable;

import com.veilant.datatable.handler.SchemasHandler;
import com.veilant.datatable.registry.SchemaRegistry;
import com.veilant.datatable.registry.SchemaValidationException;
import com.veilant.datatable.util.GsonJsonMapper;
import io.javalin.Javalin;

import java.util.Map;

/**
 * Entry point. Starts an embedded Javalin server on port 8080 - no database.
 * All registered schemas/data live in memory for the life of this process
 * (see SchemaRegistry).
 */
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        SchemaRegistry registry = new SchemaRegistry();

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new GsonJsonMapper());
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost()));
        });

        app.exception(SchemaValidationException.class, (e, ctx) ->
                ctx.status(400).json(Map.of("error", e.getMessage())));
        app.exception(Exception.class, (e, ctx) ->
                ctx.status(500).json(Map.of("error", "Internal error: " + e.getMessage())));

        new SchemasHandler(registry).register(app);

        app.start(PORT);
        System.out.println("Backend listening on http://localhost:" + PORT);
    }
}
