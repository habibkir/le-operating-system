/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito08feb2021;

/**
 *
 * @author pierf
 */
public class Compito08Feb2021 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int N = 5;
        DevicesStatus ds = new DevicesStatus(N);
        Reader[] r = new Reader[N];
        for(int i=0;i<N; i++) {
            r[i] = new Reader(new Device(i+1),ds);
            r[i].start();
        }
        
        Consumer c = new Consumer(ds);
        c.start();        
        
        Thread.sleep(10000);
        for(int i=0;i<N; i++) {
            r[i].interrupt();
        }
        c.interrupt();
        for(int i=0;i<N; i++) {
            r[i].join();
            System.out.println("R"+i+" nAcq="+r[i].nAcq);
        }
        
    }
    
}

class Device {
    int id;
    int v;

    public Device(int id) {
        this.id = id;
        v=id;
    }
    
    public double getValue() {
        v=(v+1)%5;
        return v;//id+Math.random()*.8-0.5;
    }
}

class DevicesStatus {
    private double[] values;
    private boolean[] set;
    private int nSet = 0;

    public DevicesStatus(int n) {
        values = new double[n];
        set = new boolean[n];
    }
    
    public synchronized void setValue(int id, double v) throws InterruptedException {
        while(set[id]==true)
            wait();
        set[id] = true;
        values[id] = v;
        nSet++;
        notifyAll();
    }
    
    public synchronized double[] getValues() throws InterruptedException {
        while(nSet<values.length)
            wait();
        nSet = 0;
        double[] r = values;
        values = new double[values.length];
        set = new boolean[set.length];
        notifyAll();
        return r;
    }
    
}

class Reader extends Thread {
    private Device d;
    private DevicesStatus ds;
    int nAcq = 0;

    public Reader(Device d, DevicesStatus ds) {
        this.d = d;
        this.ds = ds;
    }
    
    public void run() {
        try {
            while(true) {
                double v = d.getValue();
                ds.setValue(d.id-1, v);
                nAcq++;
                //sleep(200);
            }
        } catch(InterruptedException e) {
            
        }
    }
}

class Consumer extends Thread {
    private DevicesStatus ds;

    public Consumer(DevicesStatus ds) {
        this.ds = ds;
    }
    
    public void run() {
        try {
            while(true) {
                double[] v = ds.getValues();
                double sum = 0.0;
                for(int i=0;i<v.length; i++) {
                    sum+=v[i];
                    System.out.print(v[i]+" ");
                }
                System.out.println(" avg:"+(sum/v.length));
            }
        } catch(InterruptedException e) {
            
        }
    }
}