package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.Vector;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    private Student student;
    private Future future;

    public StudentService(String name, MessageBus msgBus,Student student1) {
        super(name,msgBus);
        this.student=student1;
        this.future = null;
    }

    @Override
    protected void initialize() {
        msgBus.register(this);
        subscribeBroadcast(TickBroadcast.class, ev-> {
            if (!student.getModels().isEmpty() && this.student.getCurrentModelNumber() < this.student.getModels().size()) {
                Model m = student.getModels().get(student.getCurrentModelNumber());
                if (m.getStatus() == Model.Status.PreTrained & this.future == null)
                {
                    TrainModelEvent trainModelEvent = new TrainModelEvent(m);
                    this.future = sendEvent(trainModelEvent);//next line prob new testmodelevent(v.get(i)); sendevent(testmodel);
                }

                else if (m.getStatus() == Model.Status.Trained & this.future.isDone()){
                    TestModelEvent testModelEvent = new TestModelEvent(m);
                    this.future = sendEvent(testModelEvent);
//                    this.student.updateCurrentModelNumber();
//                    this.future = null;
                }

                else if (m.getStatus() == Model.Status.Tested & this.future.isDone()){
                    if (m.getResults()==Model.Results.Good) {
                        PublishResultsEvent pub = new PublishResultsEvent(m,ev.getTicks());
                        this.future=sendEvent(pub);
//                        this.student.updatePublications();
                    }
                    this.student.updateCurrentModelNumber();
                    this.future = null;
                }
            }
        });

        subscribeBroadcast(PublishConfrenceBroadcast.class,ev->{
            Vector<Model> modelVector=ev.getTestedModels();
            for (int i=0; i< modelVector.size();i++){
                if(modelVector.get(i).getStudent()!=this.student){
                    this.student.updatePapersRead();
                }
            }
        });

        subscribeBroadcast(KillAllBroadcast.class, ev->{
            this.terminate();
        });
    }
}
