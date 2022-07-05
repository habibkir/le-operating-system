/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito06lug2020;

/**
 *
 * @author pierf
 */
public class Compito06Lug2020 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        int N = 20;
        Ambiente a = new Ambiente(N);
        
        Persona[] p = new Persona[N];
        for(int i=0;i<p.length; i++) {
            p[i] = new Persona(a,i);
            p[i].start();
        }
        
        Thread.sleep(30000);
        
        for(int i=0;i<p.length;i++) {
            p[i].interrupt();
            p[i].join();
            System.out.println("P"+i+" nchange:"+p[i].nchange+" nwait:"+p[i].nwait+" totDisit:"+p[i].totDist);
        }
    }
    
}

class Position {
    double x,y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Position(Position p) {
        x = p.x;
        y = p.y;
    }
    
    public void add(Position p) {
        x += p.x;
        y += p.y;
    }
    
    public double dist(Position p) {
        return Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y));
    }
    
    @Override
    public String toString() {
        return x+","+y+" ";
    }
}

class Ambiente {
    Position[] pos;

    public Ambiente(int n) {
       pos = new Position[n];
       for(int i=0; i<n; i++)
           pos[i] = new Position(0,0);
    }
    
    public synchronized int updatePos(int idPers, Position dxy) throws InterruptedException {
        int nwait = 0;
        while(checkPos(idPers,dxy)) {
            nwait++;
            wait();
        }
        pos[idPers].add(dxy);
        notifyAll();
        return nwait;
    }
    
    private boolean checkPos(int idPers, Position dxy) {
        Position newPos = new Position(pos[idPers]);
        newPos.add(dxy);
        for(int i=0; i<pos.length; i++) {
            if(i!=idPers && pos[i].dist(newPos)<1) {
                return true;
            }
        }
        return false;
    }
}

class Persona extends Thread {
    Ambiente a;
    int id;
    int nwait = 0;
    int nchange = 0;
    double totDist = 0.0;

    public Persona(Ambiente a, int id) {
        this.a = a;
        this.id = id;
    }

    public void run() {
        try {
            while(true) {
                Position dxdy=new Position(Math.random()*20-10,Math.random()*20-10);
                System.out.println("P"+id+" sposta a "+dxdy);
                nwait += a.updatePos(id, dxdy);
                nchange++;
                totDist += dxdy.dist(new Position(0,0));
                sleep(100);
            }
        } catch(InterruptedException e) {
            System.out.println("P"+id+" interrotto");
        }
        
    }
    
    
}