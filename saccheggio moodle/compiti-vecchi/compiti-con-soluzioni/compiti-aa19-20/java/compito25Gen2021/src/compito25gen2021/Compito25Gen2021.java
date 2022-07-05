/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito25gen2021;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito25Gen2021 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Counter c = new Counter();
        Queue q = new Queue();
        
        Generator[] g= new Generator[3];
        for(int i=0;i<g.length;i++) {
            g[i]=new Generator(c,q,500);
            g[i].start();
        }
        Consumer[] cc = new Consumer[3];
        for(int i=0;i<cc.length;i++) {
            cc[i]=new Consumer(q,100);
            cc[i].setName("C"+i);
            cc[i].start();
        }
        Thread.sleep(10000);
        for(int i=0;i<g.length;i++)
            g[i].interrupt();
        
        q.closing = true;
        if(q.getSize()>0)
            q.empty.acquire();
        /*while(q.getSize()>0) {
            Thread.sleep(100);
        }*/
        System.out.println("c: "+c.v);
        for(int i=0;i<cc.length;i++)
            cc[i].interrupt();
        
    }
    
}

class Msg {
    int t;
    int v;

    public Msg(int t, int v) {
        this.t = t;
        this.v = v;
    }
    
    @Override
    public String toString() {
        return "(t: "+t+", v:"+v+")";
    }
}

class Counter {
    int v;
    private Semaphore mutex = new Semaphore(1);
    
    public int getValue() throws InterruptedException {
        mutex.acquire();
        ++v;
        int r=v;
        mutex.release();
        return r;
    }
}

class Queue {
    private ArrayList<Msg> queue = new ArrayList<>();
    Semaphore mutex = new Semaphore(1);
    Semaphore pieni = new Semaphore(0);
    Semaphore empty = new Semaphore(0);
    boolean closing = false;
    
    public void putMsg(Msg m) throws InterruptedException {
        mutex.acquire();
        queue.add(m);
        mutex.release();
        pieni.release();
    }
    
    public Msg getMsg() throws InterruptedException {
        pieni.acquire();
        mutex.acquire();
        
        int pmin=0;
        for(int i=1;i<queue.size(); i++) {
            if(queue.get(i).t<queue.get(pmin).t) {
                pmin=i;
            }
        }
        Msg m = queue.remove(pmin);
        if(closing && queue.size()==0)
            empty.release();
        mutex.release();
        return m;
    }
    
    public int getSize() throws InterruptedException {
        mutex.acquire();
        int s = queue.size();
        mutex.release();
        return s;
    }
}

class Generator extends Thread {
    private Counter c;
    private Queue q;
    private int TG;

    public Generator(Counter c, Queue q, int TG) {
        this.c = c;
        this.q = q;
        this.TG = TG;
    }

    
    public void run() {
        try {
            while(true) {
                int t = c.getValue();
                q.putMsg(new Msg(t,(int)(Math.random()*100)));
                sleep(TG);
            }
        } catch(InterruptedException e) {
            
        }
    }
}

class Consumer extends Thread {
    private Queue q;
    private int TC;

    public Consumer(Queue q, int TC) {
        this.q = q;
        this.TC = TC;
    }

    public void run() {
        try {
            while(true) {
                Msg m=q.getMsg();
                System.out.println(getName()+" msg: "+m);
                sleep(TC);
            }
        } catch(InterruptedException e) {
            
        }
    }
    
}