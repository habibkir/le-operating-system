/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito.pkg11giu2019;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito11Giu2019 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Counter c = new Counter();
        Queue q = new Queue(5);
        
        Requester[] rs = new Requester[4];
        for(int i=0;i<rs.length;i++) {
            rs[i]=new Requester(c, q);
            rs[i].setName("RQ"+i);
            rs[i].start();
        }
        
        WorkersManager wm = new WorkersManager(3);
        Assigner[] as=new Assigner[2];
        for(int i=0;i<as.length;i++) {
            as[i]=new Assigner(wm, q);
            as[i].start();
        }
        
        WorkersMonitor wmm = new WorkersMonitor(wm);
        wmm.start();
        
        Thread.sleep(20000);
        
        for(int i=0;i<rs.length;i++) {
            rs[i].setStop();
        }
        for(int i=0;i<rs.length;i++) {
            System.out.println("waiting RQ"+i);
            rs[i].join();
        }
        
        for(int i=0;i<as.length;i++)
            as[i].interrupt();
        for(int i=0;i<wm.ws.length;i++) {
            wm.ws[i].interrupt();
            wm.ws[i].join();
            System.out.println("worker "+i+" usato "+wm.ws[i].nUsed);
        }
        wmm.interrupt();
    }
}

class Counter {
    private int value = 0;
    private Semaphore mutex = new Semaphore(1);
    
    public int getValue() throws InterruptedException {
        mutex.acquire();
        int r = value++;
        mutex.release();
        return r;
    }
}

class Queue {
    private ArrayList data = new ArrayList();
    private Semaphore mutex = new Semaphore(1);
    private Semaphore piene = new Semaphore(0);
    private Semaphore vuote;

    public Queue(int max) {
        vuote = new Semaphore(max);
    }
    
    public void put(Object x) throws InterruptedException {
        vuote.acquire();
        mutex.acquire();
        data.add(x);
        mutex.release();
        piene.release();
    }
    
    public Object get() throws InterruptedException {
        piene.acquire();
        mutex.acquire();
        Object r = data.remove(0);
        mutex.release();
        vuote.release();
        return r;
    }
}

class Buffer {
    private Object v;
    private Semaphore pieno = new Semaphore(0);
    private Semaphore vuoto = new Semaphore(1);
    
    public void set(Object x) throws InterruptedException {
        vuoto.acquire();
        v = x;
        pieno.release();
    }
    
    public Object get() throws InterruptedException {
        pieno.acquire();
        Object r = v;
        v = null;
        vuoto.release();
        return r;
    }
}

class Msg {
    Object v;
    Requester r;
    int nFail = 0;

    public Msg(Object v, Requester r) {
        this.v = v;
        this.r = r;
    }
    
}

class Requester extends Thread {
    private Counter c;
    private Queue q;
    Buffer result;
    private boolean stop = false;

    public Requester(Counter c, Queue q) {
        this.c = c;
        this.q = q;
        this.result = new Buffer();
    }
    
    public void run() {
        try {
            while(!stop) {
                int v = c.getValue();
                long ts = System.currentTimeMillis();
                q.put(new Msg(v,this));
                Object r = result.get();
                long te = System.currentTimeMillis();
                System.out.println(getName()+" v:"+v+" r:"+r+" "+(te-ts)+"ms");
            }
        } catch(InterruptedException e) {
            
        }
    }
    
    public void setStop() {
        stop = true;
    }
}

class Worker extends Thread {
    boolean free = true;
    int nUsed = 0;
    long started;
    Buffer in = new Buffer();
    Msg curMsg;
    WorkersManager wm;

    public Worker(WorkersManager wm) {
        this.wm = wm;
    }
    
   
    public void run() {
        try {
            while(true) {
                curMsg = (Msg)in.get();
                started = System.currentTimeMillis();
                int x = (int) curMsg.v;
                if(Math.random()<0.1) {
                    //blocco
                    System.out.println(getName()+" bloccato");
                    int c=0;
                    while(true)
                        c++;
                } else {
                    curMsg.r.result.set(x*2);
                    wm.releaseWorker(this);
                }
            }
        } catch(InterruptedException e) {
            
        }
    }
}

class WorkersManager {
    Worker ws[];
    Semaphore mutex = new Semaphore(1);
    Semaphore wrkAvailable;

    public WorkersManager(int nW) {
        ws = new Worker[nW];
        for(int i=0;i<nW;i++) {
            ws[i] = new Worker(this);
            ws[i].setName("W"+i);
            ws[i].start();
        }
        wrkAvailable = new Semaphore(nW);
    }
    
    public Worker getWorker() throws InterruptedException {
        wrkAvailable.acquire();
        mutex.acquire();
        int minUsed = -1;
        int pmin = 0;
        for(int i=0;i<ws.length;i++) {
            if(ws[i].free && (minUsed==-1 || ws[i].nUsed<minUsed)) {
                minUsed = ws[i].nUsed;
                pmin = i;
            }
        }
        ws[pmin].free = false;
        ws[pmin].nUsed++;
        mutex.release();
        return ws[pmin];
    }
    
    public void releaseWorker(Worker w) {
        w.free = true;
        wrkAvailable.release();
    }
}

class Assigner extends Thread {
    WorkersManager wm;
    Queue q;

    public Assigner(WorkersManager wm, Queue q) {
        this.wm = wm;
        this.q = q;
    }
    
    public void run() {
        try {
            while(true) {
                Object m = q.get();
                Worker w = wm.getWorker();
                w.in.set(m);
            }
        } catch(InterruptedException e) {
            
        }
    }
    
}

class WorkersMonitor extends Thread {
    WorkersManager wm;
    int nRestart = 0;

    public WorkersMonitor(WorkersManager wm) {
        this.wm = wm;
    }
    
    public void run() {
        try {
            while(true) {
                sleep(100);
                for(int i=0;i<wm.ws.length;i++) {
                    if(!wm.ws[i].free && (System.currentTimeMillis()-wm.ws[i].started)>1000) {
                        System.out.println("W"+i+" è bloccato");
                        Msg msg = wm.ws[i].curMsg;
                        int nUsed = wm.ws[i].nUsed;
                        wm.ws[i].interrupt();
                        sleep(1000);
                        if(wm.ws[i].isAlive()) {
                            wm.ws[i].stop();
                        }
                        nRestart++;
                        wm.mutex.acquire();
                        wm.ws[i] = new Worker(wm);
                        wm.ws[i].setName("W"+i);
                        wm.ws[i].nUsed = nUsed;
                        wm.ws[i].start();
                        
                        if(msg.nFail<3) {
                            wm.ws[i].free = false;
                            msg.nFail++;
                            System.out.println("RQ failed "+msg.nFail);
                            wm.ws[i].in.set(msg);
                        } else {
                            System.out.println("RQ failed "+msg.nFail+" aborted");
                            msg.r.result.set(null);
                            wm.wrkAvailable.release();
                        }
                        wm.mutex.release();
                    }
                }
            }
        } catch(InterruptedException e) {
            
        }
    }
}