package org.handler.exception;

import lombok.Getter;
import org.handler.dto.response.SupplierResponseDto;

@Getter
public class SupplierAlreadyAssignedException extends RuntimeException {
    private final SupplierResponseDto supplier;

    public SupplierAlreadyAssignedException(String message, SupplierResponseDto supplier) {
        super(message);
        this.supplier = supplier;
    }

}
