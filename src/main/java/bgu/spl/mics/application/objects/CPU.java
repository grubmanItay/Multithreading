package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int cores;
    private Collection<DataBatch> data;
    private Cluster cluster;
    private  int cpuNumber;
    private int processedBatches;
    private int totalTime;

    public CPU(int cores, Cluster cluster, int cpuNumber)
    {
        this.cores = cores;
        this.cluster = cluster;
        this.cpuNumber = cpuNumber;
        this.data = new LinkedList<DataBatch>();
        this.processedBatches = 0;
        this.totalTime = 0;
    }

    /**
     * @param db data batch that needs to be processed
     * @pre: db != null
     * @pre: this.data != null
     * @post: this.data.size() = this.data.size()@pre + 1
     */
    public void addDataBatch(DataBatch db){}

    /**
     * @pre: this.data.size() > 0
     * @post: this.data.size() = this.data.size()@pre - 1
     * @post: return value is the right number of ticks take to process the data batch
     */
    public int process(){
        int ticks = 0;
        synchronized (this.cluster.getCPUQueue(cpuNumber)) {
            if (!this.cluster.getCPUQueue(cpuNumber).isEmpty()) {
                DataBatch db = this.cluster.getCPUQueue(cpuNumber).remove();
                cluster.addProcessedBatch(db);
                if (db.getData().getType() == Data.Type.Images)
                    ticks = (32 / this.cores) * 4;
                else if (db.getData().getType() == Data.Type.Text)
                    ticks = (32 / this.cores) * 2;
                else
                    ticks = (32 / this.cores) * 1;
            }
        }
        return ticks;
    }

    public int getCores(){
        return this.cores;
    }

    public Collection<DataBatch> getData(){
        return this.data;
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    public int getProcessedBatches() {
        return processedBatches;
    }

    public void updateProcessedBatches(){
        processedBatches++;
    }

    public void updateTotalTime(int ticks){
        totalTime+= ticks;
    }
}
