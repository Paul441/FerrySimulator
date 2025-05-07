package com.example.ferry;

import java.util.List;

/** Odwzorowanie struktury YAML → obiekt. */
public class Config {
    public int dockCapacity;
    public int carArrivalIntervalMs;
    public int simulationTimeSeconds;
    public List<FerrySpec> ferries;
    public static class FerrySpec {
        public int id;
        public int capacity;
        public int maxWaitSeconds;
    }
}