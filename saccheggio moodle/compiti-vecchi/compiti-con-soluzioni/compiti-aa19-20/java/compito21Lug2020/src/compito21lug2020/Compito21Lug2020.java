/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito21lug2020;

import java.util.ArrayList;
import java.util.List;
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
        int M = 4;
        int K = 3;
        int N = 5;
        GestoreStanze gs = new GestoreStanze(M);
        Persona[] p =new Persona[N];
        for(int i=0;i<N;i++) {
            p[i] = new Persona(gs,K);
            p[i].setName("P"+i);
            p[i].start();
        }
        
        for(int i=0;i<30;i++) {
            Thread.sleep(1000);
            String s="";
            for(int j=0;j<gs.used.length;j++) {
                s+=gs.used[j]+" ";
            }
            System.out.println("stato stanze:"+s);
        }
        
        for(int i=0;i<N;i++) {
            p[i].interrupt();
            p[i].join();
        }
        
        for(int i=0;i<N;i++) {
            System.out.println("P"+i+" nVisisted:"+p[i].nVisited);
        }
        
        for(int i=0;i<gs.used.length; i++) {
            System.out.println("S"+i+" visisted: "+gs.used[i]);
        }
    }
    
}

class GestoreStanze {
    Semaphore[] stanza;
    int[] used;

    public GestoreStanze(int n) {
        stanza = new Semaphore[n];
        for(int i=0;i<n;i++)
            stanza[i] = new Semaphore(1);
        used = new int[n];
    }
    
    public int getStanzaMin(List<Integer> visited) {
        int min = -1;
        ArrayList<Integer> pmins = new ArrayList<>();
        for(int i=0;i<used.length;i++) {
            if(!visited.contains(i)) {
                if(min==-1 || used[i]<min) {
                    min=used[i];
                    pmins.clear();
                    pmins.add(i);
                } else if (used[i]==min) {
                    pmins.add(i);
                }
            }
        }
        int p = (int)(pmins.size()*Math.random());
        System.out.println(Thread.currentThread().getName()+" mins: "+pmins+" scelgo:"+p);
        return pmins.get(p);
    }
    
    public void getStanza(int st) throws InterruptedException {
        stanza[st].acquire();
        used[st]++;
    }
    
    public void releaseStanza(int st) {
        stanza[st].release();
    }
}

class Persona extends Thread {
    GestoreStanze gs;
    int K;
    int nVisited;

    public Persona(GestoreStanze gs, int K) {
        this.gs = gs;
        this.K = K;
    }
    
    public void run() {
        try {
            while(true) {
                ArrayList<Integer> visited = new ArrayList<>();
                for(int i=0;i<K; i++) {
                    int st = gs.getStanzaMin(visited);
                    System.out.println(getName()+" visita stanza "+st+" "+visited.toString());
                    gs.getStanza(st);
                    try {
                        nVisited++;
                        System.out.println(getName()+" dentro "+st);
                        visited.add(st);
                        sleep(100);
                    } finally {
                        System.out.println(getName()+" lascia "+st);
                        gs.releaseStanza(st);
                    }
                }
            } 
        } catch(InterruptedException e) {
            
        }
    }
    
}