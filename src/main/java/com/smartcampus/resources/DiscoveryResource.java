package com.smartcampus.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscovery() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", "1.0.0");
        info.put("description", "Smart Campus API");

        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        info.put("links", links);

        return Response.ok(info).build();
    }

    @GET
    @Path("/crash") // This makes the URL: /api/v1/crash
    public Response triggerError() {
        throw new RuntimeException("Simulated unexpected server failure.");
    }
}
