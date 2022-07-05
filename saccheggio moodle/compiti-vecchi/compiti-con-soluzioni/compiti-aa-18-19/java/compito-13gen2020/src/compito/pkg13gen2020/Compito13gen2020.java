/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito.pkg13gen2020;

import java.util.ArrayList;

/**
 *
 * @author pierf
 */
public class Compito13gen2020 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        MergeQueue mq = new MergeQueue();
        int N=1000;
        int M = 4;
        for(int i=0;i<N;i++) {
            int v = (int)(Math.random()*101);
            System.out.print(v+" ");
            mq.put(new int[]{v});
        }
        System.out.println();
        
        MergeThread[] mt = new MergeThread[M];
        for(int i=0;i<M;i++) {
            mt[i] = new MergeThread(mq);
            mt[i].start();
        }
        
        int[] result = mq.getResult(N);
        for(int i=0;i<N;i++)
            System.out.print(result[i]+" ");
        System.out.println();
        
        for(int i=0;i<mt.length; i++)
            mt[i].interrupt();
    }
    
}

class MergeQueue {
    private ArrayList<int[]> data = new ArrayList<>();
    
    public synchronized void put(int[] v) {
        data.add(v);
        notifyAll();
    }
    
    public synchronized int[] get() throws InterruptedException {
        while(data.size()<1)
            wait();
        return data.remove(0);
    }
    
    public synchronized int[][] get2() throws InterruptedException {
        while(data.size()<2)
            wait();
        int[][] r = new int[2][];
        r[0] = data.remove(0);
        r[1] = data.remove(0);
        return r;
    }
    
    public synchronized int[] getResult(int n) throws InterruptedException {
        while(data.size()!=1 || data.get(0).length!=n)
            wait();
        return data.get(0);
    }
}

class MergeThread extends Thread {
    private MergeQueue mq;

    public MergeThread(MergeQueue mq) {
        this.mq = mq;
    }
    
    public void run() {
        try {
            while(true) {
                int[][] v = mq.get2();
                /*int[][] v = new int[2][];
                v[0]=mq.get();
                v[1]=mq.get();*/
                int[] r = merge(v[0],v[1]);
                System.out.println(getName()+" merge("+v[0].length+","+v[1].length+")->"+r.length);
                mq.put(r);
            }
        } catch(InterruptedException e) {
            
        }
    }
    
    public int[] merge(int[] v1, int[] v2) {
        int[] r = new int[v1.length+v2.length];
        int p=0, p1=0, p2=0;
        while(p1<v1.length && p2<v2.length) {
            if(v1[p1]<=v2[p2])
                r[p++] = v1[p1++];
            else
                r[p++] = v2[p2++];
        }
        while(p1<v1.length)
            r[p++]=v1[p1++];
        while(p2<v2.length)
            r[p++]=v2[p2++];
        return r;
    }
}