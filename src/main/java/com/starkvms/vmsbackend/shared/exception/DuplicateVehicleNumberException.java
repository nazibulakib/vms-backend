package com.starkvms.vmsbackend.shared.exception;

public class DuplicateVehicleNumberException extends RuntimeException {

    public DuplicateVehicleNumberException() {
        super("A vehicle with this vehicle number already exists.");
    }

    public DuplicateVehicleNumberException(Throwable cause) {
        super("A vehicle with this vehicle number already exists.", cause);
    }
}
