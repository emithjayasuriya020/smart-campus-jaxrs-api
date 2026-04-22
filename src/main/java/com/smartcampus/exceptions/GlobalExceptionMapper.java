package com.smartcampus.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", true);

        if (exception instanceof RoomNotEmptyException) {
            RoomNotEmptyException ex = (RoomNotEmptyException) exception;
            errorBody.put("message", ex.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(errorBody).build();
        }

        if (exception instanceof LinkedResourceNotFoundException) {
            LinkedResourceNotFoundException ex = (LinkedResourceNotFoundException) exception;
            errorBody.put("message", ex.getMessage());
            return Response.status(422).entity(errorBody).build();
        }

        if (exception instanceof SensorUnavailableException) {
            errorBody.put("message", exception.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(errorBody).type(MediaType.APPLICATION_JSON).build();
        }

        errorBody.put("message", "An unexpected internal server error occurred.");
        return Response.status(500).entity(errorBody).build();
    }
}