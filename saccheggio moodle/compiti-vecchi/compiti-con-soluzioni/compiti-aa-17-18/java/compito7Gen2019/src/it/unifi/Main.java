package it.unifi;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        int n=5;
        int R=5;

        Buffer result = new Buffer(0,1);
        Controller[] ct=new Controller[n];
        ResourceManager rm = new ResourceManager(R);

        ct[n-1] = new Controller(n-1, null, result, rm);
        for(int i=n-2; i>=0; i--) {
            ct[i] = new Controller(i,ct[i+1].myBuffer, result, rm);
            ct[i].start();
        }
        ct[n-1].next = ct[0].myBuffer;
        ct[n-1].start();

        Collector c = new Collector(result);
        c.start();

        for(int i=0;i<100; i++) {
            System.out.println("GEN"+i);
            ct[0].myBuffer.add(i, false);
        }

    }
}

class Buffer {
    private ArrayList<Object> msgs = new ArrayList<>();
    private int max,id;

    public Buffer(int id,int mx) {
        max = mx;
        this.id=id;
    }
    synchronized void add(Object o, boolean print) throws InterruptedException {
        while(msgs.size()==max) {
            if(print)
                System.out.println(id+" MAX "+max);
            wait();
        }
        msgs.add(o);
        notifyAll();
    }

    synchronized Object get() throws InterruptedException {
        while(msgs.size()==0)
            wait();
        Object x = msgs.remove(0);
        notifyAll();
        return x;
    }
}

class Controller extends Thread {
    Buffer myBuffer;
    Buffer next;
    Worker myWorker;

    public Controller(int id, Buffer next, Buffer result, ResourceManager rm) {
        this.next = next;
        setName("Cnt"+id);
        myBuffer = new Buffer(id, 100);
        myWorker = new Worker(result, rm);
        myWorker.setName("W"+id);
        myWorker.start();
    }


    public void run() {
        try {
            while(true) {
                Object x = myBuffer.get();
                if(!myWorker.isWorking()) {
                    //System.out.println(getName()+" "+x+" to worker");
                    myWorker.add(x);
                } else {
                    //sleep(10);
                    //System.out.println(getName()+" "+x+" to next");
                    next.add(x, true);
                }
            }
        } catch(InterruptedException e) {

        }
    }
}

class ResourceManager {
    private int n;

    public ResourceManager(int NR) {
        n = NR;
    }

    public synchronized void acquire() throws InterruptedException {
        while(n==0)
            wait();
        n--;
    }
    public synchronized void release() {
        n++;
        notifyAll();
    }
}

class Worker extends Thread {
    private Buffer b = new Buffer(0,1);
    private Buffer result;
    private ResourceManager rm;
    private volatile boolean working = false;

    public Worker(Buffer result, ResourceManager rm) {
        this.result = result;
        this.rm = rm;
    }

    public void add(Object o) throws InterruptedException{
        b.add(o, false);
    }

    public boolean isWorking() {
        return working;
    }

    public void run() {
        try {
            while(true) {
                Object m = b.get();
                working = true;
                rm.acquire();
                //System.out.println(getName()+" START "+m);
                sleep(500);
                //System.out.println(getName()+" END "+m);
                rm.release();
                working = false;
                result.add(m, false);
            }
        } catch(InterruptedException e) {

        }
    }
}

class Collector extends Thread {
    Buffer in;

    public Collector(Buffer in) {
        this.in = in;
    }

    public void run() {
        try {
            int n=1;
            while(true) {
                System.out.println("Collector "+in.get()+" "+n++);
            }
        } catch(InterruptedException e) {

        }
    }
}