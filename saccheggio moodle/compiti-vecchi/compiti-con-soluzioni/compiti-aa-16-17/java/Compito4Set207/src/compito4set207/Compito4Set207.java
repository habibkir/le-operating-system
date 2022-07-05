/*
 * 
 */
package compito4set207;

import java.util.ArrayList;

/**
 *
 * @author bellini
 */
public class Compito4Set207 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int N=3;
        int M=100;
        
        AreaDiControllo area = new AreaDiControllo(N);
        CodaPasseggeri coda = new CodaPasseggeri(0);
        AddettoControllo addetto=new AddettoControllo(area, coda);
        addetto.start();
        
        Passeggero[] p= new Passeggero[M];
        for(int i=0; i<p.length; i++) {
            p[i] = new Passeggero(i);
            p[i].tempoArrivo=System.currentTimeMillis();
            coda.put(p[i]);
            Thread.sleep(50);
        }
        
        area.waitControllati(M);
        long s1=0,s2=0,s3=0;
        for(int i=0; i<p.length; i++) {
            long t1 = p[i].tempoDirezionamento-p[i].tempoArrivo;
            long t2 = p[i].tempoControllo-p[i].tempoDirezionamento;
            long t3 = p[i].tempoControllo-p[i].tempoArrivo;
            System.out.println("P"+i+" "+t1+" "+t2+" "+t3);
            s1+=t1;
            s2+=t2;
            s3+=t3;
        }
        System.out.println("medie: "+(s1/M)+" "+(s2/M)+" "+(s3/M));
        area.interrupt();
        addetto.interrupt();
    }
    
}

class Passeggero {
    int id;
    long tempoArrivo;
    long tempoDirezionamento;
    long tempoControllo;

    public Passeggero(int id) {
        this.id = id;
    }
    
    
}

class CodaPasseggeri {
    private ArrayList<Passeggero> coda;
    private int max; //max==0 coda illimitata
    
    public CodaPasseggeri(int max) {
        this.max = max;
        coda=new ArrayList<>();        
    }
    
    public synchronized void put(Passeggero p) throws InterruptedException {
        if(max>0) {
            while(coda.size()==max) {
                wait();
            }
        }
        coda.add(p);
        notifyAll();
    }
    
    public synchronized Passeggero get() throws InterruptedException {
        while(coda.isEmpty()) {
            wait();
        }
        Passeggero r = coda.remove(0);
        return r;
    }
    
    public synchronized int size() {
        return coda.size();
    }
}

class StazioneControllo extends Thread {
    CodaPasseggeri coda = new CodaPasseggeri(10);
    AreaDiControllo area;

    public StazioneControllo(AreaDiControllo area) {
        this.area = area;
    }
        
    public void run() {
        try {
            while(true) {
                Passeggero p = coda.get();
                p.tempoControllo = System.currentTimeMillis();
                sleep(1000);
                area.passeggeroControllato();
                System.out.println("controllato passeggero "+p.id);
            }
        } catch(InterruptedException e) {
            
        }
    }
}

class AreaDiControllo {
    StazioneControllo[] stazioni;
    int controllati = 0;
    
    public AreaDiControllo(int N) {
        stazioni = new StazioneControllo[N];
        for(int i=0; i<N; i++) {
            stazioni[i]=new StazioneControllo(this);
            stazioni[i].start();
        }
    }
    
    public synchronized void ingressoPasseggero(Passeggero p) throws InterruptedException{
        int c=0;
        while((c = minCoda())==-1) {
            System.out.println("tutte le code piene!");
            wait();
        }
        stazioni[c].coda.put(p);
    }
    
    public synchronized void passeggeroControllato() {
        controllati++;
        notifyAll();
    }
    
    public synchronized void waitControllati(int M) throws InterruptedException {
        while(controllati<M)
            wait();
    }
    
    private int minCoda() {
        int pmin=-1;
        int min=10;
        for(int i=0; i<stazioni.length; i++) {
            if(stazioni[i].coda.size()<min) {
                min=stazioni[i].coda.size();
                pmin=i;
            }
        }
        return pmin;
    }
    
    public void interrupt() {
        for(StazioneControllo c: stazioni)
            c.interrupt();
    }
}

class AddettoControllo extends Thread {
    AreaDiControllo area;
    CodaPasseggeri coda;

    public AddettoControllo(AreaDiControllo area, CodaPasseggeri coda) {
        this.area = area;
        this.coda = coda;
    }
    
    public void run() {
        try {
            while(true) {
                Passeggero p = coda.get();
                p.tempoDirezionamento = System.currentTimeMillis();
                System.out.println("direziona passeggero "+p.id);
                area.ingressoPasseggero(p);
            }
        } catch(InterruptedException e) {
            
        }
    }
    
}