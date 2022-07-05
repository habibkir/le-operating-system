/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread.cycle.ab;

/**
 *
 * @author pierf
 */
public class ThreadCycleAb {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        ActivityA at = new ActivityA();
        ActivityB bt = new ActivityB();

        CycleThreadAB cab = new CycleThreadAB(at, bt);
        cab.start();
        Thread.sleep(30000);
        cab.interrupt();

    }

}

class ActivityA implements Runnable {

    int count = 0;

    public void run() {
        try {
            int wait = (int) (Math.random() * 5 + 1) * 1000;
            System.out.println("start A count=" + count+" wait "+wait+"s");
            count++;
            Thread.sleep(wait);
            System.out.println("end A");
        } catch (InterruptedException e) {
            System.out.println("A interrotto");
        }
    }
}

class ActivityB implements Runnable {

    int count = 0;

    public void run() {
        try {
            int wait = (int) (Math.random() * 5 + 1) * 1000;
            System.out.println("start B count=" + count+" wait "+wait+"s");
            count++;
            Thread.sleep(wait);
            System.out.println("end B");
        } catch (InterruptedException e) {
            System.out.println("B interrotto");
        }
    }
}

class CycleThreadAB extends Thread {

    private ActivityA at;
    private ActivityB bt;

    public CycleThreadAB(ActivityA at, ActivityB bt) {
        this.at = at;
        this.bt = bt;
    }

    public void run() {
        Thread ta = null, tb = null;
        try {
            while (true) {
                ta = new Thread(at);
                tb = new Thread(bt);
                ta.start();
                tb.start();
                ta.join();
                tb.join();
            }
        } catch (InterruptedException e) {
            ta.interrupt();
            tb.interrupt();
            System.out.println("Cycle interrotto");
        }
    }

}
