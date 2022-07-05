package it.unifi;

import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ResourceManager rm = new ResourceManager(10);
        WorkerManager wm = new WorkerManager(20, rm);
        Requester[] r= new Requester[10];
        for(int i=0; i<r.length; i++) {
            r[i]=new Requester(i,wm);
            r[i].start();
        }
        //aspetta che tutti i requester terminino
        for(Requester x:r) {
            x.join();
        }
        wm.interrupt();
        //stampa quante volte ogni worker è stato usato
        int s=0;
        for(int i=0;i<wm.w.length; i++) {
            System.out.println(i+" "+wm.w[i].nUso);
            s+=wm.w[i].nUso;
        }
        System.out.println(s);
        s=0;
        for(int i=0;i<r.length; i++) {
            System.out.println(i+" "+r[i].nReq);
            s+=r[i].nReq;
        }
        System.out.println(s);
    }
}

class Worker extends Thread {
    boolean used;
    private ResourceManager rm;
    private Semaphore input = new Semaphore(0);
    public Semaphore output = new Semaphore(0);
    public Object value;
    public Object result;
    public int nUso;

    public Worker(ResourceManager rm) {
        this.rm=rm;
    }

    public void run() {
        try {
            while(true) {
                //aspetta se non c'è un input
                input.acquire();
                nUso++;
                rm.getResource();
                result = (int)value * 2;
                sleep(100);
                rm.releaseResource();
                //il risultato è pronto
                output.release();
            }
        } catch(InterruptedException e) {

        }
    }

    public void processValue(Object v) {
        value = v;
        input.release();
    }

    public Object getResult() throws InterruptedException {
        //aspetta un output
        output.acquire();
        return result;
    }
}

class WorkerManager {
    protected Worker[] w;
    private Semaphore s;
    private Semaphore mutex = new Semaphore(1);
    public WorkerManager(int n, ResourceManager rm) {
        w = new Worker[n];
        s = new Semaphore(n);
        for(int i=0; i<w.length; i++) {
            w[i] = new Worker(rm);
            w[i].start();
        }
    }

    public Worker[] getWorkers(int n) throws InterruptedException {
        //aspetta se non ci sono n worker disponibili
        s.acquire(n);
        Worker[] r = new Worker[n];
        mutex.acquire();
        //sezione critica evita che altri thread acquisiscano gli stessi worker
        int i=0;
        for(Worker x:w) {
            if(!x.used) {
                r[i] = x;
                r[i].used = true;
                i++;
                if(i==n)
                    break;
            }
        }
        mutex.release();
        return r;
    }

    public void releaseWorkers(Worker[] ws) {
        for(Worker w:ws) {
            w.used = false;
        }
        s.release(ws.length);
    }

    public void interrupt() {
        for(Worker x:w) {
            x.interrupt();
        }
    }
}

class Requester extends Thread {
    WorkerManager wm;
    int id;
    int nReq;
    public Requester(int id, WorkerManager wm) {
        this.wm=wm;
        this.id = id;
    }
    public void run(){
        try {
            int v=0;
            while(v<100) {
                //acquisisce gli worker
                Worker[] ws = wm.getWorkers(2+(int)(Math.random()*9));
                for(int i=0;i<ws.length; i++) {
                    //chiede ad ciascun worker di processare un valore
                    ws[i].processValue((i+1)*100+v);
                    nReq++;
                }
                for(int i=0;i<ws.length; i++) {
                    //prende il risultato di ogni worker
                    int r = (int)ws[i].getResult();
                    System.out.println("R"+id+" "+((i+1)*100+v)+"->"+r);
                    if(r!=((i+1)*100+v)*2)
                        System.out.println("ERRORE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
                v++;
                //rilascia gli worker
                wm.releaseWorkers(ws);
            }
        } catch(InterruptedException e) {

        }
    }
}

class ResourceManager {
    private Semaphore s;
    public ResourceManager(int n) {
        s = new Semaphore(n);
    }

    public void getResource() throws InterruptedException {
        s.acquire();
    }

    public void releaseResource() {
        s.release();
    }
}