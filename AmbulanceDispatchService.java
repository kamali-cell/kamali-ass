package com.system.ambulance;

import java.util.*;

public class AmbulanceDispatchService {
    private final List<Ambulance> fleet = new ArrayList<>();
    private final PriorityQueue<EmergencyRequest> waitingQueue = new PriorityQueue<>();
    private final Map<String, Ambulance> activeAssignments = new HashMap<>(); // RequestID -> Ambulance
    private final List<String> executionHistory = new ArrayList<>();

    public void registerAmbulance(Ambulance ambulance) {
        fleet.add(ambulance);
        executionHistory.add("Registered Ambulance: " + ambulance.getId() + " [" + ambulance.getType() + "]");
        processWaitingQueue(); // Check if newly registered ambulance resolves a queued task
    }

    public synchronized String submitEmergencyRequest(EmergencyRequest request) {
        executionHistory.add("Received emergency request " + request.getId() + " (Priority: " + request.getPriority() + ")");
        try {
            Ambulance allocatedAmbulance = findBestAmbulance(request);
            assignAmbulance(request, allocatedAmbulance);
            double distance = allocatedAmbulance.calculateDistance(request.getLocX(), request.getLocY());
            double eta = distance * 1.5; // Estimated 1.5 minutes per coordinate unit
            
            return String.format("SUCCESS: Request %s allocated to Ambulance %s. Distance: %.2f units. ETA: %.1f mins.", 
                    request.getId(), allocatedAmbulance.getId(), distance, eta);
        } catch (NoAmbulanceAvailableException e) {
            waitingQueue.add(request);
            request.setStatus("QUEUED");
            executionHistory.add("No resources found. Request " + request.getId() + " placed in waiting queue.");
            return "QUEUED: No available ambulance matching needs at this moment. Placed in queue.";
        }
    }

    private Ambulance findBestAmbulance(EmergencyRequest request) throws NoAmbulanceAvailableException {
        Ambulance bestMatch = null;
        double minDistance = Double.MAX_VALUE;

        for (Ambulance ambulance : fleet) {
            if (ambulance.getState() == AmbulanceState.AVAILABLE && ambulance.getType() == request.getRequiredAmbulanceType()) {
                double distance = ambulance.calculateDistance(request.getLocX(), request.getLocY());
                if (distance < minDistance) {
                    minDistance = distance;
                    bestMatch = ambulance;
                }
            }
        }

        if (bestMatch == null) {
            throw new NoAmbulanceAvailableException("No available " + request.getRequiredAmbulanceType() + " ambulance found.");
        }
        return bestMatch;
    }

    private void assignAmbulance(EmergencyRequest request, Ambulance ambulance) {
        ambulance.setState(AmbulanceState.DISPATCHED);
        request.setStatus("DISPATCHED");
        activeAssignments.put(request.getId(), ambulance);
        executionHistory.add("Ambulance " + ambulance.getId() + " dispatched to Request " + request.getId());
    }

    public synchronized void transitionState(String requestId, AmbulanceState newState) {
        Ambulance ambulance = activeAssignments.get(requestId);
        if (ambulance == null) return;

        ambulance.setState(newState);
        executionHistory.add("Ambulance " + ambulance.getId() + " transitioned to state: " + newState);

        if (newState == AmbulanceState.HOSPITAL_ARRIVED) {
            // Drop-off finished, make ambulance available again
            ambulance.setState(AmbulanceState.AVAILABLE);
            activeAssignments.remove(requestId);
            executionHistory.add("Ambulance " + ambulance.getId() + " is now AVAILABLE.");
            processWaitingQueue(); // Auto-allocate to any high-priority item waiting
        }
    }

    private void processWaitingQueue() {
        while (!waitingQueue.isEmpty()) {
            EmergencyRequest topRequest = waitingQueue.peek();
            try {
                Ambulance ambulance = findBestAmbulance(topRequest);
                waitingQueue.poll(); // Remove from queue since resource is found
                assignAmbulance(topRequest, ambulance);
            } catch (NoAmbulanceAvailableException e) {
                break; // Break loop if no resources match the current top element
            }
        }
    }

    public List<Ambulance> getFleet() { return fleet; }
    public PriorityQueue<EmergencyRequest> getWaitingQueue() { return waitingQueue; }
    public List<String> getExecutionHistory() { return executionHistory; }
}
