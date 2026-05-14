/**
 * Standard API Response wrapper từ Backend
 * Theo format: ApiResponse<T> trong BE
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  result: T;
}

/**
 * Paginated response từ Spring Data Page
 */
export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}
