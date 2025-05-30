package com.example.ferry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Launcher extends JFrame {

    
    private static RoundedPanel wrap(Component c) {
        
        RoundedPanel p = new RoundedPanel(12, new Color(0xf5f5f5));
        p.setLayout(new BorderLayout());
        p.add(c, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(4, 6, 4, 6));
        return p;
    }

    
    private final JPanel ferryTable = new JPanel(new GridLayout(0, 2, 10, 8));
    private final List<JSpinner> capSpinners = new ArrayList<>();

    public Launcher() {
        
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignore){}

        setTitle("Promy na rzece – konfiguracja");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        
        setContentPane(new GradientPanel());

        setLayout(new BorderLayout(25, 25));

        
        JLabel title = new JLabel("PROMY NA RZECE", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 36f));
        title.setForeground(new Color(0x093d59));
        add(title, BorderLayout.NORTH);

       
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        topBar.setOpaque(false);

        topBar.add(new JLabel("Liczba promów:", SwingConstants.RIGHT));
        JSpinner spCount = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        topBar.add(wrap(spCount));

        
        JPanel params = new JPanel(new GridLayout(2, 3, 12, 10));
        params.setOpaque(false);

       
        params.add(new JLabel("Interwał pojazdów:", SwingConstants.RIGHT));
        JSpinner spCar = new JSpinner(new SpinnerNumberModel(600, 1, 100_000, 1));
        params.add(wrap(spCar));
        JComboBox<String> unitCar = new JComboBox<>(new String[]{"ms", "s"});
        params.add(wrap(unitCar));

       
        params.add(new JLabel("T oczekiwania promu:", SwingConstants.RIGHT));
        JSpinner spWait = new JSpinner(new SpinnerNumberModel(10, 1, 100_000, 1));
        params.add(wrap(spWait));
        JComboBox<String> unitWait = new JComboBox<>(new String[]{"s", "ms"});
        params.add(wrap(unitWait));

       
        rebuildFerryRows((Integer) spCount.getValue());
        spCount.addChangeListener(e -> rebuildFerryRows((Integer) spCount.getValue()));

        JPanel tableWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tableWrapper.setOpaque(false);
        tableWrapper.add(ferryTable);

        
        JPanel centerBox = new JPanel();
        centerBox.setOpaque(false);
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.add(topBar);
        centerBox.add(Box.createVerticalStrut(15));
        centerBox.add(tableWrapper);
        centerBox.add(Box.createVerticalStrut(15));
        centerBox.add(params);

        add(centerBox, BorderLayout.CENTER);

        
        JButton play = new JButton("PLAY");
        play.setFont(play.getFont().deriveFont(Font.BOLD, 18f));
        play.setForeground(Color.WHITE);
        play.setBackground(new Color(0x008cba));
        play.setFocusPainted(false);
        play.setBorderPainted(false);
        play.setPreferredSize(new Dimension(120, 40));
        add(play, BorderLayout.SOUTH);

        
        play.addActionListener(e -> {
            int nFerries = (Integer) spCount.getValue();

            int carVal  = (Integer) spCar.getValue();
            int waitVal = (Integer) spWait.getValue();

            int carMs  = unitCar .getSelectedItem().equals("s") ? carVal  * 1000 : carVal;
            int waitMs = unitWait.getSelectedItem().equals("s") ? waitVal * 1000 : waitVal;

            launchSimulation(nFerries, carMs, waitMs);
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    
    private void rebuildFerryRows(int n) {
        ferryTable.removeAll();
        capSpinners.clear();
        ferryTable.setOpaque(false);

        ferryTable.add(new JLabel());
        ferryTable.add(new JLabel("Pojemność", SwingConstants.CENTER));

        for (int i = 1; i <= n; i++) {
            ferryTable.add(new JLabel("Prom " + i + ":", SwingConstants.RIGHT));
            JSpinner sp = new JSpinner(new SpinnerNumberModel(12, 5, 30, 1));
            capSpinners.add(sp);
            ferryTable.add(wrap(sp));
        }
        ferryTable.revalidate();
        ferryTable.repaint();
        pack();
    }

    
    private void launchSimulation(int nFerries, int carIntervalMs, int waitMs) {

        Config cfg = ConfigLoader.load();
        cfg.carArrivalIntervalMs = carIntervalMs;

        List<Config.FerrySpec> list = new ArrayList<>();
        for (int i = 0; i < nFerries; i++) {
            Config.FerrySpec fs = new Config.FerrySpec();
            fs.id             = i + 1;
            fs.capacity       = (Integer) capSpinners.get(i).getValue();
            fs.maxWaitSeconds = waitMs / 1000;
            list.add(fs);
        }
        cfg.ferries = list;

        new Simulation(cfg).start();
        dispose();
    }

    
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        RoundedPanel(int radius, Color bg) {
            this.radius = radius; this.bg = bg;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(new Color(0x000000, true));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    private static class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            Color c1 = new Color(0xe8f7ff);
            Color c2 = new Color(0xcbe6ff);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }
}
