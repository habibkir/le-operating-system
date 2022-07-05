/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito13set2021;

import java.util.ArrayList;

/**
 *
 * @author pierf
 */
public class Compito13Set2021 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int N = 5;
        int M = 5;
        Collector c = new Collector(N);
        OrderedQueue oq = new OrderedQueue();
        
        Generator[] g = new Generator[N];
        for(int i=0; i<g.length; i++) {
            g[i] = new Generator(c, i);
            g[i].setName("G"+i);
            g[i].start();
        }
        
        Processor[] p = new Processor[M];
        for(int i=0; i<p.length; i++) {
            p[i] = new Processor(c, oq);
            p[i].setName("P"+i);
            p[i].start();
        }
        
        Extractor e = new Extractor(oq);
        e.setName("E");
        e.start();
        
        Thread.sleep(30000);
        
        for(Generator x:g) {
            x.interrupt();
        }
        for(Processor x: p) {
            x.interrupt();
        }
        e.interrupt();
    }
    
}

class Msg {
    int p;
    Object[] d;

    public Msg(int p, Object[] d) {
        this.p = p;
        this.d = d;
    }
}

class Collector {
    Object[] data;
    int filled = 0;
    int currentP = 0;

    public Collector(int N) {
        data = new Object[N];
    }
    
    public synchronized void put(int generatorID, Object v) throws InterruptedException {
        while(data[generatorID]!=null)
            wait();
        data[generatorID] = v;
        filled++;
        notifyAll();
    }
    
    public synchronized Msg getData() throws InterruptedException {
        while(filled<data.length)
            wait();
        Msg r = new Msg(currentP++, data);
        filled = 0;
        data = new Object[data.length];
        notifyAll();
        return r;
    }
}

class Generator extends Thread {
    Collector c;
    int generatorID;

    public Generator(Collector c, int generatorID) {
        this.c = c;
        this.generatorID = generatorID;
    }
    
    public void run() {
        try {
            int p=0;
            while(true) {
                int v=100*generatorID+p;
                sleep(100+(int)(Math.random()*900));
                c.put(generatorID, v);
                p++;
            }
        } catch(InterruptedException e) {
            
        }
    }
}

class OrderedQueue {
    ArrayList<Msg> data = new ArrayList<>();
    
    synchronized void putMsg(Msg m) {
        data.add(m);
        notify();
    }
    
    Msg findMsg(int p) {
        for(Msg m: data)
            if(m.p == p)
                return m;
        return null;
    }
    
    synchronized Msg getMsg(int p) throws InterruptedException {
        Msg r = null;
        while((r=findMsg(p)) == null)
            wait();
        data.remove(r);
        return r;
    }
}

class Processor extends Thread {
    Collector c;
    OrderedQueue oq;

    public Processor(Collector c, OrderedQueue oq) {
        this.c = c;
        this.oq = oq;
    }
    
    public void run() {
        try {
            while(true) {
                Msg m = c.getData();
                int s = 0;
                for(Object o: m.d) {
                    s += (int)o;
                }
                m.d = new Object[]{s}; 
                sleep(100+(int)(Math.random()*900));
                oq.putMsg(m);
            }
        }catch(InterruptedException e) {
            
        }
    }
    
}

class Extractor extends Thread {
    OrderedQueue oq;

    public Extractor(OrderedQueue oq) {
        this.oq = oq;
    }
    
    public void run() {
        try {
            int p=0;
            while(true) {
                Msg m = oq.getMsg(p++);
                System.out.println(m.p+": "+m.d[0]);
            }
        } catch(InterruptedException e) {
            
        }
    }
}
