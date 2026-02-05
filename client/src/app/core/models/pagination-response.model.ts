export interface PaginationResponse<T> {
    items: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    pageNumber: number;
    empty: boolean;
}
