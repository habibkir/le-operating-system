public class Compito25Gen2022 {
    public static void main (String args[]) {
	int M = 10;
	ArrayList<Integer> d = new ArrayList();
	int v = (int)(Math.random()*100);
	d.add(v);
	System.out.println("0 "+v);
	for(int i=0; i<M-1; i++) {
	    v+= 1 + (int)(Math.random()*99);
	    System.out.println(""+(i+1)+" "+v);
	    d.add(v);
	}
	Container c = new Container();
	HashTable ht = new HashTable(M);

	Worker[] w = new Worker[4];
	for(int i=0; i<w.lenght; i++) {
	    w[i] = new Worker(c,ht);
	    w[i].setName("W"+i);
	    w[i].start();
	}
	//inizieranno a prendere dati dal container
	//e a metterli nella hashtable
	Collector cl = new Collector(ht,c);
	cl.start();
	cl.join(); //aspetta che finisca
	for(Worker k:w) {
	    k.interrupt();
	    k.join();
	    System.out.println(k.getName()+" "+
			       k.nOccupato);
	}
    }
}

class Container {
    ArrayList ata;
    Semaphore pieni = new Semaphore(0);
    Semaphore mutex = new Semaphore(1);

    public Container(ArrayList data) {
	this.data = data;
	this.pieni = new Semaphore(data.size());
    }

    public Object get() throws InterruptedException {
	pieni.acquire();
	//se 2 thread generano lo stesso
	//valore di p che fo?
	mutex.acquire();
	int p = (int)(Math.random()*data.size());
	Object v = data.remove(p);
	mutex.release();
	return v;
    }

    public void add(Object[] o)
	throws InterruptedException {
	//lo userà il collector
	mutex.acquire();
	data.addAll(o);
	mutex.release();
	pieni.release(o.length);
    }
}

class HashTable { //le librerie standard sono per deboli
    Object[] data;
    Semaphore mutex = new Semaphore(1);

    public HashTable(int M) {
	data = new Object[M];
    }

    public int add(int v) {
	mutex.acquire();
	int nOccupato = 0;
	int p = v % data.length; //hashing ideale
	while(data[p]!=null) {
	    p = (p+1) % data.length; //minchia che pro
	    nOccupato ++;
	    mutex.release();
	    //controlla se p aggiornato libero
	    //se al rilascio qualcheduno la se la piglia
	    mutex.acquire();
	}
	data[p] = v;
	System.out.println
	    (Thread.currentThread.getName());
	mutex.release();
	piene.release();
	return nOccupato;
    }

    public Object[] getAll() {
	piene.acquire(data.lenght);//aquire(n),o N, boh
	mutex.acquire();
	Object[] r = data;
	data = new Object[data.length];
	mutex.release();
	return r;
    }
}

class Worker extends Thread {
    Container c;
    HashTable ht;
    int nOccupato = 0;

    public void run() {
	try {
	    while(true) {
		int v = (Integer) c.get();
		System.out.println(getName()+" "+v);
		nOccupato += ht.add(v);
	    }
	} catch (InterruptedException e) {
	    System.out.println("aaaa");
	}
    }
}

class Collector extends Thread {
    HashTable ht;
    Container c;

    public Collector(HashTable ht, Container c) {
	this.ht = ht;
	this.c = c;
    }

    public void run() {
	try {
	    for(int i=0; i<3; i++) {
		Object[] o = ht.getAll();
		for(int j=0; j<o.length; j++) {
		    System.out.println(o[j]);//lui usa i
		}
		c.add(o);
	    }
	} catch (InterruptedException e) {
	    //
	}
	System.out.println("Collector finito");
    }
}
	    
