/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lettori.scrittori;

/**
 *
 * @author pierf
 */
public class LettoriScrittori {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        //SharedData sd = new SemaphoreSharedData();
        SharedData sd = new SynchronizedSharedData();
        
        Lettore[] ll=new Lettore[4];
        Scrittore[] ss= new Scrittore[4];
        
        for(int i=0; i<ss.length;i++) {
            ss[i] = new Scrittore(sd);
            ss[i].setName("S"+i);
            ss[i].start();
        }
        for(int i=0; i<ll.length; i++) {
            ll[i] = new Lettore(sd);
            ll[i].setName("L"+i);
            ll[i].start();
        }
        
        Thread.sleep(10000);
        
        for(Lettore l:ll)
            l.interrupt();
        for(Scrittore s:ss)
            s.interrupt();
        for(Lettore l:ll) {
            l.join();
            System.out.println(l.getName()+" "+l.nLetture);
        }
        for(Scrittore s:ss) {
            s.join();
            System.out.println(s.getName()+" "+s.nScritture);
        }
    }
    
}

class Scrittore extends Thread {
    SharedData sd;
    int nScritture = 0;

    public Scrittore(SharedData sd) {
        this.sd = sd;
    }
    
    public void run() {
        try {
            while(!interrupted()) {
                sleep(100);
                sd.startWriteAccess();
                nScritture++;
                System.out.println(getName()+" scrive");
                sleep(100);
                sd.endWriteAccess();
            }
        } catch(InterruptedException e) {
            System.out.println(getName()+" interrotto");
        }
    }
}

class Lettore extends Thread {
    SharedData sd;
    int nLetture =0;

    public Lettore(SharedData sd) {
        this.sd = sd;
    }
    
    
    public void run() {
        try {
            while(!interrupted()) {
                sleep(100);
                sd.startReadAccess();
                nLetture++;
                System.out.println(getName()+" legge");
                sleep(100);
                sd.endReadAccess();
            }
        }catch(InterruptedException e) {
            System.out.println(getName()+" interrotto");
        }
    }
}