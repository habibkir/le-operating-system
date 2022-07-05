/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito21lug2020;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class Compito21Lug2020 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int M = 5;
        GestoreStanze gs = new GestoreStanze(M);
        Persona[] p=new Persona[20];
        for(int i=0;i<p.length; i++) {
            p[i] = new Persona(gs,5);
            p[i].setName("P"+i);
            p[i].start();
        }
        Thread.sleep(10000);
        for(Persona px: p) {
            px.interrupt();
        }
        int s1=0;
        for(Persona px: p) {
            px.join();
            System.out.println(px.getName()+" visited:"+px.nVisited);
            s1+=px.nVisited;
        }
        int s2 = 0;
        for(int i=0;i<M; i++) {
            System.out.println("S"+i+" visited:"+gs.used[i]+" free:"+gs.free[i]);
            s2+=gs.used[i];
        }
        System.out.println(s1+" "+s2);
    }

}

class GestoreStanze {

    Semaphore[] stanze;
    int[] used;
    int[] free;

    public GestoreStanze(int M) {
        stanze = new Semaphore[M];
        for (int i = 0; i < M; i++) {
            stanze[i] = new Semaphore(1);
        }
        used = new int[M];
        free = new int[M];
    }

    public int getMinStanza(ArrayList<Integer> visited) {
        ArrayList<Integer> minVisited = new ArrayList<>();
        int min = -1;
        for (int i = 0; i < used.length; i++) {
            if (!visited.contains(i)) {
                int u = used[i];
                if (min == -1 || (min > u)) {
                    min = u;
                    minVisited = new ArrayList();
                    minVisited.add(i);
                } else if (min == u) {
                    minVisited.add(i);
                }
            }
        }
        int r = minVisited.get((int) (Math.random() * minVisited.size()));
        return r;
    }

    public void enterStanza(int ns) throws InterruptedException {
        if(stanze[ns].tryAcquire())
            free[ns]++;
        else
            stanze[ns].acquire();
        used[ns]++;
    }

    public void exitStanza(int ns) {
        stanze[ns].release();
    }

}

class Persona extends Thread {

    GestoreStanze gs;
    int K;
    int nVisited = 0;

    public Persona(GestoreStanze gs, int K) {
        this.gs = gs;
        this.K = K;
    }

    public void run() {
        try {
            while (true) {
                ArrayList<Integer> visited = new ArrayList<>();
                long ts = System.currentTimeMillis();
                for (int i = 0; i < K; i++) {
                    int ns = gs.getMinStanza(visited);
                    gs.enterStanza(ns);
                    try {
                        System.out.println(getName() + " in stanza " + ns);
                        sleep(100);
                    } finally {
                        gs.exitStanza(ns);
                        nVisited++;
                    }
                    visited.add(ns);
                }
                long te = System.currentTimeMillis();
                System.out.println(getName()+"--------------> "+(te-ts-K*100)+" ms");
            }
        } catch (InterruptedException e) {
        }
    }

}
