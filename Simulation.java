
package com.example.ferry;

import java.util.ArrayList;
import java.util.List;

public class Simulation {

    private final Config cfg;
    private final Dock dock;
    private final SimulationUI ui;
    private final List<Thread> workers = new ArrayList<>();

    public Simulation(Config cfg) {
        this.cfg  = cfg;
        this.dock = new Dock(cfg.dockCapacity);
        this.ui   = new SimulationUI(cfg.ferries);   
    }



    public void start() {

        
        Thread carGen = new Thread(
                new CarGenerator(dock, cfg.carArrivalIntervalMs, ui),
                "CarGenerator");
        workers.add(carGen);
        carGen.start();

       
        cfg.ferries.forEach(fs -> {
            Ferry f = new Ferry(fs.id, fs.capacity, fs.maxWaitSeconds,
                    dock, ui);
            Thread t = new Thread(f, "Ferry-" + fs.id);
            workers.add(t);
            t.start();
        });
    }
}
