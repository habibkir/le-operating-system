/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread.count;

/**
 *
 * @author pierf
 */
public class ThreadCount {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        CountDownThread[] cc=new CountDownThread[3];
        for(int i=0;i<cc.length; i++) {
            cc[i]=new CountDownThread(10,1000*(i+1));
            cc[i].setName("counter-"+i);
            cc[i].start();
        }
        System.out.println("aspetto 10sec...");
        Thread.sleep(10000);
        for(int i=0;i<cc.length; i++) {
            //cc[i].join();
            cc[i].interrupt();
        }
        //c.join();
        //Thread.sleep(3500);
        //c.interrupt();
    }
    
}

class CountDownThread extends Thread {
    private int max;
    private int delay;

    public CountDownThread(int max, int delay) {
        this.max = max;
        this.delay = delay;
    }
    
    public void run() {
        try {
            for(int i=max; i>=0; i--) {
                System.out.println(getName()+": count "+i);
                sleep(delay);
            }
        } catch(InterruptedException e) {
            System.out.println(getName()+": interrotto");
        }
    }
}
