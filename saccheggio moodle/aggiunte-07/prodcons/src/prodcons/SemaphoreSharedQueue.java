/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prodcons;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pierf
 */
public class SemaphoreSharedQueue implements SharedQueue{
    List data = new LinkedList();
    Semaphore mutex = new Semaphore(1);
    Semaphore piene = new Semaphore(0);
    Semaphore vuote;

    public SemaphoreSharedQueue(int n) {
        vuote = new Semaphore(n);
    }
    
    @Override
    public void add(Object o) throws InterruptedException {
        vuote.acquire();
        mutex.acquire();
        data.add(o);
        mutex.release();
        piene.release();
    }

    @Override
    public Object get() throws InterruptedException {
        piene.acquire();
        mutex.acquire();
        Object r = data.remove(0);
        mutex.release();
        vuote.release();
        return r;
    }
    
    @Override
    public int size() {
        return data.size();
    }
    
}
