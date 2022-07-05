/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compito15feb2022;

import java.util.ArrayList;

/**
 *
 * @author pierf
 */
public class Compito15Feb2022 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Laghetto l = new Laghetto();
        Pesce[] pp = new Pesce[0];
        Pesce.nVivi = pp.length;
        for(int i=0;i<pp.length; i++) {
            pp[i] = new Pesce(l,new Point(Math.random()*20-10,Math.random()*20-10));
            pp[i].start();
        }
        
        Pescatore[] ps = new Pescatore[3];
        for(int i=0;i<ps.length; i++) {
            ps[i] = new Pescatore(l);
            ps[i].setName("P"+i);
            ps[i].start();
        }
        
        for(int i=0;i<30; i++) {
            Thread.sleep(1000);
            /*int nVivi = 0;
            for(Pesce px: pp) {
                if(px.isAlive())
                    nVivi++;
            }*/
            String msg = "";
            int nPescati = 0;
            for(Pescatore px: ps) {
                msg+=px.getName()+ ":" + px.nPescato+" ";
                nPescati += px.nPescato;
            }
            System.out.println(i+") vivi:"+Pesce.nVivi+" "+msg+" tot:"+(Pesce.nVivi+nPescati));
        }
        
        for(Pesce px: pp) {
            px.interrupt();
        }
        for(Pescatore px:ps) {
            px.interrupt();
            px.join();
            System.out.println(px.getName()+" pescati:"+px.nPescato);
        }
     }
    
}

class Point {
    double x,y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    double dist(Point p) {
        return Math.sqrt((this.x-p.x)*(this.x-p.x)+(this.y-p.y)*(this.y-p.y));
    }
    
    public void add(double dx, double dy) {
        x+=dx;
        y+=dy;
    }
}

class Esca {
    Point p;
    Pesce pesce;
    boolean viva = true;

    public Esca(Point p) {
        this.p = p;
    }
    
    public synchronized boolean mangia(Pesce pp) {
        if(viva) {
            viva = false;
            pesce = pp;
            notifyAll();
            return true;
        }
        return false;
    }
    
    public synchronized Pesce waitPesce() throws InterruptedException {
        while(pesce==null)
            wait();
        return pesce;
    }
    
}

class Laghetto {
    ArrayList<Esca> esche = new ArrayList<>();
    
    public synchronized void addEsca(Esca e) {
        esche.add(e);
    }

    public synchronized void removeEsca(Esca e) {
        esche.remove(e);
    }
    
    public synchronized Esca findEsca(Point p, double d) {
        for(Esca e: esche) {
            if(p.dist(e.p)<=d) {
                return e;
            }
        }
        return null;
    }
}

class Pesce extends Thread {
    Laghetto l;
    Point p;
   static int nVivi = 0;

    public Pesce(Laghetto l, Point p) {
        this.l = l;
        this.p = p;
    }
    
    public void run() {
        try {
            while(true) {
                Esca e = l.findEsca(p, 2);
                if(e!=null) {
                    if(e.mangia(this)) {
                        synchronized (Pesce.class) {
                            nVivi--;
                        }
                        break;
                    }
                }
                p.add(Math.random()*2-1,Math.random()*2-1);
                sleep(100);
            }
        } catch(InterruptedException e) {
            
        }
    }
}

class Pescatore extends Thread {
    Laghetto l;
    int nPescato = 0;

    public Pescatore(Laghetto l) {
        this.l = l;
    }
    
    public void run() {
        try {
            while(true) {
                Esca e = new Esca(new Point(Math.random()*20-10,Math.random()*20-10));
                l.addEsca(e);
                Pesce p = e.waitPesce();
                nPescato++;
                l.removeEsca(e);
            }
        } catch(InterruptedException e) {
            
        }
    }
}
