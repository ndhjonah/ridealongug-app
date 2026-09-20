import { Injectable } from '@angular/core';

const BASE_URL = 'http://localhost:8080/api/v1/';

export interface SearchFilter {
  key: string;
  operator: string;
  fieldType: string;
  value?: unknown;
  values?: unknown[];
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  async call<T = any>(service: string, action: string, payload: object = {}, token?: string | null): Promise<T> {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(BASE_URL, {
      method: 'POST',
      headers,
      body: JSON.stringify({ SERVICE: service, ACTION: action, ...payload }),
    });

    const data = await response.json();

    if (data.returnCode !== 0) {
      throw new Error(data.returnMessage || 'Something went wrong. Please try again.');
    }

    return data.returnObject;
  }
}

export function equalsFilter(key: string, fieldType: string, value: unknown): SearchFilter {
  return { key, operator: 'EQUAL', fieldType, value };
}

export function searchPayload(filters: SearchFilter[] = [], page = 0, size = 100) {
  return { SEARCH: { filters, page, size } };
}
