package bgu.spl.mics.application.objects;


import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
	private LinkedList<GPU> GPUS;
	private CPU[] CPUS;
	private Queue<DataBatch>[] cpuQueues;
	private int cpuNumber = 0;
//	private int totalProcessedBatches;
	private Vector<String> trainedModels;
	private Statistics statistics;

	public Cluster()
	{
		GPUS = new LinkedList<>();
//		totalProcessedBatches = 0;
		this.trainedModels = new Vector<>();
		this.statistics = new Statistics();

	}

	public void addGPUS(LinkedList<GPU> GPUS)
	{
		this.GPUS = GPUS;
	}

	public void addCPUS(CPU[] CPUS)
	{
		this.CPUS = CPUS;
		this.cpuQueues = new Queue[CPUS.length];
		for (int i = 0; i<this.cpuQueues.length; i++)
			this.cpuQueues[i] = new LinkedBlockingQueue();
	}

	public Cluster(LinkedList<GPU> GPUS, CPU[] CPUS)
	{
		this.GPUS = GPUS;
		this.CPUS = CPUS;
		this.cpuQueues = new Queue[CPUS.length];
		for (int i = 0; i<this.cpuQueues.length; i++)
			this.cpuQueues[i] = new LinkedList<>();
	}

	/**
     * Retrieves the single instance of this class.
     */
//	public static Cluster getInstance() {
//	}

	public void getUnProcessedBatch(DataBatch db){
		synchronized (this.cpuQueues[cpuNumber%CPUS.length]) {
			this.cpuQueues[cpuNumber % CPUS.length].add(db);
			cpuNumber++;
//			this.cpuQueues[cpuNumber%CPUS.length].notifyAll();
		}
	}


	public Queue<DataBatch> getCPUQueue(int cpuNumber) {
		return this.cpuQueues[cpuNumber];
	}

	public void addProcessedBatch(DataBatch db) {
		db.getGpu().addProcessedDataBatch(db);
//		totalProcessedBatches++;
		this.statistics.addProcessedBatches();
	}

	public void addTrainModel(Model m){
		this.trainedModels.add(m.getName());
	}

	public CPU[] getCPUS() {
		return CPUS;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	//	public  int totalProcessedBatches(){
//		int total = 0;
//		for (int i = 0; i < CPUS.length; i++) {
//			total+= CPUS[i].getProcessedBatches();
//		}
//	}
}

