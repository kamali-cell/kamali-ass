package com.system.ambulance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AmbulanceDispatchServiceTest {
    private AmbulanceDispatchService service;

    @BeforeEach
    public void setup() {
        service = new AmbulanceDispatchService();
    }

    @Test
    public void testSuccessfulAllocationBasedOnDistance() throws InvalidRequestException {
        Ambulance farAmb = new Ambulance("AMB-FAR", AmbulanceType.ICU, "Driver A", "123", 10, 10);
        Ambulance nearAmb = new Ambulance("AMB-NEAR", AmbulanceType.ICU, "Driver B", "456", 2, 2);
        
        service.registerAmbulance(farAmb);
        service.registerAmbulance(nearAmb);

        EmergencyRequest request = new EmergencyRequest("P001", "Cardiac", "Loc X", "Hosp Y", 
                EmergencyPriority.CRITICAL, AmbulanceType.ICU, 0, 0);

        String outcome = service.submitEmergencyRequest(request);
        
        assertTrue(outcome.contains("Allocated Ambulance AMB-NEAR"));
        assertEquals(AmbulanceState.DISPATCHED, nearAmb.getState());
    }

    @Test
    public void testWaitingQueueAndPriorityOrdering() throws InvalidRequestException {
        EmergencyRequest normalReq = new EmergencyRequest("P-NORMAL", "General", "Loc A", "Hosp Y", 
                EmergencyPriority.NORMAL, AmbulanceType.BASIC, 0, 0);
        EmergencyRequest criticalReq = new EmergencyRequest("P-CRIT", "Accident", "Loc B", "Hosp Y", 
                EmergencyPriority.CRITICAL, AmbulanceType.BASIC, 0, 0);

        // Submit without available ambulances to force queue placement
        service.submitEmergencyRequest(normalReq);
        service.submitEmergencyRequest(criticalReq);

        assertEquals(2, service.getWaitingQueue().size());
        // Verify priority handling sorting: CRITICAL must sit at the head of the queue
        assertEquals("P-CRIT", service.getWaitingQueue().peek().getPatientId());
    }

    @Test
    public void testAutomaticAllocationWhenResourceBecomesAvailable() throws InvalidRequestException {
        EmergencyRequest request = new EmergencyRequest("P-WAITING", "Trauma", "Loc Z", "Hosp Y", 
                EmergencyPriority.HIGH, AmbulanceType.ADVANCED_LIFE_SUPPORT, 5, 5);
        
        String initialStatus = service.submitEmergencyRequest(request);
        assertTrue(initialStatus.contains("QUEUED"));

        Ambulance newAmb = new Ambulance("AMB-ALS", AmbulanceType.ADVANCED_LIFE_SUPPORT, "Driver C", "789", 0, 0);
        service.registerAmbulance(newAmb);

        // Verification: Newly registered ambulance should automatically fulfill the queued request
        assertTrue(service.getWaitingQueue().isEmpty());
        assertEquals(AmbulanceState.DISPATCHED, newAmb.getState());
    }

    @Test
    public void testInvalidInputExceptionHandling() {
        assertThrows(InvalidRequestException.class, () -> {
            new EmergencyRequest("", "Routine", " ", "Hosp", EmergencyPriority.NORMAL, AmbulanceType.BASIC, 0, 0);
        });
    }
}
