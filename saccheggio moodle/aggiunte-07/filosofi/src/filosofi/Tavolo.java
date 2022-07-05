/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filosofi;

/**
 *
 * @author pierf
 */
public interface Tavolo {
    
    void getBacchette(int idFilosofo) throws InterruptedException;
    void releaseBacchette(int idFilosofo);
}
