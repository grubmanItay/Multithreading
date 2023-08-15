package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;

import bgu.spl.mics.application.messages.KillAllBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

/**
 * CPU service is responsible for handling the .
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private CPU cpu;
    private int cpuNumber;
    private int waitTicks = 0;

    public CPUService(String name, CPU cpu, int index, MessageBus msgBus) {
        super(name, msgBus);
        this.cpu = cpu;
        this.cpuNumber = index;
//        this.msgBus = msgBus;
    }

    @Override
    protected void initialize() {
        msgBus.register(this);
        subscribeBroadcast(TickBroadcast.class, ev-> {
            if (ev.getTicks()>= waitTicks) {
                int processTicks = cpu.process();
                if (processTicks > 0) {
                    this.cpu.updateProcessedBatches();
//                    this.cpu.updateTotalTime(processTicks);
                    this.cpu.getCluster().getStatistics().updateCpuTimeUnits(processTicks);
                }
                waitTicks =  processTicks + ev.getTicks();
//                System.out.println("processed " + this.cpuNumber);
            }
        });

        subscribeBroadcast(KillAllBroadcast.class, ev->{
            this.terminate();
        });
    }












}
