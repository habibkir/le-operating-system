/*
 * 
 */
package compito7giu2017;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author bellini
 */
public class Compito7Giu2017 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Store urlStore = new Store();
        Store docStore = new Store();
        AtomicCounter downloadCount = new AtomicCounter();
        AtomicCounter parseCount = new AtomicCounter();
        
        int N = 10;
        int M = 5;
        Crawler[] c= new Crawler[N];
        for(int i=0; i<c.length; i++) {
            c[i] = new Crawler(urlStore, docStore, downloadCount);
            c[i].start();
        }
        
        Parser[] p= new Parser[M];        
        for(int i=0; i<p.length; i++) {
            p[i] = new Parser(docStore, urlStore, parseCount);
            p[i].start();
        }
        
        urlStore.add("http://mysite.com");
        for(int i=0; i<20; i++) {
            Thread.sleep(1000);
            System.out.println("Download: "+downloadCount.get()+
                    " Parse: "+parseCount.get());            
        }
        
        for(int i = 0; i<c.length; i++ )
            c[i].interrupt();
        for(int i = 0; i<p.length; i++)
            p[i].interrupt();
    }
    
}

class Store {
    private ArrayList<String> store = new ArrayList<>();
    private Semaphore mutex = new Semaphore(1);
    private Semaphore pieni = new Semaphore(0);
    
    public void add(String v) throws InterruptedException {
        mutex.acquire();
        store.add(v);
        mutex.release();
        pieni.release();
    }
    public void add(String[] v) throws InterruptedException {
        mutex.acquire();
        for(String s: v) {
            store.add(s);
        }
        mutex.release();
        pieni.release(v.length);
    }
    
    public String get() throws InterruptedException {
        pieni.acquire();
        mutex.acquire();
        String v = store.remove(0);
        mutex.release();
        return v;
    }
}

class Crawler extends Thread {
    private Store urlStore;
    private Store docStore;
    private AtomicCounter downloadCounter;

    public Crawler(Store urlStore, Store docStore, AtomicCounter downloadCounter) {
        this.urlStore = urlStore;
        this.docStore = docStore;
        this.downloadCounter = downloadCounter;
    }
    
    public void run() {
        try {
            while(true) {
                String url = urlStore.get();
                //sleep(100);
                downloadCounter.inc();
                docStore.add(url+" html page");
            }
        } catch(InterruptedException e) {
            System.out.println(getName()+" interrotto");
        }
    }
    
}

class Parser extends Thread {
    private Store docStore;
    private Store urlStore;
    private AtomicCounter parseCounter;

    public Parser(Store docStore, Store urlStore, AtomicCounter parseCounter) {
        this.docStore = docStore;
        this.urlStore = urlStore;
        this.parseCounter = parseCounter;
    }
    
    public void run() {
        try {
            while(true) {
                String page = docStore.get();
                //sleep(200);
                int nurl=(int)(Math.random()*11);
                String[] urls = new String[nurl];
                for(int i=0;i<nurl; i++)
                    urls[i] = page+" "+i;
                parseCounter.inc();
                urlStore.add(urls);
            }
        } catch(InterruptedException e) {
            System.out.println(getName()+" interrotto");
        }
    }
}

class AtomicCounter {
    private int c = 0;
    private Semaphore mutex = new Semaphore(1);
            
    public void inc() throws InterruptedException {
        mutex.acquire();
        c++;
        mutex.release();
    }
    
    public int get() {
        return c;
    }
}