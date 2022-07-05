/*
 * 
 */
package compito19lug2017;

/**
 *
 * @author bellini
 */
public class Compito19Lug2017 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Stanze stanze = new Stanze();
        int N = 100;
        Visitatore[] v = new Visitatore[N];
        for(int i=0; i<v.length; i++) {
            v[i]= new Visitatore(stanze);
            v[i].start();
        }
        while(stanze.print()) {
            Thread.sleep(1000);
        }
    }
    
}

class Stanze {
    private int[] personePresenti = new int[3];
    private int[] capacitaStanza = new int[3];
    
    public Stanze() {
        capacitaStanza[0]=20;
        capacitaStanza[1]=15;
        capacitaStanza[2]=10;
    }
    
    public synchronized void enterS1() throws InterruptedException {
        while(personePresenti[0]==capacitaStanza[0])
            wait();
        personePresenti[0]++;
        notifyAll();
    }
    
    public synchronized int moveFrom(int stanza, boolean[] visitate) throws InterruptedException {
        int nextRoom;
        while((nextRoom=scegliStanza(stanza,visitate))==-1)
            wait();
        personePresenti[stanza]--;
        personePresenti[nextRoom]++;
        notifyAll();
        return nextRoom;
    }
    
    private int scegliStanza(int stanza, boolean[] visitate) {
        int next = -1;
        for(int i=0; i<personePresenti.length; i++) {
            if(i!=stanza && personePresenti[i]<capacitaStanza[i]) {
                next = i;
                if(!visitate[i]) {
                    break;
                }                    
            }
        }
        return next;
    }
    
    public synchronized void esci(int stanza) {
        personePresenti[stanza]--;
        notifyAll();
    }
    
    public synchronized boolean print() {
        boolean r=false;
        for(int i=0; i<personePresenti.length; i++) {
            System.out.print("S"+(i+1)+" "+personePresenti[i]+" ");
            if(personePresenti[i]>0)
                r=true;
        }
        System.out.println();
        return r;
    }
}

class Visitatore extends Thread {
    private Stanze stanze;

    public Visitatore(Stanze stanze) {
        this.stanze = stanze;
    }
    
    public void run() {
        int nStanzeVisitate = 0;
        int stanzaCorrente = 0;
        boolean[] stanzeVisitate = new boolean[3];
        String seqStanze = "";
        try {
            stanze.enterS1();
            while(nStanzeVisitate<3) {
                if(!stanzeVisitate[stanzaCorrente]) {
                    nStanzeVisitate++;
                    stanzeVisitate[stanzaCorrente]=true;
                }
                sleep(1000);
                seqStanze += stanzaCorrente + " ";
                if(nStanzeVisitate<3)
                    stanzaCorrente = stanze.moveFrom(stanzaCorrente, stanzeVisitate);
            }
            stanze.esci(stanzaCorrente);
            System.out.println("seq "+seqStanze);
        } catch(InterruptedException e) {
            
        }
    }
}