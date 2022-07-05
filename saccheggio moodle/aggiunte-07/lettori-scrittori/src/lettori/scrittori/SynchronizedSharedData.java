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
public class SynchronizedSharedData implements SharedData {
    int nScrittori = 0;
    int nLettori = 0;
    @Override
    public synchronized void startReadAccess() throws InterruptedException {
        while(nScrittori>0)
            wait();
        nLettori++;
    }

    @Override
    public synchronized void endReadAccess() throws InterruptedException {
        nLettori--;
        notifyAll();
    }

    @Override
    public synchronized void startWriteAccess() throws InterruptedException {
        while(nScrittori>0 || nLettori>0)
            wait();
        nScrittori++;
    }

    @Override
    public synchronized void endWriteAccess() throws InterruptedException {
        nScrittori--;
        notifyAll();
    }    
}
