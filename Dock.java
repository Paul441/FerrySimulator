package com.example.ferry;

import java.util.Queue;
import java.util.concurrent.*;

/** Wspólna przystań: kolejka aut + jedna rampa (semafor). */
public class Dock {
    private final BlockingQueue<Car> waitingCars;
    private final Semaphore ramp = new Semaphore(1, true);          // true = FIFO fairness
    private final Queue<Ferry> ferryQueue = new ConcurrentLinkedQueue<>();

    public Dock(int capacity) { waitingCars = new ArrayBlockingQueue<>(capacity, true); }

    /* --------------- samochody --------------- */
    public boolean tryEnter(Car car) { return waitingCars.offer(car); }
    public Car pollCar()            { return waitingCars.poll(); }
    public int carsWaiting()        { return waitingCars.size(); }

    /* --------------- promy --------------- */
    public void requestRamp(Ferry f) throws InterruptedException {
        ferryQueue.add(f);
        ramp.acquire();              // czekamy na wolną rampę
        ferryQueue.remove(f);
    }
    public void releaseRamp() { ramp.release(); }

    /**
     * Zwraca i usuwa z kolejki samochód czekający najdłużej.
     * Jeśli kolejka pusta – zwraca null.
     */
    public Car pollOldestCar() {
        synchronized (waitingCars) {              // ArrayBlockingQueue nie ma metody peek najstarszego
            Car oldest = null;
            for (Car c : waitingCars) {
                if (oldest == null || c.arrivalEpochMillis() < oldest.arrivalEpochMillis()) {
                    oldest = c;
                }
            }
            if (oldest != null) waitingCars.remove(oldest);
            return oldest;
        }
    }

}