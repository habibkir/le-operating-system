/*
 * 
 */
package compito13feb2018;

import java.util.concurrent.Semaphore;

/**
 *
 * @author bellini
 */
public class Compito13Feb2018 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int N=6;
        int M=10;
        WorkerManager wm = new WorkerManager(N);
        Producer p = new Producer();
        p.start();
        Process[] pp=new Process[M];
        for(int i=0; i<pp.length; i++) {
            pp[i]=new Process(wm, p);
            pp[i].setName("P"+i);
            pp[i].start();
        }
        Thread.sleep(5000);
        p.interrupt();
        for(int i=0; i<pp.length; i++) {
            pp[i].interrupt();
        }
        wm.interrupt();
    }
    
}

class IntBuffer {
    private int v;
    private Semaphore pieni = new Semaphore(0);
    private Semaphore vuoti = new Semaphore(1);
    
    public int get() throws InterruptedException {
        pieni.acquire();
        int r = v;
        vuoti.release();
        return r;
    }
    
    public void put(int x) throws InterruptedException {
        vuoti.acquire();
        v = x;
        pieni.release();
    }
}

class Int2Buffer {
    private int v;
    private int c;
    private Semaphore pieni = new Semaphore(0);
    private Semaphore vuoti = new Semaphore(1);
    
    public int[] get() throws InterruptedException {
        pieni.acquire();
        int[] r = new int[2];
        r[0] = c;
        r[1] = v;
        vuoti.release();
        return r;
    }
    
    public void put(int cc, int vv) throws InterruptedException {
        vuoti.acquire();
        v = vv;
        c = cc;
        pieni.release();
    }
}
class Producer extends Thread {
    public IntBuffer b = new IntBuffer();
    
    public void run() {
        try {
            int i=0;
            while(true) {
                System.out.println("G: "+i);
                b.put(i++);
                //sleep(100);
            }
        }catch(InterruptedException e) {
            
        }
    } 
}

class WorkerManager {
    private Worker[] w;
    private boolean[] busy;
    private Semaphore available;
    private Semaphore mutex = new Semaphore(1);
    
    public WorkerManager(int N) {
        w = new Worker[N];
        busy = new boolean[N];
        available = new Semaphore(N);
        for(int i=0;i<N; i++) {
           w[i]=new Worker();
           w[i].start();
        }
    }
    
    public Worker[] getWorkers(int n) throws InterruptedException {
        available.acquire(n);
        mutex.acquire();
        Worker[] r=new Worker[n];
        int x = 0;
        for(int i=0; x<n && i<busy.length; i++) {
            if(!busy[i]) {
                r[x++]=w[i];
                busy[i]=true;
            }
        }
        mutex.release();
        return r;
    }
    
    public void releaseWorkers(Worker[] ww) throws InterruptedException {
        mutex.acquire();
        for(int i=0; i<busy.length; i++) {
            for(int j=0; j<ww.length; j++) {
                if(w[i]==ww[j]) {
                    busy[i]=false;
                }
            }
        }
        mutex.release();
        available.release(ww.length);
    }
    
    public void interrupt() {
        for(int i=0; i<w.length;i++)
            w[i].interrupt();
    }
}

class Worker extends Thread {
    public Int2Buffer in = new Int2Buffer();
    public IntBuffer out = new IntBuffer();
    
    public void run() {
        try {
        while(true) {
            int[] v = in.get();
            switch(v[0]) {
                case 0:
                    out.put(v[1]*2);
                    break;
                case 1:
                    out.put(v[1]+3);
                    break;
                case 2:
                    out.put(v[1]-4);
                    break;
            }
        }
        } catch(InterruptedException e) {
            
        }
    }
}

class Process extends Thread {
    WorkerManager wm;
    Producer p;

    public Process(WorkerManager wm, Producer p) {
        this.wm = wm;
        this.p = p;
    }
    
    public void run() {
        try {
            while(true) {
                int v = p.b.get();
                Worker[] w=wm.getWorkers(3);
                w[0].in.put(0, v);
                w[1].in.put(1,v);
                w[2].in.put(2, v);
                int s=0;
                for(int i=0;i<w.length; i++) {
                    s+=w[i].out.get();
                }
                System.out.println(getName()+":"+v+" --> "+s+" "+(4*v-1));
                wm.releaseWorkers(w);
            }
        } catch(InterruptedException e){
            
        }
    }
}