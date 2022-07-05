/* client thread, ce ne sono  N
 * request queue, una
 * worker thread, ce ne sono M
 * resource manager, uno */


public class compito {
    public static void main(String args[]) {
	System.out.println("waluigi\n");
    }
}

class Request {
    Object data;
    Object result = null;
    long start; //will be currentTimeMillis
    ClientThread sender;

    public Request(roba)
	this.roba = roba;
}

class RequestQueue {
    ArrayList<Request> data = new ArrayList();
    Semaphore mutex = new Semaphore (1);
    Semaphore piene = new Semaphore (0);
    Semaphore vuote;

    public RequestQueue (int n) {
	vuote = new Semaphore(n);
    }

    public void add(Request r) {
	vuote.acquire();
	mutex.acquire();
	data.add(r);
	mutex.release();
	piene.release();
    }

    public Request get() throws InterruptedException {
	piene.acquire();
	mutex.acquire();
	Request r = data.remove(0);
	mutex.release();
	vuote.release();
	return r;
    }
}

class ResourceManager {
    Semaphore ra;
    Semaphore rb;

    public ResourceManager(int NA, int NB) {
	ra = new Semaphore(NA);
	rb = new Semaphore(NB);
    }

    //non serve synchronized, usiamo solo i semafori
    public void getA() throws InterruptedException {
	ra.acquire();
    }
    public void getB() throws InterruptedException {
	rb.acquire();
    }

    public void releaseA() {
	ra.release();
    }
    public void releaseB() {
	rb.release();
    }
}

class ClientThread extends Thread {
    RequestQueue rq;
    Semaphore reply = new Semaphore(0);

    public ClientThread(RequestQueue rq) {
	this.rq = rq;
    }
    
    public void run() {
	try {
	    while(true) {
		Request r =
	} catch (InterruptedException e){
	    System.out.println("Ma chi è quel mona\n");
			       
    }
}
