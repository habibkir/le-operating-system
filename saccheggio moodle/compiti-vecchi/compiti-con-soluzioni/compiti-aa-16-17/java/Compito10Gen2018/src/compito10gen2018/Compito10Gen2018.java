/*
 * 
 */
package compito10gen2018;

/**
 *
 * @author bellini
 */
public class Compito10Gen2018 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        int N=20;
        int M=2;
        
        CounterMod10[] c=new CounterMod10[M];
        for(int i=0; i<c.length; i++) {
            c[i]=new CounterMod10();
        }
       
        Buffer b=new Buffer(N);
        
        Worker[] w=new Worker[N];
        for(int i=0; i<w.length; i++) {
            w[i]=new Worker(i, c, b);
            w[i].start();
        }
        Collector cl=new Collector(b);
        cl.start();
        Thread.sleep(10000);
        cl.interrupt();
        for (Worker w1 : w) {
            w1.interrupt();
        }
    }
    
}

class CounterMod10 {
    private int value = 0;
    
    public synchronized int inc() {
        int r = value;
        value = (value+1)%10;
        return r;
    }
}

class Buffer {
    private int[] value;
    private int nValuesReady;
    
    public Buffer(int n) {
        value = new int[n];
    }
    
    public synchronized void setValue(int p, int v) throws InterruptedException{
        while(value[p]>0)
            wait();
        value[p]=v;
        nValuesReady++;
        notifyAll();
    }
    
    public synchronized int[] getValues() throws InterruptedException {
        while(nValuesReady<value.length)
            wait();
        int[] r = new int[value.length];
        for(int i=0; i<value.length; i++) {
            r[i]=value[i];
            value[i]=0;
        }
        nValuesReady=0;
        notifyAll();
        return r;
    }
    
}

class Worker extends Thread {
    CounterMod10[] c;
    Buffer b;
    int id;
    
    public Worker(int id,CounterMod10[] c, Buffer b) {
        this.c=c;
        this.b=b;
        this.id=id;
    }
    
    public void run() {
      try {
        while(true) {
            int p = (int)(Math.random()*c.length);
            CounterMod10 cc = c[p];
            int v = cc.inc();
            b.setValue(id, (p+1)*10+v);
        }
      } catch(InterruptedException e) {
          
      }
    }

}

class Collector extends Thread {
    Buffer b;
    
    public Collector(Buffer b) {
        this.b=b;
    }
    public void run() {
      try{
        while(true) {
            int[] v = b.getValues();
            int s=0;
            for(int i=0; i<v.length; i++)
                s+=v[i];
            System.out.println("somma:"+s);
            //somma e stampa
        }
      } catch(InterruptedException e) {
          
      }
    }
}