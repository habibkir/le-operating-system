/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread.somma;

/**
 *
 * @author pierf
 */
public class ThreadSomma {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int[] data = new int[10000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (i + 1);
        }
        SumThread[] st = new SumThread[4];
        int n = data.length / st.length;
        for (int i = 0; i < st.length; i++) {
            st[i] = new SumThread(data, i * n, n);
            st[i].start();
        }
        int tot = 0;
        for (int i = 0; i < st.length; i++) {
            st[i].join();
            System.out.println("terminato " + i);
            tot += st[i].getSum();
        }
        System.out.println("somma: " + tot);
    }

}

class SumThread extends Thread {

    private int[] data;
    private int start;
    private int n;
    private int sum = 0;

    public SumThread(int[] data, int start, int n) {
        this.data = data;
        this.start = start;
        this.n = n;
    }

    public int getSum() {
        return sum;
    }

    public void run() {
        System.out.println(getName() + ": start");
        sum = 0;
        try {
            for (int i = start; i < start + n; i++) {
                sum += data[i];
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }
            System.out.println(getName() + ": sum=" + sum);
        } catch (InterruptedException e) {
            System.out.println("interrotto");
        }
    }
}
