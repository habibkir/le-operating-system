/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito21lug2021;

import java.util.ArrayList;

/**
 *
 * @author pierf
 */
public class Compito21Lug2021 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        RequestQueue rq = new RequestQueue(60);

        ClientThread[] ct = new ClientThread[10];
        for (int i = 0; i < ct.length; i++) {
            ct[i] = new ClientThread(rq);
            ct[i].setName("CT" + i);
            ct[i].start();
        }

        WorkerThread[] wt = new WorkerThread[5];
        for(int i=0;i<wt.length; i++) {
            wt[i] = new WorkerThread(rq, 100);
            wt[i].setName("WT"+i);
            wt[i].start();
        }
        
        Thread.sleep(10000);
        
        for(int i=0;i<ct.length;i++)
            ct[i].interrompi = true;
        
        for(ClientThread c:ct) {
            c.join();
        }
        System.out.println("clients terminati");
        for(WorkerThread w:wt) {
            w.interrupt();
            w.join();
            System.out.println(w.getName()+" served:"+w.nServed);
        }
    }

}

class Request {

    Object data;
    int nReq;
    ResultCollector collector;

    public Request(Object data, int nReq, ResultCollector collector) {
        this.data = data;
        this.nReq = nReq;
        this.collector = collector;
    }
}

class RequestQueue {

    ArrayList<Request> data = new ArrayList<>();
    int K;

    public RequestQueue(int K) {
        this.K = K;
    }

    public synchronized void add(Request r) throws InterruptedException {
        while (data.size() >= K) {
            wait();
        }
        data.add(r);
        notifyAll();
    }

    public synchronized Request get() throws InterruptedException {
        while (data.size() == 0) {
            wait();
        }
        Request r = data.remove(0);
        notifyAll();
        return r;
    }
}

class ResultCollector {

    int nReq;
    Object[] results;
    int n;

    public void reset(int nr) {
        nReq = nr;
        results = new Object[nr];
        n = 0;
    }

    public synchronized void putResult(Object result, int nresult) {
        results[nresult] = result;
        n++;
        if (n == nReq) {
            notifyAll();
        }
    }

    public synchronized void waitResults() throws InterruptedException {
        while (n < nReq) {
            wait();
        }
    }
}

class ClientThread extends Thread {

    RequestQueue rq;
    ResultCollector rc = new ResultCollector();
    boolean interrompi = false;

    public ClientThread(RequestQueue rq) {
        this.rq = rq;
    }

    public void run() {
        try {
            while (!interrompi) {
                long ts = System.currentTimeMillis();
                int nreq = 2 + (int) (Math.random() * 5);
                rc.reset(nreq);
                for (int i = 0; i < nreq; i++) {
                    rq.add(new Request((i + 1) * 10, i, rc));
                }
                rc.waitResults();
                int s = 0;
                for (int i = 0; i < nreq; i++) {
                    s += (Integer) rc.results[i];
                }
                System.out.println(getName() + " n:" + nreq + " sum:" + s
                        + " time:" + (System.currentTimeMillis() - ts));
            }
        } catch (InterruptedException e) {

        }
    }

}

class WorkerThread extends Thread {

    RequestQueue rq;
    int T1;
    int nServed = 0;

    public WorkerThread(RequestQueue rq, int T1) {
        this.rq = rq;
        this.T1 = T1;
    }

    public void run() {
        try {
            while (true) {
                Request r = rq.get();
                long t = System.currentTimeMillis();
                while ((System.currentTimeMillis() - t) < T1)
                        ;
                int v = (Integer) r.data;
                nServed++;
                r.collector.putResult(v * 2, r.nReq);
            }
        } catch (InterruptedException e) {
            System.out.println("worker interrotto");
        }
    }
}
