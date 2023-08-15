package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {
    private int start;
    private String name;
    private int date;
    private Vector<Model> testedModels;
    public ConfrenceInformation(String name,int date,int start){
        this.name=name;
        this.date=date;
        this.start=start;
        testedModels=new Vector<>();
    }
    public void addModel(Model m){
        this.testedModels.add(m);
    }
    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }

    public Vector<Model> getTestedModels() {
        return testedModels;
    }

    public int getStart() {
        return start;
    }
}
