/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito3giu2021;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito3Giu2021 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        RequestQueue rq = new RequestQueue(5);
        ResourceManager rm = new ResourceManager(5,5);
        
        WorkerThread[] wt = new WorkerThread[5];
        for(int i=0; i<wt.length; i++) {
            wt[i] = new WorkerThread(rq,rm, 100, 100);
            wt[i].start();
        }
        
        ClientThread[] ct = new ClientThread[10];
        for(int i=0; i<ct.length; i++) {
            ct[i] = new ClientThread(rq);
            ct[i].setName("CT"+i);
            ct[i].start();
        }
        
        Thread.sleep(10000);
        for(ClientThread c:ct) {
            c.interrupt();
        }
        for(WorkerThread w:wt) {
            w.interrupt();
        }
        for(ClientThread c:ct) {
            c.join();
            System.out.println(c.getName()+" nReq:"+c.nReq+" maxReq:"+c.maxTimeReq+"ms avgReq:"+(c.sumTimeReq/c.nReq));
        }
        for(WorkerThread w:wt) {
            w.interrupt();
        }
        
        System.out.println("RA:"+rm.ra.availablePermits()+" RB:"+rm.rb.availablePermits());
    }
    
}

class Request {
    Object data;
    Object result = null;
    long start = 0;
    ClientThread sender;

    public Request(Object data, ClientThread sender) {
        this.data = data;
        this.sender = sender;
        this.start = System.currentTimeMillis();
    }
}

class RequestQueue {
    ArrayList<Request> data = new ArrayList();
    Semaphore mutex = new Semaphore(1);
    Semaphore piene = new Semaphore(0);
    Semaphore vuote;

    public RequestQueue(int n) {
        vuote = new Semaphore(n);
    }
    
    public void add(Request r) throws InterruptedException {
        vuote.acquire();
        mutex.acquire();
        data.add(r);
        mutex.release();
        piene.release();
    }
    
    public Request get() throws InterruptedException {
        piene.acquire();
        mutex.acquire();
        Request r = data.remove(0);
        mutex.release();
        vuote.release();
        return r;
    }
    
}

class ResourceManager {
    Semaphore ra;
    Semaphore rb;

    public ResourceManager(int NA, int NB) {
        ra = new Semaphore(NA);
        rb = new Semaphore(NB);
    }
    
    public void getA() throws InterruptedException {
        ra.acquire();
    }
    
    public void getB() throws InterruptedException {
        rb.acquire();
    }
    
    public void releaseA() {
        ra.release();
    }
    
    public void releaseB() {
        rb.release();
    }
}

class ClientThread extends Thread {
    RequestQueue rq;
    Semaphore reply = new Semaphore(0);
    int nReq = 0; //numero richieste fatte
    long sumTimeReq = 0; //somma tempi delle richieste
    long maxTimeReq = 0; //massimo tempo di richiesta

    public ClientThread(RequestQueue rq) {
        this.rq = rq;
    }
    
    public void run() {
        try {
            while(true) {
                Request r=new Request(Math.random()*100,this);                
                rq.add(r);
                reply.acquire();
                long timeReq = System.currentTimeMillis()-r.start;
                nReq++;
                sumTimeReq+=timeReq;
                if(timeReq>maxTimeReq)
                    maxTimeReq = timeReq;
                System.out.println(getName()+" "+r.data+" "+r.result+" el:"+timeReq);
            }
        } catch(InterruptedException e) {
            
        }
    } 
    
}

class WorkerThread extends Thread {
    RequestQueue rq;
    ResourceManager rm;
    int T1,T2;

    public WorkerThread(RequestQueue rq, ResourceManager rm, int T1, int T2) {
        this.rq = rq;
        this.rm = rm;
        this.T1 = T1;
        this.T2 = T2;
    }
    
    public void run() {
        try {
            while(true) {
                Request r = rq.get();
                rm.getA();
                try {
                    sleep(T1);                    
                    rm.getB();
                    try {
                        sleep(T2);
                    } finally {
                        rm.releaseB();
                    }
                } finally {
                    rm.releaseA();
                }
                r.result = ((Double) r.data)*2;
                
                r.sender.reply.release();
            }
        } catch(InterruptedException e) {
            
        }
    }
}
