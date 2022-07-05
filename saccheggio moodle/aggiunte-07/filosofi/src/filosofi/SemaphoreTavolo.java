/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filosofi;

import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class SemaphoreTavolo implements Tavolo {
    Semaphore[] bacchette;

    public SemaphoreTavolo(int nFilosofi) {
        bacchette = new Semaphore[nFilosofi];
        for(int i=0;i<bacchette.length; i++) {
            bacchette[i] = new Semaphore(1);
        }
    }

    @Override
    public void getBacchette(int idFilosofo) throws InterruptedException {
        if(idFilosofo==0) {
            bacchette[(idFilosofo + 1)%bacchette.length].acquire();
            bacchette[idFilosofo].acquire();            
        } else {
            bacchette[idFilosofo].acquire();
            bacchette[(idFilosofo + 1)%bacchette.length].acquire();
        }
    }

    @Override
    public void releaseBacchette(int idFilosofo) {
        bacchette[idFilosofo].release();
        bacchette[(idFilosofo + 1)%bacchette.length].release();
    }
    
}
