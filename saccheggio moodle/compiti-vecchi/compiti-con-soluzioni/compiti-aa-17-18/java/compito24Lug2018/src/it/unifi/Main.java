package it.unifi;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws InterruptedException {
	    int N = 10;
	    int K = 10;
	    int M = 100;

	    Stanze s = new Stanze(N, K);
	    Persona[] persone = new Persona[M];
	    for(int i=0; i<persone.length; i++) {
	        persone[i] = new Persona(i, s);
	        persone[i].start();
        }

	    while(s.print()) {
	        Thread.sleep(100);
        }

	    for(int i=0; i<persone.length; i++) {
	        persone[i].join();
	        System.out.println(i+" impiegato: "+persone[i].tempo+"ms");
        }
    }
}

class Persona extends Thread {
    int id;
    Stanze stanze;
    long tempo;

    public Persona(int id, Stanze s) {
        this.id = id;
        this.stanze = s;
    }

    public boolean incompatibile(Persona p) {
        return Math.abs(p.id-id)<3;
    }

    @Override
    public void run() {
        try {
            stanze.fromTo(this, -1, 0);
            long start = System.currentTimeMillis();
            for(int i=0; i<stanze.getNStanze()-1; i++) {
                System.out.println();
                sleep(1000);
                stanze.fromTo(this,i, i+1);
            }
            sleep(1000);
            stanze.fromTo(this, stanze.getNStanze()-1, -1);
            tempo = System.currentTimeMillis()-start;
        } catch(InterruptedException e) {

        }
    }
}

class Stanze {
    private ArrayList<Persona>[] stanze;
    private int capacita;

    public Stanze(int N, int K) {
        stanze = new ArrayList[N];
        capacita = K;
        for(int i=0; i<stanze.length; i++)
            stanze[i] = new ArrayList<>();
    }

    public synchronized void fromTo(Persona p, int from, int to) throws InterruptedException{
        if(to>=0) {
            while(stanze[to].size()==capacita || checkIncompatibility(p, to))
                wait();
        }
        if(from>=0) {
            stanze[from].remove(p);
        }
        if(to>=0) {
            stanze[to].add(p);
        }
        notifyAll();
    }

    private boolean checkIncompatibility(Persona p, int stanza) {
        for(Persona x: stanze[stanza]) {
            if(x.incompatibile(p))
                return true;
        }
        return false;
    }

    public synchronized boolean print() {
        boolean vuote = false;
        for(int i=0; i<stanze.length; i++) {
            System.out.print(stanze[i].size()+" ");
            vuote = vuote || stanze[i].size()>0;
        }
        System.out.println();
        return vuote;
    }

    public int getNStanze() {
        return stanze.length;
    }
}






