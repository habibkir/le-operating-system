/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread.ricerca;

/**
 *
 * @author pierf
 */
public class ThreadRicerca {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int[] data = new int[10000];
        for (int i = 0; i < data.length; i++) {
            double x = Math.random();
            data[i] = (int)(x*100);
            System.out.println(i+": "+data[i]+" "+x);
        }
        int v = 0;
        SearchThread[] st = new SearchThread[4];
        int n = data.length / st.length;
        for (int i = 0; i < st.length; i++) {
            st[i] = new SearchThread(data, i * n, n, v);
            st[i].start();
        }
        for (int i = 0; i < st.length; i++) {
            st[i].join();
            if(st[i].getResult()) {
                System.out.println(i+" trovato!");
            }
        }
    }
    
}

class SearchThread extends Thread {
    private int[] data;
    private int start;
    private int n;
    private int v;
    private boolean result = false;

    public SearchThread(int[] data, int start, int n, int v) {
        this.data = data;
        this.start = start;
        this.n = n;
        this.v = v;
    }
    
    public boolean getResult() {
        return result;
    }
    
    public void run() {
        result = false;
        for(int i=start; i<start+n; i++) {
            if(data[i]==v) {
                result=true;
                break;
            }
        }
    }
}