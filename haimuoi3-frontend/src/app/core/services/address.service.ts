import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants/api-endpoints';
import {
  Address,
  ApiResponse,
  CreateAddressRequest,
  UpdateAddressRequest,
} from '../../shared/interfaces';

@Injectable({ providedIn: 'root' })
export class AddressService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}${ApiEndpoints.CUSTOMERS_ME_ADDRESSES}`;

  listMyAddresses(): Observable<ApiResponse<Address[]>> {
    return this.http.get<ApiResponse<Address[]>>(this.baseUrl);
  }

  createAddress(body: CreateAddressRequest): Observable<ApiResponse<Address>> {
    return this.http.post<ApiResponse<Address>>(this.baseUrl, body);
  }

  updateAddress(id: number, body: UpdateAddressRequest): Observable<ApiResponse<Address>> {
    return this.http.put<ApiResponse<Address>>(`${this.baseUrl}/${id}`, body);
  }

  deleteAddress(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }

  setDefaultAddress(id: number): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.baseUrl}/${id}/set-default`, {});
  }
}
