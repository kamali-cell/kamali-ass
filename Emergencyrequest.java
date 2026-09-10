package com.system.ambulance;

import java.util.UUID;

public class EmergencyRequest implements Comparable<EmergencyRequest> {
    private final String id;
    private final String patientId;
    private final String emergencyType; // e.g., Cardiac Arrest, Minor Fracture
    private final String pickupLocation;
    private final String destinationHospital;
    private final EmergencyPriority priority;
    private final AmbulanceType requiredAmbulanceType;
    private double locX;
    private double locY;
    private String status;

    public EmergencyRequest(String patientId, String emergencyType, String pickupLocation, 
                            String destinationHospital, EmergencyPriority priority, 
                            AmbulanceType requiredAmbulanceType, double locX, double locY) throws InvalidRequestException {
        if (patientId == null || patientId.trim().isEmpty() || 
            pickupLocation == null || pickupLocation.trim().isEmpty() || 
            destinationHospital == null || destinationHospital.trim().isEmpty()) {
            throw new InvalidRequestException("Patient ID, pickup location, and destination hospital cannot be empty.");
        }
        this.id = UUID.randomUUID().toString();
        this.patientId = patientId;
        this.emergencyType = emergencyType;
        this.pickupLocation = pickupLocation;
        this.destinationHospital = destinationHospital;
        this.priority = priority;
        this.requiredAmbulanceType = requiredAmbulanceType;
        this.locX = locX;
        this.locY = locY;
        this.status = "PENDING";
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getEmergencyType() { return emergencyType; }
    public String getPickupLocation() { return pickupLocation; }
    public String getDestinationHospital() { return destinationHospital; }
    public EmergencyPriority getPriority() { return priority; }
    public AmbulanceType getRequiredAmbulanceType() { return requiredAmbulanceType; }
    public double getLocX() { return locX; }
    public double getLocY() { return locY; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public int compareTo(EmergencyRequest other) {
        // Higher priority ranks processed first
        return Integer.compare(other.priority.getRank(), this.priority.getRank());
    }
}
