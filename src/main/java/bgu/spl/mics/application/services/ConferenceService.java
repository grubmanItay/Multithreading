package bgu.spl.mics.application.services;
//
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.KillAllBroadcast;
import bgu.spl.mics.application.messages.PublishConfrenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.Vector;

/**
// * Conference service is in charge of
// * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
// * after publishing results the conference will unregister from the system.
// * This class may not hold references for objects which it is not responsible for.
// *
// * You can add private fields and public methods to this class.
// * You MAY change constructor signatures and even add new public constructors.
// */
public class ConferenceService extends MicroService {
    private ConfrenceInformation con;
    public ConferenceService(String name, MessageBus msgBus, ConfrenceInformation con) {
        super(name, msgBus);
        this.con=con;
    }

    @Override
    protected void initialize() {
        msgBus.register(this);
        subscribeEvent(PublishResultsEvent.class,ev->{
            if (ev.getTicks()<this.con.getDate() & ev.getTicks()>= this.con.getStart())
                con.addModel(ev.getModel());
            else
                sendEvent(ev);
        });
        subscribeBroadcast(TickBroadcast.class, ev-> {
            if (ev.getTicks()==con.getDate()){
                PublishConfrenceBroadcast pub=new PublishConfrenceBroadcast(this.con.getTestedModels());
                msgBus.sendBroadcast(pub);
                for (int i = 0; i < this.con.getTestedModels().size(); i++) {
                    this.con.getTestedModels().get(i).getStudent().updatePublications();
                }
                msgBus.unregister(this);
                this.terminate();
            }

//            if (ev.getTicks() == 499)
//                System.out.println("hey there");
        });

        subscribeBroadcast(KillAllBroadcast.class, ev->{
            this.terminate();
        });
   }
}
