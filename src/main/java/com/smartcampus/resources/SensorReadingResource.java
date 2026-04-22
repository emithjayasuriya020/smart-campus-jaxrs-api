package com.smartcampus.resources;

import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import com.smartcampus.repository.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private static final java.util.Map<String, List<SensorReading>> historyStore = new java.util.concurrent.ConcurrentHashMap<>();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        return historyStore.getOrDefault(sensorId, new ArrayList<>());
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.getSensors().get(sensorId);

        if (sensor == null) {
            return Response.status(404).entity("{\"message\":\"Sensor not found\"}").build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is under maintenance.");
        }

        sensor.setCurrentValue(reading.getValue());

        historyStore.putIfAbsent(sensorId, new CopyOnWriteArrayList<>());
        historyStore.get(sensorId).add(reading);

        return Response.status(201).entity(reading).build();
    }
}