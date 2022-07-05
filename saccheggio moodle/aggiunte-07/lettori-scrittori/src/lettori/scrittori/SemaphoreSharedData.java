/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lettori.scrittori;

import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class SemaphoreSharedData implements SharedData {
    Semaphore mutex = new Semaphore(1);
    Semaphore scrittura = new Semaphore(1,true);
    int nLettori = 0;

    @Override
    public void startReadAccess() throws InterruptedException {
        mutex.acquire();
        nLettori++;
        if(nLettori==1)
            scrittura.acquire();
        mutex.release();
    }

    @Override
    public void endReadAccess() throws InterruptedException {
        mutex.acquire();
        nLettori--;
        if(nLettori==0)
            scrittura.release();
        mutex.release();
    }

    @Override
    public void startWriteAccess() throws InterruptedException {
        scrittura.acquire();
    }

    @Override
    public void endWriteAccess() throws InterruptedException {
        scrittura.release();
    }
    
}
