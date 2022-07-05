package it.unifi;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws InterruptedException{
        final int K = 1000;
        final int N = 5;
        final int M = 5;

        Counter c = new Counter(K);
        CodaMessaggi cm = new CodaMessaggi(100, N);
        Producer[] p = new Producer[N];
        for(int i=0; i<p.length; i++) {
            p[i]=new Producer(i, cm, c, M);
            p[i].start();
        }

        Consumer[] cc = new Consumer[M];
        for(int i=0; i<cc.length; i++) {
            cc[i] = new Consumer(i, cm);
            cc[i].start();
        }

        for(int i=0; i<p.length; i++) {
            p[i].join();
        }

        int nMsgC = 0;
        int nMsgP = 0;
        for(int i=0; i<cc.length; i++) {
            cc[i].join();
            System.out.println("C"+i+": "+cc[i].nMsg);
            nMsgC+=cc[i].nMsg;
        }
        for(int i=0; i<p.length; i++) {
            System.out.println("P"+i+": "+p[i].nMsg);
            nMsgP+=p[i].nMsg;
        }
        System.out.println("Tot Prodotti: "+nMsgP+" Tot Consumati: "+nMsgC);
    }
}

class Counter {
    private int c;
    private int max;

    public Counter(int max) {
        this.max = max;
        c = 1;
    }

    public synchronized int getValue() {
        int r = c;
        if(c<=max) {
            c++;
        } else {
            r = 0;
        }
        return r;
    }
}

class Messaggio {
    Object value;
    int idProducer;
    int idConsumer;

    Messaggio(Object v, int idP, int idC) {
        value = v;
        idProducer = idP;
        idConsumer = idC;
    }
}

class CodaMessaggi {
    private ArrayList<Messaggio> coda = new ArrayList<>();
    private int max;
    private boolean stop = false;
    private int nStop = 0;
    private int nProducers;

    public CodaMessaggi(int max, int nProducers) {
        this.max = max;
        this.nProducers = nProducers;
    }

    public synchronized void putMsg(Messaggio m) throws InterruptedException {
        while(coda.size()==max)
            wait();
        coda.add(m);
        notifyAll();
    }

    public synchronized Messaggio getMsg(int idConsumer) throws InterruptedException{
        while((coda.size()==0 && nStop!=nProducers) ||
                (coda.size()>0 && coda.get(0).idConsumer!=idConsumer))
            wait();
        if(coda.size() == 0)
            return null;
        Messaggio r = coda.remove(0);
        notifyAll();
        return r;
    }

    public synchronized void setStop() {
        //incrementa il numero di Producer che hanno terminato
        nStop++;
        notifyAll();
    }
}

class Producer extends Thread {
    private Counter c;
    private CodaMessaggi cm;
    private int idP;
    private int M;
    public int nMsg;

    public Producer(int idP,CodaMessaggi cm, Counter c, int M) {
        this.idP = idP;
        this.cm = cm;
        this.c = c;
        this.M = M;
    }

    @Override
    public void run() {
        try {
            int v;
            while((v=c.getValue())!=0) {
                cm.putMsg(new Messaggio(v,idP, (v+idP)%M));
                nMsg++;
                //sleep(1000);
            }
            cm.setStop();
        } catch(InterruptedException e) {

        }
    }
}

class Consumer extends Thread {
    private CodaMessaggi cm;
    private int idC;
    public int nMsg = 0;

    Consumer(int idC, CodaMessaggi cm) {
        this.idC = idC;
        this.cm = cm;
    }

    @Override
    public void run() {
        try {
            Messaggio m;
            do {
                m = cm.getMsg(idC);
                if(m!=null) {
                    System.out.println(idC + ": " + m.value + " " + m.idProducer + " " + m.idConsumer);
                    nMsg++;
                    sleep(100);
                }
            } while(m!=null);
        } catch (InterruptedException e) {

        }
    }

}
















