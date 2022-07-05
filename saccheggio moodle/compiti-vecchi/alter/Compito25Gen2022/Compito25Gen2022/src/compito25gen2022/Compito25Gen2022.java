/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito25gen2022;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito25Gen2022 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int M = 10;
        ArrayList<Integer> d=new ArrayList();
        int v=(int)(Math.random()*100);
        d.add(v);
        System.out.println("0 "+v);
        for(int i=0;i<M-1;i++) {
            v += 1 + (int)(Math.random()*99);
            System.out.println((i+1)+" "+v);
            d.add(v);
        }
        System.out.println();
        Container c = new Container(d);
        HashTable ht = new HashTable(M);
        
        Worker[] w=new Worker[4];
        for(int i=0;i<w.length;i++) {
            w[i] = new Worker(c,ht);
            w[i].setName("W"+i);
            w[i].start();
        }
        Collector cl = new Collector(ht,c);
        cl.start();
        cl.join();
        for(Worker k:w) {
            k.interrupt();
            k.join();
            System.out.println(k.getName()+" nOcc:"+k.nOccupato);
        }
    }    
}

class Container {
    ArrayList data;
    Semaphore pieni;
    Semaphore mutex = new Semaphore(1);

    public Container(ArrayList data) {
        this.data = data;
        this.pieni = new Semaphore(data.size());
    }
    
    public Object get() throws InterruptedException {
        pieni.acquire();
        mutex.acquire();
        int p = (int)(Math.random()*data.size());
        Object v = data.remove(p);
        mutex.release();
        return v;
    }
    
    public void add(Object[] o) throws InterruptedException {
        mutex.acquire();
        for(Object x: o)
            data.add(x);
        mutex.release();
        pieni.release(o.length);
    }
}

class HashTable {
    Object[] data;
    Semaphore mutex=new Semaphore(1);
    Semaphore piene = new Semaphore(0);

    public HashTable(int M) {
        data = new Object[M];
    }
    
    public int add(int v) throws InterruptedException {
        mutex.acquire();
        int nOccupato = 0;
        int p = v % data.length;
        while(data[p]!=null) {
            p = (p + 1) % data.length;
            nOccupato++;
            mutex.release();
            mutex.acquire();
        }
        data[p] = v;
        //System.out.println(Thread.currentThread().getName()+"inserito  "+p+" "+v);
        mutex.release();
        piene.release();
        return nOccupato;
    }
    
    public Object[] getAll() throws InterruptedException {
        piene.acquire(data.length);
        mutex.acquire();
        Object[] r = data;
        data = new Object[data.length];
        mutex.release();
        return r;
    }   
}

class Worker extends Thread {
    Container c;
    HashTable ht;
    int nOccupato = 0;

    public Worker(Container c, HashTable ht) {
        this.c = c;
        this.ht = ht;
    }
    
    
    public void run() {
        try {
            while(true) {
                int v = (Integer) c.get();
                //System.out.println(getName()+" "+v);
                nOccupato += ht.add(v);
            }
        } catch(InterruptedException e) {
            
        }
    }
}

class Collector extends Thread {
    HashTable ht;
    Container c;

    public Collector(HashTable ht, Container c) {
        this.ht = ht;
        this.c = c;
    }
    
    public void run() {
        try {
            for(int i=0;i<3; i++) {
                Object[] o = ht.getAll();
                for(int j=0;j<o.length; j++) {
                    System.out.println(j+" "+o[j]);
                }
                System.out.println();
                c.add(o);
            }
        } catch(InterruptedException e) {
            
        }
        System.out.println("Collector finito");
    }
}