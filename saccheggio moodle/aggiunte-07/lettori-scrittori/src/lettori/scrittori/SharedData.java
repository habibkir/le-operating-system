/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lettori.scrittori;

/**
 *
 * @author pierf
 */
public interface SharedData {
    void startReadAccess() throws InterruptedException;
    void endReadAccess() throws InterruptedException;
    
    void startWriteAccess() throws InterruptedException;
    void endWriteAccess() throws InterruptedException;
}
