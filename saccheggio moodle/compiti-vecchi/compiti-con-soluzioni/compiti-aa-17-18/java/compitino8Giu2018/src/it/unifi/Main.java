package it.unifi;

import java.util.ArrayList;

/**
 *
 * @author bellini
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        int N=20;
        int M=60;

        Dispensa d=new Dispensa();
        Fornelli f=new Fornelli();
        CodaRichieste cr= new CodaRichieste();
        CodaConsegne cc= new CodaConsegne();

        Cuoco[] cuochi = new Cuoco[N];

        for(int i=0; i<N; i++) {
            cuochi[i]=new Cuoco(cr, d, f, cc);
            cuochi[i].setName("C"+i);
            cuochi[i].start();
        }

        Cameriere c1=new Cameriere(cc);
        c1.setName("Cameriere1");
        Cameriere c2=new Cameriere(cc);
        c2.setName("Cameriere2");
        c1.start();
        c2.start();

        for(int i=0;i<M;i++) {
            Richiesta r=new Richiesta(i);

            cr.put(r);
            //System.out.println("gen "+r);
            Thread.sleep(500);
        }
        cc.waitConsegnati(M);
        for(int i=0;i<cuochi.length;i++)
            cuochi[i].interrupt();
        c1.interrupt();
        c2.interrupt();
    }
}

class Dispensa {
    private int[] dispensa = new int[]{200,200,400,500,400,200,600,600,400,300};

    public synchronized boolean getIngredienti(int[] ingredienti) {
        //controlla se tutti gli ingredienti sono presenti in dispensa
        for(int i=0;i<ingredienti.length; i++) {
            if(dispensa[i]<ingredienti[i])
                return false;
        }
        //solo se ci sono tutti gli ingredienti li toglie tutti dalla dispensa
        for(int i=0;i<ingredienti.length; i++) {
            dispensa[i] -= ingredienti[i];
        }
        return true;
    }
}

class Fornelli {
    private int liberi = 10;

    public synchronized void acquire() throws InterruptedException {
        //aspetta se non ci sono fornelli liberi
        while(liberi==0)
            wait();
        liberi--;
    }
    public synchronized void release() throws InterruptedException {
        liberi++;
        notify();
    }
}

class Ricette {
    static int[][] ingredienti = new int[][] {{1,2,2,4,0,0,0,1,0,0},{0,0,1,1,0,0,2,2,3,1},{1,0,1,0,1,2,2,2,1,0},{0,0,0,0,3,0,2,1,0,2}};
}

class Richiesta {
    int id;
    int[] piattiRichesti = new int[4];
    boolean[] piattiAssegnati = new boolean[4];
    int[] piattiCompletati = new int[4];
    int nTipiPiattiCompletati = 0;
    int nTipiPiattiDaCompletare = 0;

    public Richiesta(int n) {
        // crea la richiesta
        id=n;
        for(int i=0;i<piattiRichesti.length;i++) {
            piattiRichesti[i]=1+(int)(Math.random()*3);
        }
    }

    public synchronized boolean completato(int tipo,int nPiatti){
        //indica che ha completato un tipo di piatto
        piattiCompletati[tipo]=nPiatti;
        //possibile racecondition,
        //due o più cuochi possono terminare contemporaneamente due piatti diversi
        nTipiPiattiCompletati++;
        //controlla se sono stati completati tutti i tipi di piatti
        if(nTipiPiattiCompletati==4)
            return true;
        return false;
    }

    @Override
    public String toString() {
        String s=""+id+" ";
        for(int i=0;i<4;i++) {
            s += piattiRichesti[i];
        }
        s+=" ";
        for(int i=0;i<4;i++) {
            s += piattiCompletati[i];
        }
        return s;
    }
}

class Assegnazione {
    public Richiesta richiesta;
    public int tipoPiatto;

    public Assegnazione(Richiesta r, int p) {
        richiesta=r;
        tipoPiatto=p;
    }
}

class CodaRichieste {
    //coda delle richieste
    private ArrayList<Richiesta> richieste = new ArrayList<>();
    //richiesta che è in assegnazione
    private Richiesta pending = null;

    public synchronized void put(Richiesta r) {
        richieste.add(r);
        notify();
    }

    public synchronized Assegnazione get() throws InterruptedException {
        //aspetta se non ci sono richieste e non c'è una richiesta in assegnazione
        while(richieste.size()==0 && pending==null)
            wait();
        //se non c'è una richiesta in assegnazione la estrae dalla coda
        if(pending==null)
            pending = richieste.remove(0);
        //cerca il tipo di piatto da assegnare
        int tipoPiatto = -1;
        for(int i=0;i<4;i++) {
            if(!pending.piattiAssegnati[i]) {
                pending.piattiAssegnati[i]=true;
                pending.nTipiPiattiDaCompletare++;
                tipoPiatto = i;
                break;
            }
        }
        Richiesta r = pending;
        //se sono stati assegnati tutti i tipi di piatti mette pending = null
        if(pending.nTipiPiattiDaCompletare==4)
            pending=null;
        return new Assegnazione(r, tipoPiatto);
    }
}

class CodaConsegne {

    private ArrayList<Richiesta> consegne = new ArrayList<Richiesta>();
    private int consegnati = 0;

    public synchronized Richiesta get() throws InterruptedException {
        //aspetta se non ci sono piatti da consegnare
        while(consegne.size()==0)
            wait();
        consegnati++;
        notifyAll();
        return consegne.remove(0);
    }

    public synchronized void put(Richiesta r) {
        consegne.add(r);
        notifyAll();
    }

    public synchronized void waitConsegnati(int n) throws InterruptedException {
        //aspetta se non sono stati consegnati n piatti
        while(consegnati<n)
            wait();
    }
}

class Cuoco extends Thread {
    private CodaRichieste richieste;
    private CodaConsegne consegne;
    private Dispensa dispensa;
    private Fornelli fornelli;

    public Cuoco(CodaRichieste cr, Dispensa d, Fornelli f, CodaConsegne cc) {
        richieste=cr;
        consegne=cc;
        dispensa =d;
        fornelli=f;
    }

    public void run() {
        try {
            while(true) {
                //richiede un piatto da preparare
                Assegnazione a = richieste.get();
                //System.out.println(getName()+" INIZIA "+a.richiesta.id+" "+a.tipoPiatto);
                //prende gli ingredienti per il numero di piatti da preparare
                int nPiatti = 0;
                for(int i=0;i<a.richiesta.piattiRichesti[a.tipoPiatto];i++) {
                    boolean presi=dispensa.getIngredienti(Ricette.ingredienti[a.tipoPiatto]);
                    if(presi)
                        nPiatti++;
                    else
                        break;
                }
                //nPiatti indica quanti piatti si possono effettivamente preparare
                if(nPiatti>0) {
                    fornelli.acquire();
                    sleep(1000);
                    fornelli.release();
                }
                //System.out.println(getName()+" FINITO "+a.richiesta.id+" "+a.tipoPiatto);
                //indica che il piatto è stato completato
                if(a.richiesta.completato(a.tipoPiatto, nPiatti)) {
                    //System.out.println(getName()+" CONSEGNA "+a.richiesta.id+" "+a.tipoPiatto);

                    //mette in coda consegne, tutti i piatti sono stati completati
                    consegne.put(a.richiesta);
                }
            }
        } catch(InterruptedException e) {

        }
    }
}

class Cameriere extends Thread {
    CodaConsegne consegne;

    public Cameriere(CodaConsegne consegne) {
        this.consegne = consegne;
    }

    public void run() {
        try {
            while(true) {
                //prende una richiesta e la consegna
                Richiesta r=consegne.get();
                System.out.println(getName()+" consegna "+r);
            }
        } catch(InterruptedException e) {

        }
    }
}