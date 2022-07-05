/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito25nov2020;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito25Nov2020 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException{
        int N = 10;
        LayerQueue lq = new LayerQueue();
        ExtractQueue eq = new ExtractQueue(N);
        
        DownloadThread[] dt = new DownloadThread[3];
        for(int i=0; i<dt.length; i++) {
            dt[i] = new DownloadThread(lq,eq);
            dt[i].setName("DT"+i);
            dt[i].start();
        }
        
        ExtractorThread et = new ExtractorThread(eq, N);
        et.start();
        
        for(int i=0;i<N; i++) {
            int size = 1+(int)(Math.random()*100);
            System.out.println("layer "+i+" size="+size);
            lq.putLayer(new Layer(i,size));
        }
        
        et.join();
        for(int i=0; i<dt.length; i++) {
            dt[i].interrupt();
        }
    }
    
}

class Layer {
    int id;
    int size;

    public Layer(int id, int size) {
        this.id = id;
        this.size = size;
    }
}

class LayerQueue {
    private ArrayList<Layer> queue = new ArrayList<>();
    private Semaphore mutex = new Semaphore(1);
    private Semaphore pieni = new Semaphore(0);

    public LayerQueue() {
    }
    
    public Layer getLayer() throws InterruptedException {
        pieni.acquire();
        mutex.acquire();
        Layer l = queue.remove(0);
        mutex.release();
        return l;
    }
    
    public void putLayer(Layer l) throws InterruptedException {
        mutex.acquire();
        queue.add(l);
        mutex.release();
        pieni.release();
    }
}

class ExtractQueue {
    private Layer[] queue;
    private Semaphore[] avail;

    public ExtractQueue(int N) {
        queue = new Layer[N];
        avail = new Semaphore[N];
        for(int i=0;i<avail.length; i++)
            avail[i]=new Semaphore(0);
    }
    
    public void putLayer(Layer l) {
        queue[l.id] = l;
        avail[l.id].release();
    }
    
    public Layer getLayer(int i) throws InterruptedException {
        avail[i].acquire();
        Layer l = queue[i];
        queue[i] = null;
        return l;
    }
    
}

class DownloadThread extends Thread {
    private LayerQueue lq;
    private ExtractQueue eq;

    public DownloadThread(LayerQueue lq, ExtractQueue eq) {
        this.lq = lq;
        this.eq = eq;
    }
    
    public void run() {
        try {
            while(true) {
                Layer l = lq.getLayer();
                System.out.println(getName()+" inizio download layer "+l.id+" size "+l.size);
                sleep(l.size*100);
                System.out.println(getName()+" fine download layer "+l.id+" size "+l.size);
                eq.putLayer(l);
            }
        } catch(InterruptedException e) {
            System.out.println(getName()+" interrotto");
        }
    }
}

class ExtractorThread extends Thread {
    private ExtractQueue eq;
    private int n;

    public ExtractorThread(ExtractQueue eq, int n) {
        this.eq = eq;
        this.n = n;
    }
    
    public void run() {
        try {
            for(int i=0;i<n; i++) {
                System.out.println(getName()+" prendo layer "+i);
                Layer l = eq.getLayer(i);
                System.out.println(getName()+" inizio estrazione layer "+l.id+" size "+l.size);
                sleep(l.size*50);
                System.out.println(getName()+" fine estrazione layer "+l.id+" size "+l.size);
            }
        } catch(InterruptedException e) {
            
        }
    }
}