import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DataRecord, SchemaInfo } from '../models/schema.model';

/** All calls to the Java backend live here - one place to change the API base URL. */
@Injectable({ providedIn: 'root' })
export class SchemaService {
  private readonly baseUrl = 'http://localhost:8080/api/schemas';

  constructor(private http: HttpClient) {}

  /** Registers a new API URL with the backend, which fetches it and infers a schema. */
  registerSchema(url: string, name: string): Observable<SchemaInfo> {
    return this.http.post<SchemaInfo>(this.baseUrl, { url, name });
  }

  /** Lists every schema registered so far (name + columns only, no data). */
  listSchemas(): Observable<SchemaInfo[]> {
    return this.http.get<SchemaInfo[]>(this.baseUrl);
  }

  /** Fetches the cached dataset (up to 200 rows) for a registered schema. */
  getData(schemaId: string): Observable<{ records: DataRecord[] }> {
    return this.http.get<{ records: DataRecord[] }>(`${this.baseUrl}/${schemaId}/data`);
  }
}
