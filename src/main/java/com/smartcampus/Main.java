package com.smartcampus;

import com.smartcampus.resources.DiscoveryResource;
import com.smartcampus.resources.RoomResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.io.IOException;
import java.net.URI;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws IOException {
        final ResourceConfig rc = new ResourceConfig();

        rc.register(com.smartcampus.resources.DiscoveryResource.class);
        rc.register(com.smartcampus.resources.RoomResource.class);
        rc.register(com.smartcampus.resources.SensorResource.class);

        rc.register(com.smartcampus.exceptions.GlobalExceptionMapper.class);
        rc.register(com.smartcampus.filters.LoggingFilter.class);

        String webApiContext = "api/v1";
        URI finalUri = URI.create(BASE_URI + webApiContext);

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(finalUri, rc);

        System.out.println("Smart Campus API started at " + finalUri);
        System.out.println("POST your JSON to: " + finalUri + "/rooms");
        System.out.println("Hit enter to stop...");
        System.in.read();
        server.shutdownNow();
    }
}