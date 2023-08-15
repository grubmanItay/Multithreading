package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.Vector;

public class PublishConfrenceBroadcast implements Broadcast {
    private Vector<Model> testedModels;
    public PublishConfrenceBroadcast(Vector<Model> testedModels){
        this.testedModels=testedModels;
    }

    public Vector<Model> getTestedModels() {
        return testedModels;
    }
}
