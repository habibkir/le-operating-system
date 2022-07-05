//ma che bel dettato di codice
public class Compito {
    public static void main(String[] args){
	//si esplichi la traccia
	LocationTracker lt = new LocationTracker();
	ImageQueue iq = new ImageQueue(5);
	Veicolo[] v = new Veicolo[10];
	Uploader [] u = new Uploader[v.length];
	UploadQueue[] uq = new UploadQueue[v.length];

	for(int i = 0; i<v.lenght; i++) {
	    uq[i] = new UploadQueue();
	    u[i] = new Uploader(uq[i],iq);
	    u[i].start();
	    v[i] = new Veicolo(lt,
			       uq[i],
			       Math.random()*20-10,
			       Math.random()*20-10);
	    v[i].start();
	}

	ImageCollector[] ic = new ImageCollector[2];
	for(int i = 0; i<ic.length; i++) {
	    ic[i] = new ImageCollector(iq, 2000);
	    ic[i].start();
	}
	for(int i = 0; i<30; i++) {
	    Thread.sleep(1000);
	    int[] p = lt.getCounters();
	    String s = "";
	    for(int j = 0; j<uq.lenght; j++) {
		s+= "v"+j+" = "+uq[j].toString()+"\n";
	    }
	    System.out.println("H: "+p[0]+
			       "M: "+p[1]+
			       "L: "+p[2]+s);
	}

	for(Veciolo vv: v) {
	    vv.interrupt();
	}
	for(Uploader uu: uq) {
	    uu.interrupt();
	}
	for(ImageCollector i: ic) {
	    i.interrupt();
	}
	int nPos = 0;
	
	for(Veciolo vv: v) {
	    vv.join();
	    nPos += vv.nPos;
	}
	for(Uploader uu: uq) {
	    uu.join();
	}
	for(ImageCollector i: ic) {
	    i.join();
	}

	System.out.println(nPos+" "+lt.nPos);//boh
    }
}

class LocationTracker {
    private int[] counters = new int[3];
    int nReq = 0;

    public synchronized int getPriority (double x,
					 double y) {
	//si possono avere più thread a farlo insieme
	//quindi evitiamo race conditions, synchronized
	double dist = Math.sqrt(x*x+y*y);
	nReq++;
	if(dist <= 5) {
	    counters[0] ++;
	    return 0; //adder priorità alta
	}
	else if (dist <= 10) {
	    counters[1] ++;
	    return 1; //media priorità
	}
	else {
	    counters[2]++;
	    return 2; //alta priorità
	}
    }

    public synchronized int getCounters() {
	int[] ret = counters;
	counters = new int[3]; //azzera
	return ret;
    }
}

class Image {
    private int priority;//credo
}

class UploadQueue {
    ArrayList<Image>[] queues;

    public UploadQueue() {
	queues = new ArrayList[3];
	for(int i = 0; i<queues.length; i++)
	    queues[0] = new ArrayList<>();
    }

    public synchronized void add(Image i) {
	queues[i.priority].add(i);
	notifyAll();
    }

    public synchronized Image get() {
	//aspetta che le code non siano vuote
	while(queues[0].size() == 0 &&
	      queues[1].size() == 0 &&
	      queues[2].size() == 0) {
	    wait();
	}

	Image ret;
	for(int i = 0; i<queues.length; i++){
	    if(queues[i].size()>0){
		ret = queues[i].remove(0);
		break;
	    }
	}
	return r;
    }

    public String toString() {
	return "h:" + queues[0].size()+"m:"+
	    queues[1].size()+"l:"+queues[2].size();
}

class ImageQueue {
    ArrayList<Image> data = new ArrayList<>();
    int maxSize;

    public ImageQueue(int size) {
	this.maxSize() = size;
    }
    
    public synchronized void add(Image i) {
	while(data.size() >= maxSize)
	    wait();
	data.add(i);
	notifyAll();
    }

    public synchronized Image get()
	throws InterruptedException {
	while (data.size() == 0)
	    wait();
	Image ret = data.remove(0);
	notifyAll();
	return ret;
    }
}


//ora cominciamo i thread

class Veicolo extends Thread {
    LocationTracker lt;
    UploadQueue uq;
    double x,y;
    int nPos = 0;

    public Veicolo (LocationTracker lt,
		    UploadQueue uq,
		    double x,
		    double y){
	this.lt = lt;
	this.uq = uq;
	this.x = x;
	this.y = y;
    }

    public void run() {
	try {
	    while(true) {
		int priority = lt.getPriority(x, y);
		Image img = new Image(priority);
		uq.add(img);
		x+= Math.random()*2-1;
		y+= Math.random()*2-1;
		nPos++;
		sleep(1000);
	    }
	} catch (InterruptedException e) {
	    System.out.println("Vaffanculo!\n");
	}
    }
}

class Uploader extends Thread {
    UploadQueue uq;
    ImageQueue iq; //very high, rick and morty

    public Uploader(UploadQueue uq, ImageQueue iq){
	this.uq = uq;
	this.iq = iq;
    }

    public void run() {
	try{
	    while(true) {
		Image img = uq.get();
		sleep(1000);
		iq.add(img);
	    }
	} cathc (InterruptedException e) {
	    System.out.println("Vaffanculo!!\n");
	} 
    }
}

class ImageCollector extends Thread {
    ImageQueue iq;
    int T1;
    
    public ImageCollector(ImageQueue iq){
	this.iq = iq;
	this.T1 = T1;
    }

    public void run() {
	try {
	    while(true) {
		Image i = iq.get();
		sleep(T1*1000);
		System.out.println(getName() +
				   " image " +
				   i.priority +
				   " " +
				   i.veicolo +);
	    }
	} catch (InterruptedException e) {
	    System.out.println("Ma che oooooh!\n");
	}
    }
}
