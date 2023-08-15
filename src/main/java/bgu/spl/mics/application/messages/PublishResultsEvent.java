package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent implements Event {
    private Model model;
    private int ticks;

    public PublishResultsEvent(Model model,int ticks){
        this.model = model;
        this.ticks = ticks;
    }
    public Model getModel(){
        return this.model;
    }

    public int getTicks() {
        return ticks;
    }
}
