/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filosofi;

/**
 *
 * @author pierf
 */
public class Filosofi {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Tavolo t = new SemaphoreTavolo(5);
        Tavolo t = new SynchronizedTavolo(5);
        Filosofo[] ff = new Filosofo[5];
        for(int i=0; i<ff.length; i++) {
            ff[i]=new Filosofo(t, i);
            ff[i].start();
        }
    }
    
}

class Filosofo extends Thread {
    Tavolo tavolo;
    int idFilosofo;

    public Filosofo(Tavolo tavolo, int idFilosofo) {
        this.tavolo = tavolo;
        this.idFilosofo = idFilosofo;
    }
    
    public void run() {
        try {
            while(true) {
                System.out.println("F"+idFilosofo+" PENSA");
                //sleep(100);
                System.out.println("F"+idFilosofo+" HA FAME");
                
                tavolo.getBacchette(idFilosofo);
                System.out.println("F"+idFilosofo+" MANGIA");
                //sleep((int)(Math.random()*1000));
                tavolo.releaseBacchette(idFilosofo);
            }
        } catch(InterruptedException e) {
            System.out.println("F"+idFilosofo+" INTERROTTO");
        }
    }
}