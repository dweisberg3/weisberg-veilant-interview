package com.veilant.datatable.util;

import com.google.gson.Gson;
import io.javalin.json.JsonMapper;

import java.lang.reflect.Type;

/**
 * Routes Javalin's ctx.json()/bodyAsClass() through the same Gson instance the
 * rest of the codebase uses, so transient fields (e.g. SchemaDefinition's
 * cached records) and JsonObject values keep serializing the same way.
 */
public class GsonJsonMapper implements JsonMapper {

    private final Gson gson = new Gson();

    @Override
    public String toJsonString(Object obj, Type type) {
        return gson.toJson(obj, type);
    }

    @Override
    public <T> T fromJsonString(String json, Type targetType) {
        return gson.fromJson(json, targetType);
    }
}
