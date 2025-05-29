package com.example.ferry;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/*
  Ekran startowy:
 • tytuł „PROMY NA RZECE”
 • wybór liczby promów (1-10)
 • osobny spinner pojemności dla każdego promu
 • przycisk PLAY → start symulacji
 */
public class Launcher extends JFrame {

    private final JPanel capacityPanel = new JPanel(new GridLayout(0, 2, 10, 6));
    private final List<JSpinner> capSpinners = new ArrayList<>();

    public Launcher() {
        setTitle("Promy na rzece – konfiguracja");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));

       
        JLabel title = new JLabel("PROMY NA RZECE", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        top.add(new JLabel("Liczba promów:"));
        JSpinner spCount = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        top.add(spCount);
        add(top, BorderLayout.CENTER);

        
        add(capacityPanel, BorderLayout.WEST);
        rebuildCapacitySpinners((Integer) spCount.getValue());

        spCount.addChangeListener(e ->
                rebuildCapacitySpinners((Integer) spCount.getValue())
        );

        
        JButton play = new JButton("PLAY");
        add(play, BorderLayout.SOUTH);
        play.addActionListener(e -> startSimulation((Integer) spCount.getValue()));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    
    private void rebuildCapacitySpinners(int count) {
        capacityPanel.removeAll();
        capSpinners.clear();

        for (int i = 1; i <= count; i++) {
            capacityPanel.add(new JLabel("Pojemność promu " + i + ":",
                    SwingConstants.RIGHT));
            JSpinner sp = new JSpinner(new SpinnerNumberModel(12, 5, 30, 1));
            capSpinners.add(sp);
            capacityPanel.add(sp);
        }
        capacityPanel.revalidate();
        capacityPanel.repaint();
        pack();  
    }

    
    private void startSimulation(int nFerries) {
        Config cfg = ConfigLoader.load();               

        List<Config.FerrySpec> list = new ArrayList<>();
        for (int i = 0; i < nFerries; i++) {
            Config.FerrySpec fs = new Config.FerrySpec();
            fs.id = i + 1;
            fs.capacity = (Integer) capSpinners.get(i).getValue();
            fs.maxWaitSeconds = 20;                     
            list.add(fs);
        }
        cfg.ferries = list;

        new Simulation(cfg).start();
        dispose();                                      
    }
}
