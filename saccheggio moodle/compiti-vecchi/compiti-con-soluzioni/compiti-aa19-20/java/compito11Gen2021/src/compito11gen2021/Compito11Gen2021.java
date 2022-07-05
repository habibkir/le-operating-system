/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito11gen2021;

import java.util.ArrayList;

/**
 *
 * @author pierf
 */
public class Compito11Gen2021 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Queue q = new Queue();
        ResourceManager rm= new ResourceManager(2,2);
        int K = 6;
        
        Generator[] g=new Generator[3];
        for(int i=0;i<g.length;i++) {
            g[i]=new Generator(q,K);
            g[i].start();
        }
        
        Consumer[] c=new Consumer[K];
        for(int i=0;i<K;i++) {
            c[i]=new Consumer(q,rm,i);
            c[i].start();
        }
        
        for(int i=0;i<20;i++) {
            System.out.print("q: "+q.size());
            for(Consumer cc: c) {
                System.out.print(" "+cc.nMsgProcessed);
            }
            System.out.println(" rm:"+rm);
            Thread.sleep(1000);
        }
        
        for(int i=0;i<g.length;i++) {
            g[i].interrupt();
        }
        for(int i=0;i<c.length;i++) {
            c[i].interrupt();
            c[i].join();
        }
        System.out.println("ra:"+rm);
    }
    
}

class Msg {
    int type;
    int value;

    public Msg(int type, int value) {
        this.type = type;
        this.value = value;
    }
    
}

class Queue {
    private ArrayList<Msg> queue = new ArrayList<>();
    
    public synchronized void putMsg(Msg m) {
        queue.add(m);
        notifyAll();
    }
    
    public synchronized Msg getMsg(int type) throws InterruptedException {
        while(queue.size()==0 || queue.get(0).type!=type)
            wait();
        Msg m = queue.remove(0);
        return m;
    }
    
    public synchronized int size() {
        return queue.size();
    }
}

class ResourceManager {
    int nA,nB;

    public ResourceManager(int nA, int nB) {
        this.nA = nA;
        this.nB = nB;
    }
    
    public synchronized boolean acquire() throws InterruptedException {
        while(nA==0 && nB==0) {
            System.out.println("wait A & B");
            wait();
        }
        if(nA>0) {
            nA--;
            return true;
        }
        nB--;
        return false;
    }
    
    public synchronized void release(boolean resourceA) {
        if(resourceA)
            nA++;
        else
            nB++;
        notify();
    }
    
    @Override
    public String toString() {
        return "nA:"+nA+" nB:"+nB;
    }
}

class Generator extends Thread {
    private Queue q;
    private int K;

    public Generator(Queue q, int K) {
        this.q = q;
        this.K = K;
    }
    
    public void run() {
        //try {
            while(!interrupted()) {
                Msg m = new Msg((int)(Math.random()*K),10);
                q.putMsg(m);
            }
            
        //}
    }
    
}

class Consumer extends Thread {
    private Queue q;
    private ResourceManager rm;
    private int type;
    int nMsgProcessed = 0;

    public Consumer(Queue q, ResourceManager rm, int type) {
        this.q = q;
        this.rm = rm;
        this.type = type;
    }
    
    public void run() {
        try {
            while(true) {
                Msg m=q.getMsg(type);
                boolean ra=rm.acquire();
                try {
                    sleep(100);
                } finally {
                    rm.release(ra);
                }
                nMsgProcessed++;
            }
        } catch(InterruptedException e) {
            
        }
    }
}
