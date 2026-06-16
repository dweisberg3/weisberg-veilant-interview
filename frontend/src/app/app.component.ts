import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { TableModule } from 'primeng/table';
import { ColumnDef, DataRecord, SchemaInfo } from './models/schema.model';
import { SchemaService } from './services/schema.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, TableModule, DropdownModule, ButtonModule, InputTextModule, MessageModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  // --- Registration form state ---
  newUrl = '';
  newName = '';
  registering = false;
  registerError: string | null = null;

  // --- Schema list + selection ---
  schemas: SchemaInfo[] = [];
  selectedSchema: SchemaInfo | null = null;

  // --- Table state ---
  columns: ColumnDef[] = [];
  data: DataRecord[] = [];
  loadingData = false;
  dataError: string | null = null;

  constructor(private schemaService: SchemaService) {}

  ngOnInit(): void {
    this.schemaService.listSchemas().subscribe((schemas) => (this.schemas = schemas));
  }

  register(): void {
    const url = this.newUrl.trim();
    if (!url) {
      return;
    }

    this.registering = true;
    this.registerError = null;

    this.schemaService.registerSchema(url, this.newName.trim()).subscribe({
      next: (schema) => {
        this.schemas = [...this.schemas, schema];
        this.newUrl = '';
        this.newName = '';
        this.registering = false;
      },
      error: (err) => {
        this.registerError = err.error?.error ?? 'Failed to register schema';
        this.registering = false;
      }
    });
  }

  // Selecting a schema shows its column headers immediately, but clears any
  // previously loaded data - the user must click "Get Data Now" to fetch rows.
  onSchemaChange(): void {
    this.columns = this.selectedSchema?.columns ?? [];
    this.data = [];
    this.dataError = null;
  }

  loadData(): void {
    if (!this.selectedSchema) {
      return;
    }

    this.loadingData = true;
    this.dataError = null;

    this.schemaService.getData(this.selectedSchema.id).subscribe({
      next: (response) => {
        this.data = response.records;
        this.loadingData = false;
      },
      error: (err) => {
        this.dataError = err.error?.error ?? 'Failed to load data';
        this.loadingData = false;
      }
    });
  }
}
