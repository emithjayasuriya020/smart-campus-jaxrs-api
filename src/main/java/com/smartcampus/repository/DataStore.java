package com.smartcampus.repository;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    public static Map<String, Room> getRooms() { return rooms; }
    public static Map<String, Sensor> getSensors() { return sensors; }
}