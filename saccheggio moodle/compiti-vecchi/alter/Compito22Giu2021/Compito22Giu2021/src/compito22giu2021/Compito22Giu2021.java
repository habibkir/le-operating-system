/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito22giu2021;

import java.util.ArrayList;

/**
 *
 * @author pierf
 */
public class Compito22Giu2021 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        LocationTracker lt = new LocationTracker();
        ImageQueue iq = new ImageQueue(5);
        Veicolo[] v = new Veicolo[10];
        Uploader[] u = new Uploader[v.length];
        UploadQueue[] uq = new UploadQueue[v.length];
        
        for(int i=0; i<v.length; i++) {
            uq[i] = new UploadQueue();
            u[i] = new Uploader(uq[i],iq);
            u[i].start();
            v[i] = new Veicolo(lt,uq[i],Math.random()*20-10,Math.random()*20-10);
            v[i].start();
        }
        
        ImageCollector[] ic = new ImageCollector[2];
        for(int i=0;i<ic.length; i++) {
            ic[i] = new ImageCollector(iq, 4000);
            ic[i].start();
        }
        for(int i=0;i<30; i++) {
            Thread.sleep(1000);
            int[] p = lt.getCounters();
            String s = "";
            for(int j=0; j<uq.length; j++) {
                s += "v"+j+" = "+uq[j].toString()+"\n";
            }
            System.out.println("H: "+p[0]+" M:"+p[1]+" L:"+p[2]+"\n"+s);
        }
        
        for(Veicolo vv: v) {
            vv.interrupt();
        }
        for(Uploader uu: u) {
            uu.interrupt();
        }
        for(ImageCollector i: ic) {
            i.interrupt();
        }
        int nPos = 0;
        for(Veicolo vv: v) {
            vv.join();
            nPos += vv.nPos;
        }
        for(Uploader uu: u) {
            uu.join();
        }
        for(ImageCollector i: ic) {
            i.join();
        }
        System.out.println(nPos+" "+lt.nReq);
    }
    
}

class LocationTracker {
    int[] counters = new int[3];
    int nReq = 0;
    
    public synchronized int getPriority(double x, double y) {
        double dist = Math.sqrt(x*x+y*y);
        nReq++;
        if(dist<=5) {
            counters[0]++;
            return 0; //priorità alta
        } else if(dist <= 10) {
            counters[1]++;
            return 1; // media priorità
        } else {
            counters[2]++;
            return 2;
        }
    }
    
    public synchronized int[] getCounters() {
        int[] r = counters;
        counters = new int[3];
        return r;
    }
}

class Image {
    int priority;
    String veicolo;

    public Image(int priority, String veicolo) {
        this.priority = priority;
        this.veicolo = veicolo;
    }

        
}

class UploadQueue {
    ArrayList<Image>[] queues;

    public UploadQueue() {
        queues=new ArrayList[3];
        for(int i=0;i<queues.length; i++)
            queues[i] = new ArrayList<>();
    }
    
    public synchronized void add(Image i) {
        queues[i.priority].add(i);
        notifyAll();
    }
    
    public synchronized Image get() throws InterruptedException {
        while(queues[0].size()==0 && queues[1].size()==0 && queues[2].size()==0) {
            wait();
        }
        Image r;
        if(queues[0].size()>0) {
            r = queues[0].remove(0);
        } else if(queues[1].size()>0) {
            r = queues[1].remove(0);
        } else {
            r = queues[2].remove(0);
        }
        return r;
    }
    
    public String toString() {
        return "h:"+queues[0].size()+" m:"+queues[1].size()+" l:"+queues[2].size();
    }
}

class ImageQueue {
    ArrayList<Image> data = new ArrayList<>();
    int size;

    public ImageQueue(int size) {
        this.size = size;
    }
    
    public synchronized void add(Image i) throws InterruptedException {
        while(data.size()>=size)
            wait();
        data.add(i);
        notifyAll();
    }
    
    public synchronized Image get() throws InterruptedException {
        while(data.size()==0)
            wait();
        Image r = data.remove(0);
        notifyAll();
        return r;
    }
}

class Veicolo extends Thread {
    LocationTracker lt;
    UploadQueue uq;
    double x,y;
    int nPos = 0;

    public Veicolo(LocationTracker lt, UploadQueue uq, double x, double y) {
        this.lt = lt;
        this.uq = uq;
        this.x = x;
        this.y = y;
    }
    
    public void run() {
        try {
            while(true) {
                int priority = lt.getPriority(x, y);
                Image img = new Image(priority, getName());
                uq.add(img);
                x += Math.random()*2-1;
                y += Math.random()*2-1;
                nPos++;
                sleep(1000);
            }
        } catch(InterruptedException e) {
            
        }
    }
}

class Uploader extends Thread {
    UploadQueue uq;
    ImageQueue iq;

    public Uploader(UploadQueue uq, ImageQueue iq) {
        this.uq = uq;
        this.iq = iq;
    }
    
    public void run() {
        try {
            while(true) {
                Image img = uq.get();
                sleep(100);
                iq.add(img);
            }
        } catch(InterruptedException e) {
            
        }
    }
}

class ImageCollector extends Thread {
    ImageQueue iq;
    int T1;

    public ImageCollector(ImageQueue iq, int T1) {
        this.iq = iq;
        this.T1 = T1;
    }
    
    public void run() {
        try {
            while(true) {
                Image i = iq.get();
                sleep(T1*1000);
                System.out.println(getName()+ " image "+i.priority+" "+i.veicolo);
            }
        } catch(InterruptedException e) {
            
        }
    }
}