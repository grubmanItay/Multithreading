package bgu.spl.mics.application.objects;

//import bgu.spl.mics.Event;
//import sun.awt.image.ImageWatched;

import bgu.spl.mics.Event;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {

    public void sendTrainModelToCluster() {
        this.cluster.addTrainModel(this.model);
    }

    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private Cluster cluster;
    private int freeProcessedBatches;
    private int currentFreeSpace;
    private int trainedBatches;
    private int numOfBatches;
    private LinkedList<DataBatch> unProcessedDataBatch;
    private BlockingQueue<DataBatch> processedDataBatch;
    private int totalTime;

    public GPU(String type, Cluster cluster){
        this.cluster = cluster;
        this.unProcessedDataBatch = new LinkedList<>();
        this.processedDataBatch = new LinkedBlockingQueue<>();
        if (type.equals("RTX3090")) {
            this.type = Type.RTX3090;
            this.freeProcessedBatches = 32;
        }
        else if (type.equals("RTX2080")) {
            this.type = Type.RTX2080;
            this.freeProcessedBatches = 16;
        }
        else {
            this.type = Type.GTX1080;
            this.freeProcessedBatches = 8;
        }
        currentFreeSpace = freeProcessedBatches;
        trainedBatches = 0;
        totalTime = 0;
    }

    public int getNumOfBatches(){
        return this.numOfBatches;
    }

    public int getTrainedBatches(){
        return this.trainedBatches;
    }

    public Type getType(){
        return this.type;
    }

//    public void trainModel() {
//        this.model.setStatus(Model.Status.Training);
////        receiveModel(model);
//
//        while (!this.unProcessedDataBatch.isEmpty()) {
//            if (!this.processedDataBatch.isEmpty()) {
////                totalTicks = totalTicks + train(this.processedDataBatch.remove());
//                trainedBatches++;
//                currentFreeSpace++;
//                if (currentFreeSpace > freeProcessedBatches / 2)
//                    for (int i = 0; i < Math.min(freeProcessedBatches, unProcessedDataBatch.size()); i++) {
//                        cluster.getUnProcessedBatch(this.unProcessedDataBatch.removeFirst());
//                        currentFreeSpace--;
//                    }
//            }
//        }
//
//        while (trainedBatches != numOfBatches) {
//            if (!this.processedDataBatch.isEmpty()) {
//                totalTicks = totalTicks + train(this.processedDataBatch.remove());
//                trainedBatches++;
//                currentFreeSpace++;
//            }
//        }
//        this.model.setStatus(Model.Status.Trained);
//        return totalTicks;
//    }

    /**
     * @param model the model that we currently working on
     * @pre: model!=null
     * @pre: this.model==null
     * @post: this.model!=null
     * @post: this.unProcessedDataBatch.size() > 0
     */
    public void receiveModel(Model model) {//receives the model, splits the data into data batches and add them to unProcessedDataBatch
        this.model = model;
        int startIndex = 1;
        numOfBatches = model.getData().getSize() / 1000;
        if (model.getData().getSize() % 1000 != 0)
            numOfBatches++;
        for (int i = 0; i<numOfBatches; i++)
        {
            this.unProcessedDataBatch.push(new DataBatch(model.getData(),startIndex, this));
            startIndex += 1000;
        }

//        for (int i = 0; i < Math.min(freeProcessedBatches, unProcessedDataBatch.size()); i++) {
//            cluster.getUnProcessedBatch(this.unProcessedDataBatch.removeFirst());
//            currentFreeSpace--;
//        }
    }

    public int getFreeProcessedBatches(){
        return this.freeProcessedBatches;
    }

    public int getProcessedDataSize(){
        return this.processedDataBatch.size();
    }

    public int getUnProcessedDataSize(){
        return this.unProcessedDataBatch.size();
    }

    public Model getModel(){
        return this.model;
    }

    /**
     * @pre: this.unProcessedDataBatch.size()>0
     * @post: this.unProcessedDataBatch.size() = this.unProcessedDataBatch.size()@pre - 1
     */
    public void sendToCluster() {//sends to the cluster unprocessed Data batch
        if (this.currentFreeSpace > 0 & !this.unProcessedDataBatch.isEmpty()) {
            DataBatch db = this.unProcessedDataBatch.removeFirst();
            this.cluster.getUnProcessedBatch(db);
            currentFreeSpace--;
        }

        //add to cluster
    }


    /**
     * @param db the processed data batch that we want to add to processedDataBatch
     * @pre: db != null
     * @pre: this.processedDataBatch.size() < this.freeProcessedBatches
     * @post: this.processedDataBatch.size() = this.processedDataBatch.size()@pre + 1
     */
    public void addProcessedDataBatch(DataBatch db){
        synchronized (this.processedDataBatch) {
            this.processedDataBatch.add(db);
            this.processedDataBatch.notifyAll();
        }
//        this.processedDataBatch.notifyAll();
    }

//    public void addEvent(Event e){
//        eventsList.push(e);
//    }


    /**
     * @pre: this.processedDataBatch.size() > 0
     * @post: this.processedDataBatch.size() = this.processedDataBatch.size()@pre - 1
     * @post: returned object is not null
     */
    public int train() {
        synchronized (this.processedDataBatch) {
            int trainTicks=0;
            if (!this.processedDataBatch.isEmpty()) {
                this.processedDataBatch.remove();
                currentFreeSpace++;
                trainedBatches++;
                if (this.type == Type.RTX3090)
                    trainTicks = 1;
                else if (this.type == Type.RTX2080)
                    trainTicks = 2;
                else
                    trainTicks = 4;
//                this.totalTime+= trainTicks;
            }
            this.cluster.getStatistics().updateGpuTimeUnits(trainTicks);
            return trainTicks;
        }
    }

    /**
     * @post: this.model == null
     */
    public void resetModel(){
        this.model = null;
        this.trainedBatches = 0;
        this.numOfBatches = 0;
    }

    public Model.Results testModel(Model model, Student.Degree degree) {
        Model.Results result;
        Random r = new Random();
        int low = 1;
        int high = 10;
        int randomNum = r.nextInt(high-low) + low;
        if (degree == Student.Degree.MSc) {
            if (randomNum <= 6) {
                result = (Model.Results.Good);
                System.out.println("Good results");
            }
            else {
                result = (Model.Results.Bad);
                System.out.println("Bad results");
            }
        }
        else {
            if (randomNum <= 8) {
                result = (Model.Results.Good);
                System.out.println("Good results");
            }
            else {
                result = (Model.Results.Bad);
                System.out.println("Bad results");
            }
        }
        model.setResults(result);
        return result;
    }

    public Cluster getCluster() {
        return cluster;
    }
}
