/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread.abc;

import static java.lang.Thread.sleep;

/**
 *
 * @author pierf
 */
public class ThreadAbc {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        ActivityAThread at = new ActivityAThread();
        ActivityCThread ct = new ActivityCThread();
        ActivityBThread bt = new ActivityBThread(at,ct);
        
        at.start();
        bt.start();
        ct.start();
        
        bt.join();
        System.out.println("b finito");
    }

}

class ActivityAThread extends Thread {

    @Override
    public void run() {
        try {
            System.out.println("start A");
            sleep(6000);
            System.out.println("end A");
        } catch (InterruptedException e) {
            System.out.println("A interrotto");
        }
    }
}

class ActivityCThread extends Thread {

    @Override
    public void run() {
        try {
            System.out.println("start C");
            sleep(3000);
            System.out.println("end C");
        } catch (InterruptedException e) {
            System.out.println("C interrotto");
        }
    }
}

class ActivityBThread extends Thread {
    private ActivityAThread at;
    private ActivityCThread ct;

    public ActivityBThread(ActivityAThread at, ActivityCThread ct) {
        this.at = at;
        this.ct = ct;
    }
    
    @Override
    public void run() {
        try {
            System.out.println("start B1");
            sleep(1000);
            System.out.println("end B1");
            at.join();
            ct.join();
            System.out.println("start B2");
            sleep(1000);
            System.out.println("end B2");
        } catch (InterruptedException e) {
            System.out.println("A interrotto");
        }
    }
}