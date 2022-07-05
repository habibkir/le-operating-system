/*
 * 
 */
package compito03lug2017;

/**
 *
 * @author bellini
 */
public class Compito03Lug2017 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int N = 6;
        int M = 50;
        Resources r = new Resources();
        WorkerManager wm = new WorkerManager(N);
        IntArrayBuffer iab = new IntArrayBuffer();
        
        Master[] masters = new Master[M];
        for(int i=0; i<masters.length; i++) {
            masters[i] = new Master(r, wm, iab);
            masters[i].setName("M"+i);
            masters[i].start();
        }
        int k=1;
        for(int i=0;i<2*M; i++) {
            int[] v = new int[100];
            for(int j=0;j<v.length; j++) {
                v[j] = k++;
            }
            iab.put(v);
        }
        iab.setStop();
        for(int i=0; i<masters.length; i++) {
            masters[i].join();
        }        
        wm.interrupt();
    }
    
}

class Resources {
    private int nA = 10;
    private int nB = 6;
    
    public synchronized boolean getResources() throws InterruptedException {
        while(nA<2 && nB<2) {
            wait();
        }
        if(nA>=2) {
            nA -= 2;
            return true; //preso risorsa A
        } else {
            nB -= 2;
            return false; //preso risorsa B
        }
    }
    
    public synchronized void releaseResources(boolean presoA) {
        if(presoA) {
            nA+=2;
        } else {
            nB+=2;
        }
        notify();
    }
} 

class IntArrayBuffer {
    private int[] v = null;
    private boolean stop = false;
    
    public synchronized void put(int[] x) throws InterruptedException {
        while(v!=null) {
            wait();
        }
        v = x;
        notifyAll();
    }
    
    public synchronized int[] get() throws InterruptedException {
        while(v==null && !stop) {
            wait();
        }
        if(v!=null) {
            int[] r = v;
            v = null;
            notifyAll();
            return r;
        } else {
            notifyAll();
            return null;
        }
    }
    
    public synchronized void setStop() {
        stop = true;
        notifyAll();
    }
}


class Worker extends Thread {
    private int[] v;
    private int start;
    private int result = -1;
    
    public void run() {
        try {
            while(true) {
                int[] r = get();
                int s = 0;
                for(int i=start; i<r.length; i+=2) {
                    s+=r[i];
                }
                System.out.println(getName()+" s="+s);
                setResult(s);
            }
        } catch(InterruptedException e) {
            
        }
    }
    
    public synchronized void put(int[] vv, int start) throws InterruptedException {
        while(v!=null) {
            wait();
        }
        v = vv;
        this.start = start;
        notifyAll();
    }
    
    public synchronized int getResult() throws InterruptedException {
        while(result == -1) {
            wait();
        }
        int r = result;
        result = -1;
        notifyAll();
        return r;
    }
    
    private synchronized int[] get() throws InterruptedException {
        while(v==null)
            wait();
        int[] r = v;
        v = null;
        notifyAll();
        return r;
    }
    
    private synchronized void setResult(int r) throws InterruptedException {
        while(result != -1)
            wait();
        result = r;
        notifyAll();
    }
}

class WorkerManager {
    private Worker[] workers;
    private int nFreeWorkers;
    
    public WorkerManager(int N) {
        workers = new Worker[N];
        for(int i=0; i<workers.length; i++) {
            workers[i] = new Worker();
            workers[i].setName("W"+i);
            workers[i].start();
        }
        nFreeWorkers = N;
    }
    
    public synchronized Worker[] getWorkers(int n) throws InterruptedException {
        while(nFreeWorkers<n) {
            wait();
        }
        Worker[] r = new Worker[n];
        nFreeWorkers-=n;
        int w=0;
        for(int i=0; i<workers.length && w<n; i++) {
            if(workers[i]!=null) {
                r[w++] = workers[i];
                workers[i] = null;
            }
        }
        return r;
    }
    
    public synchronized void releaseWorker(Worker[] w) {
        nFreeWorkers+=w.length;
        int ww = 0;
        for(int i=0; i<workers.length && ww<w.length; i++) {
            if(workers[i]==null) {
                workers[i] = w[ww++];
            }
        }
        notify();
    }
    
    public void interrupt() {
        for(int i=0; i<workers.length; i++) {
            workers[i].interrupt();
        }        
    }
}

class Master extends Thread {
    private Resources resources;
    private WorkerManager wm;
    private IntArrayBuffer iab;

    public Master(Resources resources, WorkerManager wm, IntArrayBuffer iab) {
        this.resources = resources;
        this.wm = wm;
        this.iab = iab;
    }
    
    public void run() {
        try {
            while(true) {
                int[] v=iab.get();
                if(v==null) {
                    break;
                }
                boolean presoA = resources.getResources();
                Worker[] w = wm.getWorkers(2);
                w[0].put(v, 0);
                w[1].put(v, 1);
                int r1 = w[0].getResult();
                int r2 = w[1].getResult();
                System.out.println(getName()+" somma="+(r1+r2));
                wm.releaseWorker(w);
                resources.releaseResources(presoA);
            }
        } catch (InterruptedException e) {
            
        }
    }
}










