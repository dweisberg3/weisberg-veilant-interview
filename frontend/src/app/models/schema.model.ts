/** A single column of a dynamically-loaded schema. */
export interface ColumnDef {
  field: string;
  type: 'string' | 'number' | 'boolean';
}

/** A registered data source, as returned by the backend. */
export interface SchemaInfo {
  id: string;
  name: string;
  sourceUrl: string;
  columns: ColumnDef[];
  recordCount: number;
}

/** A single row of data - keys match SchemaInfo.columns[].field. */
export type DataRecord = Record<string, string | number | boolean | null>;
