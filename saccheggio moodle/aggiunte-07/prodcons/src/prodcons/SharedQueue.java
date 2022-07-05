/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prodcons;

/**
 *
 * @author pierf
 */
public interface SharedQueue {
    void add(Object o) throws InterruptedException;
    Object get() throws InterruptedException;
    int size();
}
