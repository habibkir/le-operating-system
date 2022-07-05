/*
 * 
 */
package compito12giu2017;

import java.util.ArrayList;

/**
 *
 * @author bellini
 */
public class Compito12Giu2017 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException{
        int N = 1000;
        int M = 10;
        int K = 3;
        
        InputSliceManager isl = new InputSliceManager(M);
        OutputSliceManager osl = new OutputSliceManager(K, M);
        ProcessSlice[] ps = new ProcessSlice[M];
        for(int i=0; i<ps.length; i++) {
            ps[i]=new ProcessSlice(i, isl, osl);
            ps[i].start();
        }
        
        for(int i=0; i<3; i++) {
            Object[] data = new Object[N];
            for(int j=0; j<N; j++)
                data[j]=j; //(int)(Math.random()*21)-10;
            isl.processData(data);
            ArrayList[] r=osl.getResults();
            for(int j=0;j<r.length; j++)
                System.out.println(r[j]);
        }
        for(int i=0;i<ps.length; i++)
            ps[i].interrupt();
    }
    
}

class InputSliceManager {
    protected ArrayList[] inputSlices;
    private int nGet; 

    public InputSliceManager(int M) {
        inputSlices = new ArrayList[M];
        for(int i=0;i<inputSlices.length; i++)
            inputSlices[i]=new ArrayList();
        nGet = M;
    }
    
    public synchronized void processData(Object[] data) throws InterruptedException {
        //aspettare
        while(nGet!=inputSlices.length)
            wait();
        nGet = 0;
        split(data);
        notifyAll();
    }
    
    protected void split(Object[] data) {
        //divide i dati tra i vari slice
        int p=0;
        for(int i=0;i<data.length; i++) {
            inputSlices[p].add(data[i]);
            p=(p+1)%inputSlices.length;
        }
    }
    
    public synchronized ArrayList getSlice(int n) throws InterruptedException {
        while(inputSlices[n].isEmpty())
            wait();
        nGet++;
        notifyAll();
        ArrayList r = inputSlices[n];
        inputSlices[n] = new ArrayList();
        return r;
    }
}

class OutputSliceManager {
    private ArrayList[] outputSlices;
    private int nEnd = 0;
    private int nProcess;
    
    public OutputSliceManager(int K, int M) {
        outputSlices=new ArrayList[K];
        for(int i=0;i<outputSlices.length; i++)
            outputSlices[i]=new ArrayList();
        nProcess = M;
    }
    
    public synchronized void putResult(int sl, Object o) {
        outputSlices[sl].add(o);
    }
    
    public synchronized void ended() {
        nEnd++;
        notifyAll();
    }
    
    public synchronized ArrayList[] getResults() throws InterruptedException {
        while(nEnd != nProcess)
            wait();
        ArrayList[] r = outputSlices;
        outputSlices = new ArrayList[r.length];
        for(int i=0;i<outputSlices.length; i++)
            outputSlices[i]=new ArrayList();
        nEnd = 0;
        return r;
    }
}

class ProcessSlice extends Thread {
    private int id;
    private InputSliceManager isl;
    private OutputSliceManager osl;

    public ProcessSlice(int id, InputSliceManager isl, OutputSliceManager osl) {
        this.id = id;
        this.isl = isl;
        this.osl = osl;
    }
    
    public void run() {
        try {
            while(true) {
                ArrayList s = isl.getSlice(id);
                int min = (int) s.get(0);
                int max = min;
                int sum=min;
                for(int i=1; i<s.size(); i++) {
                    int v = (int) s.get(i);
                    if(v<min)
                        min=v;
                    if(v>max)
                        max=v;
                    sum+=v;
                }
                osl.putResult(0, min);
                osl.putResult(1, max);
                osl.putResult(2, sum);
                osl.ended();
            }
        } catch(InterruptedException e) {
            
        }
    }
}