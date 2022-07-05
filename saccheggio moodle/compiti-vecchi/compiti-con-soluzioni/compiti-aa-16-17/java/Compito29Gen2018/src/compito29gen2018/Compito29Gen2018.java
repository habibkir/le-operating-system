/*
 * 
 */
package compito29gen2018;

import static java.lang.Thread.sleep;
import java.util.concurrent.Semaphore;
import javax.annotation.Resource;

/**
 *
 * @author bellini
 */
public class Compito29Gen2018 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        Resources res = new Resources();
        Buffer b1 = new Buffer();
        Buffer b2 = new Buffer();
        
        Phase1 p1=new Phase1(b1, res);
        Phase2 p2=new Phase2(b1, b2, res);
        Phase3 p3=new Phase3(b2, res);
        p1.start();
        p2.start();
        p3.start();
        
        Thread.sleep(20000);
        p1.interrupt();
        p2.interrupt();
        p3.interrupt();
        
        //attendo che terminino i thread interrotti
        p1.join();
        p2.join();
        p3.join();
        System.out.println("a:"+res.a.availablePermits()+" b:"+res.b.availablePermits()+" c:"+res.c.availablePermits());
    }
    
}

class Buffer {
    private Object obj;
    private Semaphore piene = new Semaphore(0);
    private Semaphore vuote = new Semaphore(1);
    
    public void put(Object o) throws InterruptedException {
        vuote.acquire();
        //mutex.acquire();
        obj = o;
        //mutex.release();
        piene.release();
    }
    
    public Object get() throws InterruptedException {
        piene.acquire();
        //mutex.acquire();
        Object r = obj;
        obj=null;
        //mutex.release();
        vuote.release();
        return r;
    }
}

class Resources {
    public Semaphore a=new Semaphore(4);
    public Semaphore b=new Semaphore(3);
    public Semaphore c=new Semaphore(2);
}

class Phase1 extends Thread {
    private Buffer out;
    private Resources r;
    
    public Phase1(Buffer b, Resources r) {
        out=b;
        this.r = r;
    }
    
    public void run() {
        try {
            int i=0;
            while(true) {
                boolean acqB = false;
                r.a.acquire(2);
                try {
                    r.b.acquire(2);
                    acqB = true;
                    //sleep(200);
                } finally {
                    r.a.release(2);
                    if(acqB)
                        r.b.release(2);
                }
                out.put(i++);
            }
        } catch(InterruptedException e) {
            System.out.println("Phase1 interrotta");
        }
    }
}

class Phase2 extends Thread {
    private Buffer in;
    private Buffer out;
    private Resources r;
    
    public Phase2(Buffer b1, Buffer b2, Resources r) {
        in = b1;
        out=b2;
        this.r = r;
    }
    
    public void run() {
        try {
            while(true) {
                int v = (int) in.get();
                boolean acqB=false, acqC=false;
                r.a.acquire(2);
                try {
                    r.b.acquire(2);
                    acqB=true;
                    r.c.acquire(2);
                    acqC=true;
                    //sleep(200);
                } finally {
                    r.a.release(2);
                    if(acqB)
                        r.b.release(2);
                    if(acqC)
                        r.c.release(2);
                }
                out.put(v*2);
            }
        } catch(InterruptedException e) {
            System.out.println("Phase2 interrotta");            
        }
    }
}

class Phase3 extends Thread {
    private Buffer in;
    private Resources r;
    
    public Phase3(Buffer b1, Resources r) {
        in = b1;
        this.r = r;
    }
    
    public void run() {
        try {
            while(true) {
                int v = (int) in.get();
                boolean acqC = false;
                r.b.acquire(2);
                try {
                    r.c.acquire(2);
                    acqC=true;
                    //sleep(200);
                } finally {
                    r.b.release(2);
                    if(acqC)
                        r.c.release(2);
                }
                System.out.println(v+1);
            }
        } catch(InterruptedException e) {
            System.out.println("Phase3 interrotta");            
        }
    }
}
