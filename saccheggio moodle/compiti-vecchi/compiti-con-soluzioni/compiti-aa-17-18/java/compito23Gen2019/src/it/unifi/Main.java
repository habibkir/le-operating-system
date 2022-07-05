package it.unifi;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws InterruptedException {
	    Queues q = new Queues();
	    int T = 10;
	    Generator g0 = new Generator(0, T, q);
	    Generator g1 = new Generator(1, T, q);
	    Generator g2 = new Generator(2, T, q);
	    g0.start();
	    g1.start();
	    g2.start();
	    Processor p = new Processor(T, q);
	    p.start();
	    g0.join();
	    g1.join();
	    g2.join();
	    System.out.println("Generazione terminata");
	    q.setEnd();
    }
}

class Queues {
    private ArrayList p0 = new ArrayList<>();
    private ArrayList p1 = new ArrayList<>();
    private ArrayList p2 = new ArrayList<>();

    private boolean generationEnded = false;

    public synchronized void add(int priority, Object o) {
        if(priority==0)
            p0.add(o);
        else if(priority==1)
            p1.add(o);
        else if(priority==2)
            p2.add(o);
        notifyAll();
    }

    public synchronized Object get() throws InterruptedException {
        while(!generationEnded && p0.isEmpty() && p1.isEmpty() && p2.isEmpty())
            wait();
        Object r;
        if(!p2.isEmpty())
            r = p2.remove(0);
        else if(!p1.isEmpty())
            r = p1.remove(0);
        else if(!p0.isEmpty())
            r = p0.remove(0);
        else
            r = null;
        return r;
    }

    public void setEnd() {
        generationEnded = true;
    }
}

class Generator extends Thread {
    private int priority;
    private int T;
    private Queues q;

    public Generator(int p, int T, Queues q) {
        priority = p;
        this.T = T;
        this.q = q;
    }

    public void run() {
        try {
            for (int i = 0; i < 1000; i++) {
                int v = priority * 1000 + i;
                q.add(priority, v);
                Thread.sleep(T);
            }
        } catch(InterruptedException e) {

        }
    }
}

class Processor extends Thread {
    private int T;
    private Queues q;

    public Processor(int T, Queues q) {
        this.T = T;
        this.q = q;
    }

    public void run() {
        try {
            Object o;
            do {
                o = q.get();
                System.out.println(o);
                Thread.sleep(T);
            } while(o!=null);
        } catch(InterruptedException e) {

        }
    }
}