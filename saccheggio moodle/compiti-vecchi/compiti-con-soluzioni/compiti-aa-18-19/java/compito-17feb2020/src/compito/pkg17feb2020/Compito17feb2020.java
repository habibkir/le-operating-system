/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito.pkg17feb2020;

/**
 *
 * @author pierf
 */
public class Compito17feb2020 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int N = 10;
        Tavolo t = new Tavolo(N);
        Banco b = new Banco(t);
        b.start();
        Giocatore[] gg = new Giocatore[N];
        for(int i=0; i<gg.length;i++) {
            gg[i] = new Giocatore(t, i);
            gg[i].setName("G"+i);
            gg[i].start();
        }
        Thread.sleep(10000);
        b.ferma = true;
        for(int i=0;i<gg.length;i++) {
            gg[i].join();
            System.out.println("G"+i+": "+gg[i].nVinte+" "+gg[i].nPari);
        }
    }
    
}

class Tavolo {
    private int banco = 0;
    private int[] giocato;
    private int nGiocato = 0;
    private int nLettoRisultato = 0;
    private boolean ferma = false;

    public Tavolo(int nG) {
        giocato = new int[nG];
    }
    
    public synchronized void bancoGioca(int v, boolean f) {
        banco = v;
        ferma = f;
        //System.out.println("banco gioca "+v);
        notifyAll();
    }
    
    public synchronized boolean waitBanco() throws InterruptedException {
        while(banco==0 || nLettoRisultato>0)
            wait();
        //System.out.println(Thread.currentThread().getName()+" banco giocato");
        return ferma;
    }
    
    public synchronized void gioca(int v, int ng) {
        giocato[ng]=v;
        nGiocato++;
        //System.out.println("G"+ng+" gioca "+v);
        notifyAll();
    }
    
    public synchronized int waitGiocatoTutti(int ng) throws InterruptedException {
        while(nGiocato<giocato.length)
            wait();
        //System.out.println("giocato tutti "+ng);
        int dmin=1000;
        int nmin = 0;
        for(int i=0;i<giocato.length;i++) {
            int di=Math.abs(banco-giocato[i]);
            if(di<dmin) {
                dmin=di;
                nmin=1;
            } else if(di==dmin) {
                nmin++;
            }
        }
        nLettoRisultato++;
        notifyAll();
        int dg = Math.abs(banco-giocato[ng]);
        if(dg==dmin) {
            if(nmin==1) {
                //System.out.println("VINTO");
                return 1; //vinto
            } else {
                //System.out.println("PARI");
                return 2; //pari
            }
        }
        //System.out.println("PERSO");
        return 0; //perso
    }
    
    public synchronized void waitLettoTutti() throws InterruptedException {
        while(nLettoRisultato<giocato.length)
            wait();
        //System.out.println("LETTO TUTTI");
        banco = 0;
        giocato = new int[giocato.length];
        nGiocato = 0;
        nLettoRisultato = 0;
    }
}

class Banco extends Thread {
    private Tavolo t;
    boolean ferma = false;

    public Banco(Tavolo t) {
        this.t = t;
    }
    
    public void run() {
        try {
            while(true) {
                boolean f = ferma;
                t.bancoGioca(1+(int)(Math.random()*99),f);
                if(f)
                    break;
                t.waitLettoTutti();
            }
        } catch(InterruptedException e) {
            
        }
        System.out.println("banco fermo");
    }
}

class Giocatore extends Thread {
    private Tavolo t;
    private int ng;
    public int nVinte,nPari;

    public Giocatore(Tavolo t, int ng) {
        this.t = t;
        this.ng = ng;
    }
    
    public void run() {
        try {
            boolean ferma;
            do {
                ferma = t.waitBanco();
                t.gioca(1+(int)(Math.random()*99), ng);
                int r = t.waitGiocatoTutti(ng);
                if(r==1)
                    nVinte++;
                else if(r==2)
                    nPari++;
            } while(!ferma);
        } catch(InterruptedException e) {
            
        }
    }
}