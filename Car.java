package com.example.ferry;

/** Pojedynczy samochód (immutable record). */
public record Car(long id, long arrivalEpochMillis) {
    @Override public String toString() { return "Car-" + id; }
}
