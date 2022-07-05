/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito06lug2021;

import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito06Lug2021 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        CentroVaccinale cv = new CentroVaccinale(10, 1000);
        AddettiEntrata ad = new AddettiEntrata(3, 500);
        AddettiVaccino av = new AddettiVaccino(5, 1000);
        Persona[] persone = new Persona[200];
        for(int i=0;i<persone.length; i++) {
            persone[i] = new Persona(cv,ad,av);
            persone[i].start();
        }
        
        while(cv.nPersoneACasa<persone.length) {
            Thread.sleep(1000);
            System.out.println("cv:"+cv.getPersoneInside()+" nv:"+av.nVaccini+" ncasa:"+cv.nPersoneACasa);

        }
        int sum=0, max=0;
        for(Persona p:persone) {
            sum += p.elapsedTime;
            if(max<p.elapsedTime) {
                max = p.elapsedTime;
            }
        }
        System.out.println("avg:"+((double)sum/persone.length)+" max:"+max);
    }
    
}

class CentroVaccinale {
    Semaphore persone;
    int nPersoneMax;
    int T3;
    int nPersoneACasa = 0;
    Semaphore mutex = new Semaphore(1);
    
    public CentroVaccinale(int nPersoneMax, int T3) {
        this.nPersoneMax = nPersoneMax;
        persone = new Semaphore(nPersoneMax);
        this.T3 = T3;
    }
    
    public void enter() throws InterruptedException {
        persone.acquire();
    }
    
    public void exit() throws InterruptedException {
        persone.release();
        mutex.acquire();
        nPersoneACasa++;
        mutex.release();
    }
    
    public int getPersoneInside() {
        return nPersoneMax-persone.availablePermits();
    }
}

class AddettiEntrata {
    Semaphore addetti;
    int T1;

    public AddettiEntrata(int A, int T1) {
        addetti = new Semaphore(A);
        this.T1 = T1;
    }
    
    public boolean check() throws InterruptedException {
        addetti.acquire();
        Thread.sleep(T1);
        boolean result = true;
        if(Math.random()<0.05)
            result = false;
        addetti.release();
        return result;
    }
}

class AddettiVaccino {
    Semaphore vaccinatori;
    int T2;
    int nVaccini;
    Semaphore mutex = new Semaphore(1);

    public AddettiVaccino(int V, int T2) {
        this.vaccinatori = new Semaphore(V);
        this.T2 = T2;
    }
    
    public void vaccina() throws InterruptedException {
        vaccinatori.acquire();
        Thread.sleep(T2);
        mutex.acquire();
        nVaccini++;
        mutex.release();
        vaccinatori.release();
    }
    
}

class Persona extends Thread {
    CentroVaccinale cv;
    AddettiEntrata ae;
    AddettiVaccino av;
    int elapsedTime;

    public Persona(CentroVaccinale cv, AddettiEntrata ae, AddettiVaccino av) {
        this.cv = cv;
        this.ae = ae;
        this.av = av;
    }
    
    public void run() {
        try {
            long ts = System.currentTimeMillis();
            int texpected = ae.T1;
            cv.enter();
            if(ae.check()) {
                av.vaccina();
                sleep(cv.T3);
                texpected+=av.T2+cv.T3;
            }
            cv.exit();
            long te = System.currentTimeMillis();
            elapsedTime = (int)(te-ts)-(texpected);
        } catch(InterruptedException e) {
            
        }
    }
}