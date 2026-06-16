# Dynamic Data Table Viewer

A small full-stack demo: a Java backend that registers external REST APIs,
infers a flat schema from their data, and caches a sample of records in
memory; and an Angular + PrimeNG frontend that lets a user register a source,
pick it from a dropdown, and load its data into a dynamic, paginated table.

## How it works

1. User pastes an API URL (must return a JSON array of flat objects - no
   nested objects/arrays) into the registration form and clicks **Register**.
2. The backend fetches the URL, validates the data is flat, infers column
   names + types from the first ~20 records, and caches up to 200 records
   in memory. It returns a schema `{ id, name, columns, recordCount }`.
3. The new schema's name appears in the **View data** dropdown.
4. Selecting a schema immediately renders the table's column headers (from
   `columns`), with no rows yet.
5. Clicking **Get Data Now** fetches the cached records for that schema and
   populates the table, which paginates client-side via PrimeNG's `p-table`.

There is no database - everything lives in memory in the running backend
process and is lost on restart.

### Design note: schema is inferred, not pre-declared

Real public APIs return plain JSON arrays, not a `{schema, data}` envelope.
Rather than requiring a custom wrapper format (which no real API follows),
the backend derives the schema directly from the data it fetches. This keeps
"dynamic schema loading", "flat-data-only validation", and "in-memory
storage" intact while working with genuine public APIs out of the box.

---



| Method | Path                        | Description                                  |
|--------|-----------------------------|-----------------------------------------------|
| POST   | `/api/schemas`              | `{ "url": "...", "name": "optional" }` - registers a new source |
| GET    | `/api/schemas`               | Lists registered schemas (id, name, columns, recordCount) |
| GET    | `/api/schemas/{id}/data`     | Returns `{ "records": [...] }` (up to 200 cached rows) |


---

## real, public, no-auth APIs to try

All of these return a flat JSON array directly (no auth, no API key) and
work as-is with the registration form:

1. **`https://jsonplaceholder.typicode.com/posts`**
   100 blog posts: `userId`, `id`, `title`, `body`.

2. **`https://jsonplaceholder.typicode.com/todos`**
   200 todo items: `userId`, `id`, `title`, `completed` (boolean).

3. **`https://jsonplaceholder.typicode.com/comments`**
   500 comments: `postId`, `id`, `name`, `email`, `body`.

4. **`https://jsonplaceholder.typicode.com/albums`**
   100 photo albums: `userId`, `id`, `title`.

5. **`https://official-joke-api.appspot.com/jokes/ten`**
   10 random jokes: `type`, `setup`, `punchline`, `id`.

For contrast, `https://jsonplaceholder.typicode.com/users` is a good demo
of the **validation failure path** - each user object has a nested
`address` and `company` object, so registering it returns
`400 Bad Request: "Schema must be flat: field 'address' contains a nested
object or array"`.

---

## Project layout

```
backend/
  pom.xml
  src/main/java/com/veilant/datatable/
    Main.java                  - starts HttpServer, wires up routes
    handler/SchemasHandler.java - all /api/schemas request handling
    registry/SchemaRegistry.java - fetch, validate, in-memory storage
    registry/SchemaInference.java - flat-schema validation + type inference
    model/                     - SchemaDefinition, ColumnDefinition
    util/HttpUtil.java         - CORS + JSON response helpers

frontend/
  src/app/
    app.component.ts/html/css - registration form, dropdown, dynamic table
    services/schema.service.ts - all HTTP calls to the backend
    models/schema.model.ts    - SchemaInfo, ColumnDef, DataRecord types
```

