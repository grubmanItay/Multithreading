package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private Data data;
    private int startIndex;
    private GPU gpu;

    public DataBatch(Data data, int startIndex, GPU gpu)
    {
        this.data = data;
        this.startIndex = startIndex;
        this.gpu = gpu;
    }

    public Data getData()
    {
        return this.data;
    }

    public int getStartIndex()
    {
        return this.startIndex;
    }

    public GPU getGpu()
    {
        return this.gpu;
    }
}
