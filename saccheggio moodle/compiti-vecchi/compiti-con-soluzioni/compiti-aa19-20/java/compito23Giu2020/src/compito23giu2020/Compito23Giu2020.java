/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito23giu2020;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito23Giu2020 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int K1 = 5;
        int K2 = 8;
        int M = 6;
        
        int NC = (M*K1+K2-1)/K2;

        MsgQueue s = new MsgQueue(K2);
        MsgCollector c = new MsgCollector(NC);
        ProcessorThread[] t = new ProcessorThread[10];
        for (int i = 0; i < t.length; i++) {
            t[i] = new ProcessorThread(s, c, K2);
            t[i].setName("PR"+i);
            t[i].start();
        }
        PrinterThread pt = new PrinterThread(c, NC);
        pt.setName("PRT");
        pt.start();
        
        for (int i = 0; i < M; i++) {
            ArrayList<Integer> x = new ArrayList<>();
            for (int j = 0; j < K1; j++) {
                x.add(j);
            }
            s.put(x);
        }
        s.put(null);
        pt.join();
        for(int i=0;i<t.length; i++) {
            t[i].interrupt();
        }
    }
}

class MsgQueue {
    Semaphore pieni = new Semaphore(0);
    Semaphore mutex = new Semaphore(1);
    ArrayList<Integer> data = new ArrayList<>();
    int id = 0;
    int K2;

    public MsgQueue(int K2) {
        this.K2 = K2;
    }
    
    void put(ArrayList<Integer> v) throws InterruptedException {
        if (v == null) {
            pieni.release(K2);
            return;
        }
        mutex.acquire();
        data.addAll(v);
        mutex.release();
        pieni.release(v.size());
    }

    ArrayList<Integer> get(int n) throws InterruptedException {
        pieni.acquire(n);
        mutex.acquire();
        ArrayList<Integer> r = new ArrayList<>();
        r.add(id++);
        for (int i = 0; data.size() > 0 && i < n; i++) {
            r.add(data.remove(0));
        }
        mutex.release();
        if(r.size()==1)
            return null;
        return r;
    }
}

class MsgCollector {

    Semaphore[] s;
    ArrayList<Integer>[] data;

    public MsgCollector(int NC) {
        s=new Semaphore[NC];
        for(int i=0;i<s.length;i++)
            s[i] = new Semaphore(0);
        data = new ArrayList[NC];
    }

    void put(ArrayList<Integer> v) throws InterruptedException {
        data[v.get(0)] = v;
        s[v.get(0)].release();
    }

    ArrayList<Integer> get(int p) throws InterruptedException {
        s[p].acquire();
        return data[p];
    }
}

class ProcessorThread extends Thread {

    MsgQueue s;
    MsgCollector c;
    int K2;

    public ProcessorThread(MsgQueue s, MsgCollector c, int K2) {
        this.s = s;
        this.c = c;
        this.K2 = K2;
    }

    public void run() {
        try {
            while (true) {
                ArrayList<Integer> v = s.get(K2);
                if(v==null)
                    break;

                sleep(1000 + (int) (Math.random() * 2000));
                for (int i = 1; i < v.size(); i++) {
                    v.set(i,v.get(i)*2);                
                }
                c.put(v);
            }
        } catch (InterruptedException e) {

        }
    }
}

class PrinterThread extends Thread {
    private MsgCollector c;
    private int NC;

    public PrinterThread(MsgCollector c, int NC) {
        this.c = c;
        this.NC = NC;
    }

    public void run() {
        try {
            ArrayList<Integer> xx;
            for(int i=0;i<NC; i++) {
                xx = c.get(i);
                System.out.println(xx);
            }
        } catch(InterruptedException e) {
            
        }
    }
}
