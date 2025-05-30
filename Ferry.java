


package com.example.ferry;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Ferry implements Runnable {

    private final int id;
    private final int capacity;
    private final Duration maxWait;
    private final Dock dock;
    private final SimulationUI ui;

    private final List<Car> onBoard = new ArrayList<>();

    public Ferry(int id,
                 int capacity,
                 int maxWaitSeconds,
                 Dock dock,
                 SimulationUI ui) {
        this.id       = id;
        this.capacity = capacity;
        this.maxWait  = Duration.ofSeconds(maxWaitSeconds);
        this.dock     = dock;
        this.ui       = ui;
    }

    @Override public void run() {
        try {
            /* losowy pierwszy start 0-10 s */
            Thread.sleep(ThreadLocalRandom.current().nextLong(0, 8_000));
            while (!Thread.currentThread().isInterrupted()) {
                doCycle();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void doCycle() throws InterruptedException {

        
        Thread.sleep(ThreadLocalRandom.current().nextLong(2_000, 10_000));

        
        ui.ferryWaiting(id);
        dock.requestRamp(this);
        ui.ferryDocked(id);

        //rozładunek aut
        int arriving = ThreadLocalRandom.current().nextInt(1, capacity + 1);
        ui.log(id, "Arrived with " + arriving + "/" + capacity + " cars");
        ui.updateFerryLoad(id, arriving, capacity);

        for (int i = 0; i < arriving; i++) {
            ui.addExitingCars(1, ui.dockEntryX());
            ui.updateFerryLoad(id, arriving - i - 1, capacity);
            ui.log(id, "Vehicle " + (i + 1) + " left ferry");
            Thread.sleep(300);                     
        }

        
        onBoard.clear();
        Instant startLoad = Instant.now();
        Instant deadline  = startLoad.plus(maxWait);

        while (onBoard.size() < capacity && Instant.now().isBefore(deadline)) {
            Car c = dock.pollOldestCar();
            if (c != null) {
                onBoard.add(c);
                ui.carEmbarked(c);
                ui.updateFerryLoad(id, onBoard.size(), capacity);
                Thread.sleep(300);                 
            } else {
                Thread.sleep(100);
            }
            if (onBoard.isEmpty()) deadline = Instant.now().plus(maxWait);
        }

       
        Thread.sleep(500);

        dock.releaseRamp();
        ui.ferryLeft(id);
        ui.log(id, "Departing (" + onBoard.size() + "/" + capacity + ")");

        crossRiver();
    }

    private void crossRiver() throws InterruptedException {
        long travel = 2_000 + onBoard.size() * 120
                + ThreadLocalRandom.current().nextLong(800);
        Thread.sleep(travel);
    }
}
