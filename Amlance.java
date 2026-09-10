package com.system.ambulance;

public class Ambulance {
    private final String id;
    private final AmbulanceType type;
    private final String driverName;
    private final String driverPhone;
    private AmbulanceState state;
    private double currentX;
    private double currentY;

    public Ambulance(String id, AmbulanceType type, String driverName, String driverPhone, double startX, double startY) {
        this.id = id;
        this.type = type;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.state = AmbulanceState.AVAILABLE;
        this.currentX = startX;
        this.currentY = startY;
    }

    public String getId() { return id; }
    public AmbulanceType getType() { return type; }
    public String getDriverDetails() { return driverName + " (" + driverPhone + ")"; }
    public AmbulanceState getState() { return state; }
    public void setState(AmbulanceState state) { this.state = state; }
    public double getCurrentX() { return currentX; }
    public double getCurrentY() { return currentY; }
    public void updateLocation(double x, double y) { this.currentX = x; this.currentY = y; }

    public double calculateDistance(double targetX, double targetY) {
        // Manhattan distance calculation for simulation simplicity
        return Math.abs(this.currentX - targetX) + Math.abs(this.currentY - targetY);
    }
}
