/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread.peterson;

/**
 *
 * @author pierf
 */
public class ThreadPeterson {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        SharedThing st = new SharedThing();
        Thread0 t0 = new Thread0(st);
        Thread1 t1 = new Thread1(st);
        t0.start();
        t1.start();
        t0.join();
        t1.join();
        System.out.println("count "+st.count);
    }
    
}

//provare a togliere indicazione volatile
class SharedThing {
    volatile int turn = 0;
    volatile boolean flag0, flag1;
    int count;
    
    void enter0() {
        flag0=true;
        turn=1;
        while(flag1 && turn==1)
            ;
    }
    
    void exit0() {
        flag0=false;
    }
    
    void enter1() {
        flag1=true;
        turn=0;
        while(flag0 && turn==0)
            ;
    }
    
    void exit1() {
        flag1=false;
    }
}

class Thread1 extends Thread {
    SharedThing st;

    public Thread1(SharedThing st) {
        this.st = st;
    }
    
    public void run() {
        for(int i=0;i<100000;i++) {
            st.enter1();
            st.count++;
            st.exit1();
        }
    }
}

class Thread0 extends Thread {
    SharedThing st;

    public Thread0(SharedThing st) {
        this.st = st;
    }
    
    public void run() {
        for(int i=0;i<100000;i++) {            
            st.enter0();
            st.count++;
            st.exit0();
        }
    }
}