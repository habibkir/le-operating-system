/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito.pkg16lug2019;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author pierf
 */
public class Compito16Lug2019 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        /*TokensManager tm = new TokensManager(new int[]{0,0,3,0});
        ThreadT[] t = new ThreadT[4];
        t[0]=new ThreadT(tm, new int[]{0,1}, new int[]{2}, 0, 200);
        t[1]=new ThreadT(tm, new int[]{0,1}, new int[]{2}, 100, 200);
        t[2]=new ThreadT(tm, new int[]{2}, new int[]{0,3}, 0, 100);
        t[3]=new ThreadT(tm, new int[]{3}, new int[]{1}, 200, 300);
        
        for(int i=0;i<t.length;i++){
            t[i].setName("T"+(i+1));
            t[i].start();
        }
        Thread.sleep(30000);
        for(int i=0;i<t.length;i++) {
            t[i].interrupt();
        }*/
        load("def.txt");
    }
    
    public static void load(String filename) throws FileNotFoundException, InterruptedException {
        FileInputStream fin = new FileInputStream(filename);
        Scanner s = new Scanner(fin);
        int nCnt = s.nextInt();
        int[] cnt = new int[nCnt];
        for(int i=0;i<nCnt;i++) {
            cnt[i] = s.nextInt();
        }
        TokensManager tm = new TokensManager(cnt);
        int nThr = s.nextInt();
        ThreadT[] t = new ThreadT[nThr];
        for(int i=0;i<nThr;i++) {
            String name = s.next();
            System.out.print(name+" [");
            int nCntIn = s.nextInt();
            int[] cntIn = new int[nCntIn];
            for(int j=0; j<nCntIn; j++) {
                cntIn[j] = s.nextInt()-1;
                System.out.print(cntIn[j]+" ");
            }
            System.out.print("] --> [");
            int nCntOut = s.nextInt();
            int[] cntOut = new int[nCntOut];
            for(int j=0;j<nCntOut;j++) {
                cntOut[j]=s.nextInt()-1;
                System.out.print(cntOut[j]+" ");
            }
            int dmin = s.nextInt();
            int dmax = s.nextInt();
            System.out.println("] delay in ["+dmin+","+dmax+")");
            t[i] = new ThreadT(tm, cntIn, cntOut, dmin, dmax);
            t[i].setName(name);
            t[i].start();
        }
        
        Thread.sleep(30000);
        for(int i=0;i<t.length;i++) {
            t[i].interrupt();
        }
    }
        
}

class TokensManager {
    private int[] tokens;

    public TokensManager(int[] tokens) {
        this.tokens = tokens;
    }
    
    public synchronized void getTokens(int[] p) throws InterruptedException{
        while(!checkTokens(p))
            wait();
        for(int i=0;i<p.length;i++) {
            tokens[p[i]]--;
        }
        print();
    }
    
    private boolean checkTokens(int[] p) {
        for(int i=0;i<p.length;i++) {
            if(tokens[p[i]]==0)
                return false;
        }
        return true;
    }
    
    public synchronized void setTokens(int[] p) {
        for(int i=0;i<p.length;i++)
            tokens[p[i]]++;
        print();
        notifyAll();
    }
    
    public void print() {
        System.out.print(Thread.currentThread().getName()+" ");
        for(int i=0;i<tokens.length;i++)
            System.out.print(tokens[i]+" ");
        System.out.println();
    }
    
}

class ThreadT extends Thread {
    private TokensManager tm;
    private int[] tkIn,tkOut;
    private int dmin,dmax;

    public ThreadT(TokensManager tm, int[] tkIn, int[] tkOut, int dmin, int dmax) {
        this.tm = tm;
        this.tkIn = tkIn;
        this.tkOut = tkOut;
        this.dmin = dmin;
        this.dmax = dmax;
    }
    
    public void run() {
        try {
            while(true) {
                tm.getTokens(tkIn);
                sleep(dmin+(int)(Math.random()*(dmax-dmin)));
                tm.setTokens(tkOut);
            }
        } catch(InterruptedException e) {
            
        }
    }
}