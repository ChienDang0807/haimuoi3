export interface Address {
  id: number;
  addressName: string;
  recipientName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  streetAddress: string;
  isDefault: boolean;
  createdAt: string;
}

export interface CreateAddressRequest {
  addressName?: string;
  recipientName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  streetAddress: string;
}

export type UpdateAddressRequest = CreateAddressRequest;
