package com.example.ferry;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimulationUI extends JFrame {

    /* ─── modele ─── */
    private static class FerryState {
        volatile boolean atDock = false;
        volatile boolean waiting = false;
        volatile long    waitingSince = 0;
        volatile int     load = 0;
        final    int     capacity;
        FerryState(int cap){ capacity = cap; }
    }
    private static class ExitingCar {
        int x;
        ExitingCar(int x){ this.x = x; }
    }

    /* ─── pola ─── */
    private final Map<Integer,FerryState> ferries = new ConcurrentHashMap<>();
    private final java.util.List<Car> queue = new ArrayList<>();
    private final java.util.List<ExitingCar> exiting = new ArrayList<>();

    private volatile int incomingSpeed = 0;

    private final DrawPanel canvas = new DrawPanel();
    private final JTextArea logArea = new JTextArea(6, 60);
    private final javax.swing.Timer animTimer;

    public SimulationUI(java.util.List<Config.FerrySpec> specs){
        setTitle("River Ferry Simulation");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        specs.forEach(s -> ferries.put(s.id, new FerryState(s.capacity)));

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        setLayout(new BorderLayout(10,10));
        add(canvas, BorderLayout.CENTER);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        /* panel (MENU / EXIT) – przycisk PAUSE usunięty */
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        JButton menu = new JButton("MENU");
        JButton exit = new JButton("EXIT");
        for(JButton b: new JButton[]{menu, exit}){
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttons.add(b); buttons.add(Box.createVerticalStrut(8));
        }
        add(buttons, BorderLayout.EAST);

        exit.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?",
                    "Exit confirmation",
                    JOptionPane.YES_NO_OPTION);
            if(opt==JOptionPane.YES_OPTION) System.exit(0);
        });
        menu.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(Launcher::new);
        });

        /* animacja pomarańczowych kropek */
        animTimer = new javax.swing.Timer(40, e -> {
            synchronized(exiting){
                exiting.removeIf(c -> c.x > getWidth());
                exiting.forEach(c -> c.x += 4);
            }
            canvas.repaint();
        });
        animTimer.start();

        setSize(900, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /* --- metody API --- */
    private void later(Runnable r){ if(SwingUtilities.isEventDispatchThread()) r.run(); else SwingUtilities.invokeLater(r);}
    public void updateFerryLoad(int id,int load,int cap){ later(()->{var s=ferries.get(id);if(s!=null){s.load=load;canvas.repaint();}});}
    public void ferryWaiting(int id){ later(()->{var s=ferries.get(id);if(s!=null){s.waiting=true;s.waitingSince=System.currentTimeMillis();canvas.repaint();}});}
    public void ferryDocked(int id){ later(()->{ ferries.values().forEach(fs->fs.atDock=false); var s=ferries.get(id); if(s!=null){s.waiting=false;s.atDock=true;canvas.repaint();}});}
    public void ferryLeft(int id){ later(()->{var s=ferries.get(id);if(s!=null)s.atDock=false;canvas.repaint();});}
    public void carArrived(Car c,int q){ later(()->{queue.add(c);log(-1,"Car "+c.id()+" arrived (queue="+q+")");canvas.repaint();});}
    public void carEmbarked(Car c){ later(()->{queue.remove(c);canvas.repaint();});}
    public void carRejected(Car c){ log(-1,"Car "+c.id()+" turned back – pier full!");}
    public void updateIncomingSpeed(int kmh){ later(()->{incomingSpeed=kmh;canvas.repaint();});}
    public void addExitingCars(int n,int x){ later(()->{for(int i=0;i<n;i++) exiting.add(new ExitingCar(x));});}
    public boolean hasExitingCars(){ synchronized(exiting){return !exiting.isEmpty();}}
    public int dockEntryX(){ return getWidth()/3 + 10; }
    public void log(int fid,String msg){ later(()->{String tag=fid>=0?"[F"+fid+"] ":"[SYS] ";logArea.append(tag+msg+System.lineSeparator());logArea.setCaretPosition(logArea.getDocument().getLength());});}

    /* --- rysowanie --- */
    private class DrawPanel extends JPanel{
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g; int w=getWidth(),h=getHeight();
            int roadY=(int)(h*0.25);

            /* tło + droga */
            g2.setColor(new Color(0x90c8ff)); g2.fillRect(0,0,w,h);
            g2.setColor(Color.DARK_GRAY); g2.fillRect(0,roadY-6,w,12);

            /* zielone kropki */
            int sx=20, step=55;
            synchronized(queue){
                for(int i=0;i<queue.size();i++){
                    Car c=queue.get(i); int x=sx+i*step;
                    g2.setColor(new Color(0x2ecc71)); g2.fillOval(x-6,roadY-6,12,12);
                    g2.setColor(Color.BLACK); g2.setFont(g2.getFont().deriveFont(10f));
                    long wait=(System.currentTimeMillis()-c.arrivalEpochMillis())/1000;
                    g2.drawString("#"+(i+1),x-8,roadY-10);
                    g2.drawString(wait+"s",x-10,roadY+20);
                }
            }

            /* pomarańczowe kropki */
            synchronized(exiting){
                g2.setColor(new Color(0xffa500));
                exiting.forEach(ec -> g2.fillOval(ec.x-6,roadY-6,12,12));
            }

            /* prędkość auta */
            g2.setColor(Color.BLACK);
            g2.drawString("Nadjeżdża: "+incomingSpeed+" km/h", w-190, roadY-25);

            /* przystań */
            int dockX=w/3, dockY=(int)(h*0.45), dockW=160, dockH=14;
            g2.setColor(new Color(0x9b6a3b)); g2.fillRect(dockX,dockY,dockW,dockH);
            g2.setColor(Color.BLACK); g2.drawString("Przystań", dockX+dockW/2-25, dockY+dockH+12);

            /* prom przy pomoście */
            ferries.forEach((id,s)->{ if(s.atDock) drawFerry(g2,id,s,dockX+10,dockY-40); });

            /* kolejka promów */
            var waiting=ferries.entrySet().stream()
                    .filter(e->e.getValue().waiting)
                    .sorted(Comparator.comparingLong(e->e.getValue().waitingSince))
                    .toList();
            for(int idx=0;idx<waiting.size();idx++){
                var e=waiting.get(idx);
                int posX=dockX+dockW+40+idx*140;
                drawFerryWaiting(g2,e.getKey(),e.getValue(),posX,dockY-40,idx+1);
            }
        }

        /* helpers */
        private void drawFerry(Graphics2D g2,int id,FerryState s,int x,int y){
            g2.setColor(Color.WHITE); g2.fillRect(x,y,120,30);
            g2.setColor(Color.BLUE.darker()); g2.drawRect(x,y,120,30);
            g2.setFont(g2.getFont().deriveFont(11f));
            g2.drawString("Prom "+id,x+5,y+12);
            g2.drawString(s.load+"/"+s.capacity,x+5,y+24);
        }
        private void drawFerryWaiting(Graphics2D g2,int id,FerryState s,int x,int y,int pos){
            g2.setColor(new Color(255,255,255,200)); g2.fillRect(x,y,120,30);
            g2.setColor(Color.ORANGE.darker());
            float[] dash={4f,4f}; Stroke old=g2.getStroke();
            g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,1f,dash,0f));
            g2.drawRect(x,y,120,30); g2.setStroke(old);
            long w=(System.currentTimeMillis()-s.waitingSince)/1000;
            g2.drawString("kolejka #"+pos, x+5, y+10);
            g2.drawString("Prom "+id+" ("+w+"s)", x+5, y+24);
        }
    }
}
