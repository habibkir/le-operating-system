/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito.pkg3feb2020;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito3feb2020 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Queue q = new Queue();
        SharedCounter sc = new SharedCounter();

        QueueMonitor qm = new QueueMonitor(q);
        qm.start();

        Producer[] p = new Producer[4];
        for (int i = 0; i < p.length; i++) {
            p[i] = new Producer(sc, q);
            p[i].start();
            Thread.sleep(10000);
        }
        Thread.sleep(60000);
        for (Producer pp : p) {
            pp.interrupt();
        }
        while (q.size() > 0) {
            Thread.sleep(1000);
        }
        qm.interrupt();
    }

}

class SharedCounter {

    private int v = 0;
    private Semaphore mutex = new Semaphore(1);

    public int getValue() throws InterruptedException {
        mutex.acquire();
        v++;
        int r = v;
        mutex.release();
        return r;
    }
}

class Queue {

    private ArrayList data = new ArrayList();
    private Semaphore mutex = new Semaphore(1);
    private Semaphore pieni = new Semaphore(0);

    public void put(Object o) throws InterruptedException {
        mutex.acquire();
        data.add(o);
        mutex.release();
        pieni.release();
    }

    public Object get() throws InterruptedException {
        pieni.acquire();
        mutex.acquire();
        Object r = data.remove(0);
        mutex.release();
        return r;
    }

    public int size() throws InterruptedException {
        mutex.acquire();
        int s = data.size();
        mutex.release();
        return s;
    }
}

class Producer extends Thread {

    private SharedCounter sc;
    private Queue q;

    public Producer(SharedCounter sc, Queue q) {
        this.sc = sc;
        this.q = q;
    }

    public void run() {
        try {
            while (true) {
                int v = sc.getValue();
                q.put(v);
                sleep(10);
            }
        } catch (InterruptedException e) {
            System.out.println("Producer " + getName() + " terminato");
        }
    }
}

class Consumer extends Thread {

    private Queue q;

    public Consumer(Queue q) {
        this.q = q;
    }

    public void run() {
        try {
            System.out.println("Consumer " + getName() + " partito");
            while (true) {
                Object o = q.get();
                sleep(20);
            }
        } catch (InterruptedException e) {
            System.out.println("Consumer " + getName() + " terminato");
        }
    }
}

class QueueMonitor extends Thread {

    private Queue q;
    private ArrayList<Consumer> cc = new ArrayList<>();

    public QueueMonitor(Queue q) {
        this.q = q;
    }

    public void run() {
        try {
            int M = 1000;
            Consumer c = new Consumer(q);
            c.start();
            cc.add(c);
            long lastStarted = 0;
            int nLow = 0;
            while (true) {
                int s = q.size();
                System.out.println("Queue size: " + s+" #consumer:"+cc.size());
                if (s > M * cc.size()) {
                    if (lastStarted == 0 || (System.currentTimeMillis() - lastStarted) > 5000) {
                        c = new Consumer(q);
                        c.start();
                        cc.add(c);
                        lastStarted = System.currentTimeMillis();
                    }
                }
                if (s < 10 && cc.size() >= 2) {
                    nLow++;
                    System.out.println("LOW "+nLow);
                    if (nLow == 3) {
                        c = cc.remove(0);
                        c.interrupt();
                        nLow = 0;
                    } 
                } else {
                    nLow = 0;
                }

                sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("QueueMonitor terminato");
            for (Consumer c : cc) {
                c.interrupt();
            }
        }
    }
}
