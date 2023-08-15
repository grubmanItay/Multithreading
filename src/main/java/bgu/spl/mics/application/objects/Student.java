package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {

    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private Vector<Model> models;
    private Vector<Model> finishedModels;
    private int currentModelNumber;

    public Student(String name,String department,Degree status){
        this.name=name;
        this.department=department;
        this.status=status;
        this.models= new Vector<>();
        this.finishedModels = new Vector<>();
        this.currentModelNumber = 0;
        this.publications = 0;
        this.papersRead = 0;
    }

    public void addModels(Vector<Model> models){
        this.models = models;
    }

    public Vector<Model> getModels() {
        return this.models;
    }
    //public Student(String name, String department, Degree status)
    //{
    //    this.name = name;
    //    this.department = department;
    //    this.status = status;
    //    this.publications = 0;//   this.papersRead = 0;
    //}

    public int getCurrentModelNumber(){
        return this.currentModelNumber;
    }

    public Degree getStatus() {
        return this.status;
    }

    public void updateCurrentModelNumber(){
        this.currentModelNumber++;
    }

    public void updatePublications(){
        this.publications++;
    }
    public void updatePapersRead(){
        this.papersRead++;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public int getPublications() {
        return publications;
    }

    public Vector<Model> getFinishedModels() {
        return finishedModels;
    }

    public void addFinishedModel(Model m){
        this.finishedModels.add(m);
    }
}
