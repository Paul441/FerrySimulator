package com.example.ferry;

public class Main {
    public static void main(String[] args) {
        /* pokazujemy ekran startowy zamiast bezpośredniej symulacji */
        javax.swing.SwingUtilities.invokeLater(Launcher::new);
    }
}
