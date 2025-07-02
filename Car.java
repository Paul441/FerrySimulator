package com.example.ferry;


public record Car(long id, long arrivalEpochMillis) {
    @Override public String toString() { return "Car-" + id; }
}
