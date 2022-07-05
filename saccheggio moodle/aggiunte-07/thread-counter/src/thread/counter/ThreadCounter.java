/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread.counter;

/**
 *
 * @author pierf
 */
public class ThreadCounter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        CountThread[] ct = new CountThread[5];
        Semaforo s = new Semaforo(1);
        for (int i = 0; i < ct.length; i++) {
            ct[i] = new CountThread(s);
            ct[i].start();
        }
        for (int i = 0; i < ct.length; i++) {
            ct[i].join();
        }
        System.out.println("count=" + CountThread.count);
    }

}

class CountThread extends Thread {

    Semaforo s;
    static int count = 0;

    public CountThread(Semaforo s) {
        this.s = s;
    }

    public void run() {
        //try {
            for (int i = 0; i < 10000; i++) {
                //Thread.sleep(10);
                //s.acquire();
                count++;
                //s.release();
            }
        /*} catch (InterruptedException e) {

        }*/
    }
}

class Semaforo {
    int value;

    public Semaforo(int value) {
        this.value = value;
    }
    
    public synchronized void acquire() throws InterruptedException {
        while(value <=0)
            wait();
        value--;
    }

    public synchronized void acquire(int n) throws InterruptedException {
        while(value < n)
            wait();
        value-=n;
    }
    
    public synchronized void release(int n) {
        value+=n;
        notifyAll();
    }

    public synchronized void release() {
        release(1);
    }
}
