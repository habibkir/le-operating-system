package it.unifi;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        int M = 10;
        int G = 4;
        int K = 2;
	    Counter c = new Counter();
	    InputQueue iq = new InputQueue(M);
	    OutputQueue oq = new OutputQueue();

	    Generator[] gg= new Generator[G];
	    for(int i=0; i<gg.length; i++) {
	        gg[i]=new Generator(i+1,iq,c);
	        gg[i].setName("G"+i);
	        gg[i].start();
        }
	    Processor[] pp = new Processor[K];
	    for(int i=0; i<pp.length; i++) {
	        pp[i] = new Processor(iq, oq);
	        pp[i].setName("P"+i);
	        pp[i].start();
        }
	    Collector cl = new Collector(oq);
	    cl.start();
	    Thread.sleep(1000);
        for(int i=0; i<gg.length; i++) {
            gg[i].interrupt();
        }
        for(int i=0; i<pp.length; i++) {
            pp[i].interrupt();
        }
        cl.interrupt();

        cl.join();
        int totGen = 0;
        for(int i=0; i<gg.length; i++) {
            gg[i].join();
            totGen += gg[i].nMsg;
        }
        System.out.println("totGen:"+totGen+" collected:"+cl.nMsg+" lost:"+iq.nMsgLost+" iq size:"+iq.size()+" oq size:"+oq.size());
        System.out.println("delta:"+(totGen-cl.nMsg-iq.nMsgLost-iq.size()-oq.size()));
    }
}

class Counter {
    private int count = 0;
    private Semaphore mutex = new Semaphore(1,true);

    public int getId() throws InterruptedException {
        mutex.acquire();
        count++;
        int r = count;
        mutex.release();
        return r;
    }
}

class Packet {
    public int id;
    public int[] data;
    public static final int N = 10;

    public Packet(int id, int[] data) {
        this.id = id;
        this.data = data;
    }
}

class InputQueue {
    private int M;
    private ArrayList q = new ArrayList<>();
    private Semaphore mutex = new Semaphore(1,true);
    private Semaphore piene = new Semaphore(0);
    public int nMsgLost = 0;

    InputQueue(int M) {
        this.M = M;
    }

    public void put(Object o) throws InterruptedException {
        mutex.acquire();
        if(q.size()==M) {
            Object x=q.remove(0);
            q.add(o);
            nMsgLost++;
            // System.out.println(Thread.currentThread().getName()+" eliminato "+((Packet)x).id);
        } else {
            q.add(o);
            piene.release();
        }
        //System.out.println(Thread.currentThread().getName()+" aggiunto "+((Packet)o).id);
        mutex.release();
    }

    public Object get() throws InterruptedException {
        Object r;
        piene.acquire(); //aspetta se piene == 0
        mutex.acquire();
        r = q.remove(0);
        //System.out.println(Thread.currentThread().getName()+" estratto "+((Packet)r).id);
        mutex.release();
        return r;
    }

    public int size() {
        return q.size();
    }
}

class OutputQueue {
    private ArrayList<Packet> queue = new ArrayList<>();
    Semaphore piene = new Semaphore(0);
    Semaphore mutex = new Semaphore(1);
    public void put(Packet p) throws InterruptedException {
        //inserimento ordinato in base ad id del pacchetto
        mutex.acquire();
        int i=0;
        while(i<queue.size() && queue.get(i).id<p.id)
            i++;
        if(i<queue.size())
            queue.add(i, p);
        else
            queue.add(p);
        /*String s="";
        for(Packet x: queue)
            s+=x.id+" ";
        System.out.println(p.id+" ["+s+"]");*/
        mutex.release();
        piene.release();
    }

    public Packet get() throws InterruptedException {
        piene.acquire();
        mutex.acquire();
        Packet p = queue.remove(0);
        mutex.release();
        return p;
    }

    public int size() {
        return queue.size();
    }
}

class Generator extends Thread {
    private Counter c;
    private InputQueue q;
    private int idg;
    public int nMsg;

    Generator(int idg, InputQueue q, Counter c) {
        this.idg = idg;
        this.q = q;
        this.c = c;
    }

    public void run() {
        try {
            while(true) {
                int id = c.getId();
                int[] d = new int[Packet.N];
                for(int i=0;i<d.length; i++)
                    d[i]=idg;
                q.put(new Packet(id, d));
                //incrementa il numero di messaggi generati
                nMsg++;
            }
        } catch(InterruptedException e) {

        }
    }
}

class Processor extends Thread {
    InputQueue iq;
    OutputQueue oq;

    Processor(InputQueue iq, OutputQueue oq) {
        this.oq = oq;
        this.iq = iq;
    }

    public void run() {
        try {
            while(true) {
                Packet p = (Packet) iq.get();
                int s = 0;
                for(int i=0; i<p.data.length; i++)
                    s+=p.data[i];
                //System.out.println(getName()+" id:"+p.id+" s:"+s);
                Packet outp = new Packet(p.id,new int[] {s});
                oq.put(outp);
            }
        } catch(InterruptedException e) {

        }
    }
}

class Collector extends Thread {
    private OutputQueue oq;
    public int nMsg = 0;
    public Collector(OutputQueue oq) {
        this.oq = oq;
    }

    @Override
    public void run() {
        try {
            while(true) {
                Packet p = oq.get();
                nMsg++;
                System.out.println("id:"+p.id+" "+p.data[0]);
            }
        } catch(InterruptedException e) {

        }
    }
}