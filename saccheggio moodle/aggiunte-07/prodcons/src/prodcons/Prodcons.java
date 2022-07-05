/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prodcons;

/**
 *
 * @author pierf
 */
public class Prodcons {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Produttore[] pp=new Produttore[3];
        Consumatore[] cc= new Consumatore[2];
        //SharedQueue queue = new SemaphoreSharedQueue(10);
        SharedQueue queue = new SynchronizedSharedQueue(10);
        
        for(int i=0;i<pp.length; i++) {
            pp[i] = new Produttore(queue,100);
            pp[i].setName("P"+i);
            pp[i].start();
        }
        
        for(int i=0; i<cc.length; i++) {
            cc[i]= new Consumatore(queue,100);
            cc[i].setName("C"+i);
            cc[i].start();
        }
        
        for(int i=0;i<10; i++) {
            System.out.println("size: "+queue.size());
            Thread.sleep(1000);            
        }
        for(Produttore p:pp) {
            p.interrupt();
        }
        for(Consumatore c:cc) {
            c.interrupt();
        }
        for(Produttore p:pp) {
            p.join();
            System.out.println(p.getName()+" "+p.nProd);
        }
        for(Consumatore c:cc) {
            c.join();
            System.out.println(c.getName()+" "+c.nCons);
        }
        
        System.out.println(queue.size());
        
    }
    
}

class Produttore extends Thread {
    SharedQueue sq;
    int attesa;
    int nProd = 0;

    public Produttore(SharedQueue sq, int attesa) {
        this.sq = sq;
        this.attesa = attesa;
    }
    
    public void run() {
        try {
            while(true) {
                sleep(attesa);
                sq.add(getName()+" "+(nProd++));
            }
        } catch(InterruptedException e) {
            System.out.println(getName()+" interrotto");
        }
    }
    
}

class Consumatore extends Thread {
    SharedQueue sq;
    int attesa;
    int nCons = 0;

    public Consumatore(SharedQueue sq, int attesa) {
        this.sq = sq;
        this.attesa = attesa;
    }
    
    public void run() {
        try {
            while(true) {
                Object v=sq.get();
                //System.out.println(getName()+" preso "+v);
                nCons++;
                sleep(attesa);
            }
        } catch(InterruptedException e) {
            System.out.println(getName()+" interrotto");
        }
    }
}
