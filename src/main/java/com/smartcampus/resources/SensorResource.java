package com.smartcampus.resources;

import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.Room;
import com.smartcampus.repository.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        if (type != null && !type.isEmpty()) {
            return DataStore.getSensors().values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(DataStore.getSensors().values());
    }

    @POST
    public Response registerSensor(Sensor sensor) {
        Room room = DataStore.getRooms().get(sensor.getRoomId());

        if (room == null) {
            throw new LinkedResourceNotFoundException("Referential Integrity Error: Room '"
                    + sensor.getRoomId() + "' does not exist.");
        }

        DataStore.getSensors().put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}