//mi sono perso un paio di un sacco di cose, non so cosa cazzo stia succedendo
public class compito21Lug2021 {
    public static void main (String[] args)
	throws InterruptedException {
	RequestQueue rq = new RequestQueue(5);

	ClientThread[] ct = new ClientThread[10];
	for(int i = 0; i<ct.length(); i++) {
	    ct[i] = new ClientThread(rq);
	    ct[i].setName("CT"+i);
	    ct[i].start();
	}

	WorkerThread[] wt = new WorkerThread[2];
	for(int i = 0; i<wt.length; i++) {
	    wt[i] = new WorkerThread(rq, 100);
	    wt[i].setName("W"+i);
	    wt[i].start();
	}

	Thread.sleep(10*1000);
	
	for(int i = 0; i<ct.length; i++) {
	    ct[i].interrompi = true;
	}

	for(ClientThread c : ct) {
	    c.join();
	}
	System.out.println("clients terminati");
	for(WorkerThread w : wt) {
	    w.interrupt ();
	    //successe roba
	    System.out.println(w.getName()+i+" served"
			       +w.Served());
	}
    }
}

class Request {
    Object data;
    int nReq; //il numero della richiesta
    ResultCollector collector = null;
    public Request(Object data, int nReq,
		   ResultCollector collector) {
	this.data = data;
	this.nReq = nReq;
	this.collector = collector;
    }
}

class RequestQueue {
    ArrayList<Request> data = new ArrayList<>();
    int K;

    public RequestQueue( ArrayList data, int K) {
	this.K = K;
    }

    public synchronized void add(Request r) {
	while(data.size()>=K) {//aspetta se pieno
	    wait();
	}
	data.add(r);

	notifyAll();
    }

    public synchronized Request get() {
	while(data.size()==0) {
	    wait();
	}
	Request r = data.remove(0);
	notifyAll();
	return r;
    }
}

class ResultCollector {
    //ci arrivano le richieste de worker
    //funzionalità per aspettare che...
    int nReq;
    Object[] results;
    int n; //quante sono state inserite

    public void reset(int nr) {
	nReq = nr;
	results = new Object[nr];
	n = 0;
    }

    public synchronized putResult(Object result,
				  int nresult) {
	results[nresult] = result;
	n++;
	if(n==nReq)
	    notifyAll();
    }

    public synchronized void waitResults()
	throws InterruptedException {
	while(n<nReq)
	    wait();
    }
}

class ClientThread extends Thread {
    RequestQueue rq;
    ResultCollector rc = new ResultCollector;
    boolean interrompi = false;

    public ClientThread(RequestQueue rq) {
	this.rq = rq;
    }

    public void run() {
	try {
	    while(!interrompi) {
		long ts = System.currentTimeMillis();
		int nreq = 2+(int)(Math.random()*5);
		rc.reset(nreq);
		for(int i = 0; i<nreq; i++) {
		    rq.add(new Request((i+1)*10,
				       i, rc));
		}
		rc.waitResults();
		int s = 0; //somma
		for(int i = 0; i<nreq; i++) {
		    s+= (Integer)rc.results[i];
		}
		System.out.println(getName()+"n: "
				   +nreq+"sum: "+s
				   +"time:"+
				   System.currentTimeMillis - ts);
	    }
	} catch (InterruptedException e) {
	    //
	}
    }
}

class WorkerThread extends Thread {
    RequestQueue rq;
    int T1;

    WorkerThread (RequestQueue rq, int T1){
	this.rq = rq;
	this.T1 = T1;
    }

    public void run() {
	try {
	    while(true) {
		Request r = rq.get();
		//attesa attiva
		long t = System.currentTimeMillis();
		while((System.currentTimeMillis()-t)<1);
		//siamo arrivati?
		//siamo arrivati?
		//siamo arrivati?
		int v = (Integer)r.data();
		r.collector.putResult(v*2, r.nReq);
	    }
	}catch (InterruptedException e) {
	    System.out.println("worker interrotto");
	}
    }
}
//ho scazzato quelcosa, erano 4 graffe
