/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito22nov2021;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito22Nov2021 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int N = 50; //numero persone
        int K = 10; //numero iniziale infetti
        int V = 10;  //numero vaccinati
        int M = 5;  //posti ospedale
        int T = 2000; //tempo (ms) in ospedale
        
        Ambiente a = new Ambiente();
        Ospedale o = new Ospedale(M);
        Persona[] ps = new Persona[N];
        for(int i=0;i<ps.length; i++) {
            int cv = (i<K ? 50 : 0);
            boolean vc = (i >= ps.length - V);
            ps[i]=new Persona(cv,vc,a,o,new Point(Math.random()*20,Math.random()*20), T);
            a.addPersona(ps[i]); //AGGIUNTO
            ps[i].setName("P"+i);
            ps[i].start();
            
        }
        for(int i=0;i<30; i++) {
            Thread.sleep(1000);
            int nCV10 =0;
            for(Persona p: ps) {
                if(p.caricaVirale>10)
                    nCV10++;
            }
            System.out.println(i+" nCV>10: "+nCV10+" nOsp:"+(M-o.liberi.availablePermits())+" nCodaOsp:"+o.nCoda);
        }
        for(Persona p: ps) {
            p.interrupt();
            p.join();
            System.out.println(p.getName()+" "+p.vaccinato+" ric:"+p.nContagiRicevuti+" fatti:"+p.nContagiFatti);
        }
    }
    
}

class Point {
    double x,y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    double dist(Point p) {
        return Math.sqrt((this.x-p.x)*(this.x-p.x)+(this.y-p.y)*(this.y-p.y));
    }
    
    public void add(double dx, double dy) {
        x+=dx;
        y+=dy;
    }
    
}

class Ambiente {
    ArrayList<Persona> persone = new ArrayList<>();
    Semaphore mutex = new Semaphore(1);
    
    public void addPersona(Persona p) throws InterruptedException {
        mutex.acquire();
        persone.add(p);
        mutex.release();
    }
    
    public void removePersona(Persona p) throws InterruptedException {
        mutex.acquire();
        persone.remove(p);
        mutex.release();
    }
    
    public ArrayList<Persona> findPersone(Point p, double d) throws InterruptedException {
        mutex.acquire();
        ArrayList<Persona> pp = new ArrayList<>();
        for(Persona ps: persone) {
            if(ps.checkPos(p, d)) {
                pp.add(ps);
            }
        }
        mutex.release();
        return pp;
    }
}

class Ospedale {
    Semaphore liberi;
    int nCoda;
    Semaphore mutex =new Semaphore(1);

    public Ospedale(int M) {
        liberi = new Semaphore(M);
    }
    
    public void entra() throws InterruptedException {
        mutex.acquire();
        nCoda++;
        mutex.release();
        liberi.acquire();
        mutex.acquire();
        nCoda--;
        mutex.release();
    }
    
    public void esci() {
        liberi.release();
    }
    
}

class Persona extends Thread {
    int caricaVirale;
    boolean vaccinato;
    Ambiente a;
    Ospedale o;
    Point pos;
    Semaphore mutex = new Semaphore(1);
    int T;
    int nContagiFatti = 0;
    int nContagiRicevuti = 0;

    public Persona(int caricaVirale, boolean vaccinato, Ambiente a, Ospedale o, Point pos, int T) {
        this.caricaVirale = caricaVirale;
        this.vaccinato = vaccinato;
        this.a = a;
        this.o = o;
        this.pos = pos;
        this.T = T;
    }

    public void run() {
        try {
            while(true) {
                // cambiato ordine
                if(caricaVirale > 100) {
                    a.removePersona(this);
                    o.entra();
                    sleep(T);
                    caricaVirale = 0;
                    o.esci();
                    a.addPersona(this);
                } else if(caricaVirale>10) {
                    ArrayList<Persona> vicini = a.findPersone(pos, 2);
                    for(Persona px: vicini) {
                        if(px!=this) {
                            if(px.infetta())
                                nContagiFatti++;
                        }
                    }
                }
                
                mutex.acquire();
                pos.add(Math.random()*2-1, Math.random()*2-1);
                if(caricaVirale>0)
                    caricaVirale--;
                mutex.release();
                sleep(100);
            }
        } catch(InterruptedException e) {
            
        }
    }
    
    public boolean infetta() throws InterruptedException {
        mutex.acquire();
        if(!vaccinato || Math.random()<0.1) {
            caricaVirale +=5;
            nContagiRicevuti++;
            mutex.release();
            return true;
        }
        mutex.release();
        return false;
    }
    
    public boolean checkPos(Point p, double d) throws InterruptedException {
        mutex.acquire();
        boolean r = pos.dist(p)<=d;
        mutex.release();
        return r;
    }
}