/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito10giu2020;

import static java.lang.Thread.sleep;
import java.util.ArrayList;

/**
 *
 * @author pierf
 */
public class Compito10Giu2020 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int N=2;
        int M=4;
        int P=3;
        int T1=3;
        int T2=0;
        PriorityResourceManager rm = new PriorityResourceManager(N, P);
        Requester[] rq=new Requester[M];
        for(int i=0;i<M;i++) {
            rq[i]=new Requester(rm, i, i<P? i : P-1, T1, T2);
            rq[i].setName("R"+i);
            rq[i].start();
            Thread.sleep(1000);
        }
        Thread.sleep(60000);
        for(int i=0;i<rq.length;i++) {
            rq[i].interrupt();
        }
        for(int i=0;i<rq.length;i++) {
            rq[i].join();
            System.out.println("R"+i+": "+rq[i].count);
        }
    }
    
}

class PriorityResourceManager {
    int nResources;
    ArrayList<Integer>[] requests;

    public PriorityResourceManager(int maxResources, int nPri) {
        nResources = maxResources;
        this.requests = new ArrayList[nPri];
        for(int i=0;i<nPri;i++)
            this.requests[i]=new ArrayList<>();
    }
   
    public synchronized void acquire(int id, int priority) throws InterruptedException {
        requests[priority].add(id);
        while(check(id,priority)) {
            System.out.println("R"+id+" wait");
            wait();
        }
        nResources--;
    }
    
    private boolean check(int id, int priority) { 
        //attende se non ci sono risorse
        if(nResources==0)
            return true;
        //attende se ci sono thread in attesa a piu' alta priorita'
        for(int i=0;i<priority; i++) {
            if(requests[i].size()>0)
                return true;
        }
        //non attende se il thread e' il primo in coda
        if(requests[priority].get(0)==id) {
            requests[priority].remove(0);
            return false;
        }
        return true;
    }
    
    public synchronized void release() {
        nResources++;
        System.out.println(Thread.currentThread().getName()+" released");
        notifyAll();
    }
}

class Requester extends Thread {
    PriorityResourceManager rm;
    int id;
    int pri;
    int T1,T2;
    int count;

    public Requester(PriorityResourceManager rm, int id, int pri, int T1, int T2) {
        this.rm = rm;
        this.id = id;
        this.pri = pri;
        this.T1 = T1;
        this.T2 = T2;
    }
    
    public void run() {
        try {
            while(true) {
                //System.out.println(getName()+" "+pri+" requesting");
                rm.acquire(id, pri);
                System.out.println(getName()+" "+pri+" acquired");
                try {
                    sleep(T1*1000);
                    //System.out.println(getName()+" "+pri+" released");
                    count++;
                } finally {
                    rm.release();
                }
                sleep(T2*1000);
            }
        } catch(InterruptedException e) {
            
        }
    }
}