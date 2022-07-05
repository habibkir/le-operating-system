/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito10gen2022;

import java.util.ArrayList;

/**
 *
 * @author pierf
 */
public class Compito10Gen2022 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Table t = new Table(5);
        Banco b = new Banco(t);
        Giocatore[] giocatori = new Giocatore[5];
        for(int i=0;i<giocatori.length; i++) {
            giocatori[i] = new Giocatore(t, 5000);
            giocatori[i].setName("G"+i);
            giocatori[i].start();
        }
        b.start();
        b.join();
        double amounts=b.totalAmount;
        for(Giocatore g: giocatori) {
            System.out.println(g.getName()+" g:"+g.nGiocate+" v:"+g.nVinte);
            amounts += g.totalAmount;
        }
        System.out.println("totale:"+amounts);
    }
    
}

class Bet {
    int number;
    double amount;
    Giocatore g;

    public Bet(int number, double amount, Giocatore g) {
        this.number = number;
        this.amount = amount;
        this.g = g;
    }
}

class Table {
    ArrayList<Bet> bets = new ArrayList<>();
    int nGiorcatori;

    public Table(int nGiorcatori) {
        this.nGiorcatori = nGiorcatori;
    }
    
    synchronized void addBet(Bet b) {
        bets.add(b);
        notifyAll();
    }
    
    synchronized void endGiocatore() {
        nGiorcatori--;
        notifyAll();
    }
    
    synchronized ArrayList<Bet> waitAllBets() throws InterruptedException {
        while(bets.size()<nGiorcatori)
            wait();
        ArrayList<Bet> r = bets;
        bets = new ArrayList<>();
        return r;
    }
    
}

class Giocatore extends Thread {
    Table t;
    double totalAmount;
    Double result = null;
    int nVinte = 0;
    int nGiocate = 0;

    public Giocatore(Table t, double totalAmount) {
        this.t = t;
        this.totalAmount = totalAmount;
    }
    
    synchronized void waitResult() throws InterruptedException {
        while(result==null)
            wait();
    }
    
    synchronized void setResult(double r) {
        result = r;
        notifyAll();
    }
    
    public void run() {
        try {
            while(totalAmount>1) {
                int betNumber = (int)(Math.random()*100)+1;
                double bet = totalAmount*0.2;
                totalAmount -= bet;
                t.addBet(new Bet(betNumber, bet , this));
                result = null;
                this.waitResult();
                if(result>0) {
                    nVinte++;
                    totalAmount += result;
                }
                System.out.println(getName()+" "+betNumber+" "+result+" "+totalAmount);
                nGiocate++;
            }
            System.out.println(getName()+" FINITO");
            t.endGiocatore();
        } catch(InterruptedException e) {
            
        }
    }
}

class Banco extends Thread {
    Table t;
    double totalAmount = 0;

    public Banco(Table t) {
        this.t = t;
    }
    
    public void run() {
        try {
            while(t.nGiorcatori>0) {
                int bet = 1 + (int)(Math.random()*100);
                ArrayList<Bet> bets = t.waitAllBets();
                System.out.println("BANCO "+bet);
                ArrayList<Giocatore> winners = new ArrayList<>();
                double totBets = 0;
                int minDist = 100;
                for(Bet b: bets) {
                    int dist = bet-b.number;
                    totBets += b.amount;
                    if(dist>=0) {
                        if(dist < minDist) {
                            minDist = dist;
                            winners.clear();
                            winners.add(b.g);
                        } else if(dist == minDist) {
                            winners.add(b.g);
                        }
                    }
                }
                double result = 0;
                if(winners.size()>0) {
                    result = totBets/winners.size();
                } else {
                    totalAmount += totBets;
                }
                for(Bet b:bets) {
                    if(winners.contains(b.g)) {
                        b.g.setResult(result);
                    } else {
                        b.g.setResult(0);
                    }
                }
            }
        } catch(InterruptedException e) {
            
        }
    }
}
