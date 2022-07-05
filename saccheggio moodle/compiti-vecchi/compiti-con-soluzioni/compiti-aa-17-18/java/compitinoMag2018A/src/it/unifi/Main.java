package it.unifi;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        int N;
        int M;
        Scanner s = new Scanner(System.in);
        do {
           System.out.println("N=");
           N = s.nextInt();
        } while(N<=0 || N%2!=0);
        do {
            System.out.println("M=");
            M = s.nextInt();
        } while(M<=0);
        ArrayGeneratorThread[] agt=new ArrayGeneratorThread[N];
        for(int i=0; i<agt.length; i++){
            agt[i]=new ArrayGeneratorThread(M);
            agt[i].start();
        }
        PScalarThread[] pst = new PScalarThread[N/2];
        for(int i=0; i<pst.length; i++) {
            pst[i] = new PScalarThread(agt[i*2],agt[i*2+1]);
            pst[i].start();
        }
        CollectorThread ct = new CollectorThread(pst);
        ct.start();
        ct.join();
    }
}

class ArrayGeneratorThread extends Thread {
    private float[] values;

    public ArrayGeneratorThread(int M) {
        values = new float[M];
    }

    public void run() {
        for(int i=0; i<values.length; i++) {
            values[i] = (float)(Math.random()*10-5);
        }
    }

    public float[] getValues() {
        return values;
    }
}

class PScalarThread extends Thread {
    private ArrayGeneratorThread g1;
    private ArrayGeneratorThread g2;
    private float pscalarValue;

    public PScalarThread(ArrayGeneratorThread g1, ArrayGeneratorThread g2) {
        this.g1=g1;
        this.g2=g2;
    }

    public void run() {
        try {
            g1.join();
            g2.join();

            pscalarValue = 0;
            float[] v1=g1.getValues();
            float[] v2=g2.getValues();
            for(int i=0; i<v1.length; i++) {
                pscalarValue += v1[i]*v2[i];
            }
        } catch(InterruptedException e) {

        }
    }

    public float getPscalarValue() {
        return pscalarValue;
    }
}

class CollectorThread extends Thread {
    private PScalarThread[] pthreads;

    public CollectorThread(PScalarThread[] pth) {
        this.pthreads = pth;
    }

    public void run() {
        try {
            float sum = 0;
            for (int i = 0; i < pthreads.length; i++) {
                pthreads[i].join();
                sum += pthreads[i].getPscalarValue();
            }
            System.out.println("somma pscalar = "+sum);
        } catch(InterruptedException e) {

        }
    }
}