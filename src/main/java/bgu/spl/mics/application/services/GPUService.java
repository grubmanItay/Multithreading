package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.KillAllBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link },
 * in addition to sending the {@link }.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private GPU gpu;

    private LinkedList<TrainModelEvent> trainModelList;
    private LinkedList<TestModelEvent> testModelList;
    private int waitTicks = 0;

    public GPUService(String name, GPU gpu, MessageBus msgBus) {
        super(name,msgBus);
        this.gpu = gpu;
        this.trainModelList = new LinkedList<>();
        this.testModelList = new LinkedList<>();
//        this.msgBus = msgBus;
    }

    @Override
    protected void initialize() {
        msgBus.register(this);
        subscribeEvent(TrainModelEvent.class, ev -> {
            trainModelList.add(ev);});
        subscribeBroadcast(TickBroadcast.class, ev-> {
            if (ev.getTicks() == 5499)
                System.out.println("");
            if (!this.testModelList.isEmpty()) {
                TestModelEvent testModelEvent = testModelList.remove();
                Model.Results result = this.gpu.testModel(testModelEvent.getModel(), testModelEvent.getModel().getStudent().getStatus());
                complete(testModelEvent, result);
                testModelEvent.getModel().setStatus(Model.Status.Tested);
                testModelEvent.getModel().getStudent().addFinishedModel(testModelEvent.getModel());
                this.gpu.getCluster().getStatistics().addModelName(testModelEvent.getModel().getName());
            }
            if (this.gpu.getModel() == null) {
                if (!trainModelList.isEmpty()) {
                    this.gpu.receiveModel(trainModelList.peek().getModel());
                    this.gpu.getModel().setStatus(Model.Status.Training);
                }
            }
            else {
                if (ev.getTicks()>= waitTicks) {
//                    if (this.gpu.getType() == GPU.Type.RTX3090) {
                        System.out.println(this.getName() + "Trained: "+this.gpu.getTrainedBatches()+" Batches");
                        if (this.gpu.getNumOfBatches() == this.gpu.getTrainedBatches()) {
                            System.out.println("finished");
                            this.gpu.getModel().setStatus(Model.Status.Trained);
                            complete(trainModelList.remove(), this.gpu.getModel());
                            this.gpu.sendTrainModelToCluster();
                            gpu.resetModel();
                        }
                        else {
                            int trainTicks = this.gpu.train();
                            waitTicks = ev.getTicks()+ trainTicks;
                            this.gpu.sendToCluster();
                        }
//                    }
//                    else if (this.gpu.getType() == GPU.Type.RTX2080) {
//                        if (ev.getTicks() % 2 == 0) {
//                            System.out.println(this.getName() + "Trained: " + this.gpu.getTrainedBatches() + " Batches");
//                            if (this.gpu.getNumOfBatches() == this.gpu.getTrainedBatches()) {
//                                this.gpu.getModel().setStatus(Model.Status.Trained);
//                                complete(trainModelList.remove(), this.gpu.getModel());
//                                gpu.resetModel();
//                            } else {
//                                this.gpu.train();
//                                this.gpu.sendToCluster();
//                            }
//
//                        }
//                    }
//                    else {
//                        if (ev.getTicks() % 4 == 0) {
//                            System.out.println(this.getName() + "Trained: " + this.gpu.getTrainedBatches() + " Batches");
//                            if (this.gpu.getNumOfBatches() == this.gpu.getTrainedBatches()) {
//                                this.gpu.getModel().setStatus(Model.Status.Trained);
//                                complete(trainModelList.remove(), this.gpu.getModel());
//                                gpu.resetModel();
//                            } else {
//                                this.gpu.train();
//                                this.gpu.sendToCluster();
//                            }
//                        }
//                    }
                }
            }
        });
        subscribeEvent(TestModelEvent.class, ev->{
            this.testModelList.add(ev);
        });

        subscribeBroadcast(KillAllBroadcast.class, ev->{
            this.terminate();
        });

    }
}
