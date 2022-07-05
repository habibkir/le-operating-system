package it.unifi;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        int N = 10;
        int M = 5;
        int K = 20;

	    Counter c = new Counter();
	    MultiQueue mq = new MultiQueue(N, M);
	    MsgDispatcher md = new MsgDispatcher(K);

	    Producer[] p = new Producer[N];
	    for(int i = 0; i<p.length; i++) {
	        p[i] = new Producer(i,mq, c);
	        p[i].start();
        }
	    Consumer[]  cns = new Consumer[2];
	    for(int i=0; i<cns.length; i++) {
	        cns[i] = new Consumer(mq, md);
	        cns[i].setName("CNS"+i);
	        cns[i].start();
        }
	    Client[] cl = new Client[K];
	    for(int i=0; i<cl.length; i++) {
	        cl[i] = new Client(i, md);
	        cl[i].start();
        }
	    Thread.sleep(20000);
	    for(int i=0; i<p.length; i++)
	        p[i].interrupt();
	    for(int i=0; i<cns.length; i++)
	        cns[i].interrupt();
	    for(int i=0; i<cl.length; i++)
	        cl[i].interrupt();
    }
}

class Counter {
    private int count = 0;
    private Semaphore mutex = new Semaphore(1);

    public int getCount() throws InterruptedException {
        mutex.acquire();
        int r = count;
        count++;
        mutex.release();
        return r;
    }
}

class MultiQueue {
    private ArrayList<Integer>[] queue;
    private Semaphore[] mutex;
    private Semaphore[] pieni;
    private Semaphore[] vuoti;
    private Semaphore access;

    public MultiQueue(int N, int M) {
        queue = new ArrayList[N];
        mutex = new Semaphore[N];
        pieni = new Semaphore[N];
        vuoti = new Semaphore[N];
        access = new Semaphore(1);
        for(int i=0; i<N; i++) {
            queue[i] = new ArrayList<>();
            mutex[i] = new Semaphore(1);
            pieni[i] = new Semaphore(0);
            vuoti[i] = new Semaphore(M);
        }
    }

    public void putValue(int q, int v) throws InterruptedException {
        vuoti[q].acquire();
        mutex[q].acquire();
        queue[q].add(v);
        mutex[q].release();
        pieni[q].release();
    }

    public int getMax() throws InterruptedException {
        access.acquire();
        int max = -1;
        for(int i=0; i<queue.length; i++) {
            pieni[i].acquire();
            mutex[i].acquire();
            int v = queue[i].remove(0);
            mutex[i].release();
            vuoti[i].release();
            //System.out.println(Thread.currentThread().getName()+" v:"+v);
            if(v>max)
                max = v;
        }
        access.release();
        return max;
    }
}

class MsgDispatcher {
    private volatile Object msg;
    private Semaphore[] waitGet;
    private int cnt;
    private Semaphore mutex = new Semaphore(1);
    private Semaphore waitPut = new Semaphore(0);

    public MsgDispatcher(int K) {
        msg = null;
        waitGet = new Semaphore[K];
        for(int i=0; i<waitGet.length; i++)
            waitGet[i] = new Semaphore(0);
        cnt = 0;
    }

    public void putMsg(Object m) throws InterruptedException {
        mutex.acquire();
        //se c'è un messaggio ancora da inviare ai client aspetta
        while(msg != null) {
            mutex.release();
            //si mette in attesa
            waitPut.acquire();
            mutex.acquire();
            /*if(msg!=null)
                System.out.println(Thread.currentThread().getName()+" !!!! "+msg);*/
        }
        this.msg = m;
        //sveglia tutti i client che sono in attesa
        for(Semaphore s: waitGet) {
            s.release();
        }
        mutex.release();
    }

    public Object getMsg(int idClient) throws InterruptedException {
        //aspetta un messaggio
        waitGet[idClient].acquire();
        mutex.acquire();
        //prende il messaggio
        Object r = msg;
        //incrementa il numero di accessi da parte dei client
        cnt++;
        //se tutti i client hanno preso il messaggio sveglia un consumer
        if(cnt==waitGet.length) {
            msg = null;
            cnt = 0;
            waitPut.release();
        }
        mutex.release();
        return r;
    }
}

class Producer extends Thread {
    private int id;
    private MultiQueue mq;
    private Counter c;

    public Producer(int id, MultiQueue mq, Counter c) {
        this.id = id;
        this.mq = mq;
        this.c = c;
        setName("P"+id);
    }

    @Override
    public void run() {
        try {
            while(true) {
                int v = c.getCount();
                mq.putValue(id, v);
            }
        } catch(InterruptedException e) {
            System.out.println("Producer "+id+" interrotto");
        }
    }
}

class Consumer extends Thread {
    public MultiQueue mq;
    public MsgDispatcher md;

    public Consumer(MultiQueue mq, MsgDispatcher md) {
        this.mq = mq;
        this.md = md;
    }

    @Override
    public void run() {
        try {
            while(true) {
                int max = mq.getMax();
                System.out.println(getName()+" max:"+max);
                md.putMsg(max);
                //sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("Consumer "+getName()+" interrotto");
        }
    }
}

class Client extends Thread {
    private MsgDispatcher md;
    private int id;

    public Client(int id, MsgDispatcher md) {
        this.md = md;
        this.id = id;
        setName("CL"+id);
    }

    @Override
    public void run() {
        try {
            while(true) {
                Object m = md.getMsg(id);
                System.out.println(getName()+" "+m);
            }
        } catch(InterruptedException e) {
            System.out.println("Client "+id+" interrotto");
        }
    }
}