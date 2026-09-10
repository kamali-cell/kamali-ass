package com.system.ambulance;

public class NoAmbulanceAvailableException extends Exception {
    public NoAmbulanceAvailableException(String message) {
        super(message);
    }
}
