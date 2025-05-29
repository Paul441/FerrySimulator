package com.example.ferry;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/** Produkuje samochody w losowych odstępach. */
public class CarGenerator implements Runnable {

    private final Dock dock;
    private final int avgIntervalMs;
    private final SimulationUI ui;

    private final AtomicLong seq = new AtomicLong();  

    public CarGenerator(Dock dock, int avgIntervalMs, SimulationUI ui) {
        this.dock = dock;
        this.avgIntervalMs = avgIntervalMs;
        this.ui = ui;
    }

    @Override public void run() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        try {
            while (!Thread.currentThread().isInterrupted()) {

                int speed = rnd.nextInt(40, 91);             // km/h
                ui.updateIncomingSpeed(speed);

                long wait = Math.max(80,
                        (long) (rnd.nextGaussian(avgIntervalMs, avgIntervalMs * 0.35)));
                Thread.sleep(wait);

                long id  = seq.incrementAndGet();
                Car car  = new Car(id, System.currentTimeMillis());

                if (dock.tryEnter(car)) ui.carArrived(car, dock.carsWaiting());
                else                    ui.carRejected(car);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
