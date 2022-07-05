/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prodcons;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author pierf
 */
public class SynchronizedSharedQueue implements SharedQueue {

    List data = new LinkedList();
    int n;

    public SynchronizedSharedQueue(int n) {
        this.n = n;
    }
    
    @Override
    public synchronized void add(Object o) throws InterruptedException {
        while(data.size()>=n) {
            wait();
        }
        data.add(o);
        notify();
    }

    @Override
    public synchronized Object get() throws InterruptedException {
        while(data.size()==0) {
            wait();
        }
        Object r = data.remove(0);
        notify();
        return r;
    }

    @Override
    public int size() {
        return data.size();
    }
    
}
