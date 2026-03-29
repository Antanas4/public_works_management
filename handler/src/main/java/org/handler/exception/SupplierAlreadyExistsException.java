package org.handler.exception;

import lombok.Getter;
import org.handler.dto.response.SupplierResponseDto;

@Getter
public class SupplierAlreadyExistsException extends RuntimeException {
    private final SupplierResponseDto supplier;

    public SupplierAlreadyExistsException(String message, SupplierResponseDto supplier) {
        super(message);
        this.supplier = supplier;
    }

}
