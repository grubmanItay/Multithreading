package bgu.spl.mics.application.objects;

import java.util.Vector;

public class Statistics {
    private Vector<String> modelNames;
    private Integer processedBatches;
    private Integer cpuTimeUnits;
    private Integer gpuTimeUnits;

    public Statistics(){
        this.cpuTimeUnits = 0;
        this.gpuTimeUnits = 0;
        this.processedBatches = 0;
        this.modelNames = new Vector<>();
    }

    public int getProcessedBatches() {
        return processedBatches;
    }

    public int getCpuTimeUnits() {
        return cpuTimeUnits;
    }

    public int getGpuTimeUnits() {
        return gpuTimeUnits;
    }

    public Vector<String> getModelNames() {
        return modelNames;
    }

    public void updateCpuTimeUnits(int timeUnits)
    {
        synchronized (cpuTimeUnits) {
            cpuTimeUnits += timeUnits;
        }
    }

    public void updateGpuTimeUnits(int timeUnits) {
        synchronized (gpuTimeUnits) {
            gpuTimeUnits += timeUnits;
        }
    }

    public void addModelName(String name) {
        synchronized (this.modelNames) {
            this.modelNames.add(name);
        }
    }

    public void addProcessedBatches() {
        synchronized (processedBatches) {
            this.processedBatches++;
        }
    }
}
