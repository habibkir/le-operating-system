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
public class SynchronizedTavolo implements Tavolo {
    int[] state;

    public SynchronizedTavolo(int n) {
        state = new int[n]; //0=non mangia 1=mangia
    }
    
    @Override
    public synchronized void getBacchette(int idFilosofo) throws InterruptedException {
        while(state[(idFilosofo+1)%state.length]==1 || 
                state[(idFilosofo + state.length-1)%state.length]==1) {
            System.out.println("F"+idFilosofo+" ASPETTA");
            wait();
        }
        state[idFilosofo]=1;
    }

    @Override
    public synchronized void releaseBacchette(int idFilosofo) {
        state[idFilosofo] = 0;
        notifyAll();
    }
    
}
