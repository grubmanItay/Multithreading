package bgu.spl.mics.application;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.GetStarted;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;

import java.io.*;
import java.util.LinkedList;
import java.util.Vector;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {

        Cluster cluster = new Cluster();
        MessageBus msgb= new MessageBusImpl();
        LinkedList<Student> studentList = new LinkedList<>();
        LinkedList<ConfrenceInformation> conferenceList = new LinkedList<>();
//        File input = new File("C:\\Users\\Gil\\Downloads\\assigenment2_2_2_2\\example_input.json");
        File input = new File(args[0]);
        try {
            int threadNumber = 1;
            JsonElement fileElement = JsonParser.parseReader(new FileReader(input));
            JsonObject fileObject = fileElement.getAsJsonObject();

            JsonArray students = fileObject.get("Students").getAsJsonArray();
            JsonArray gpus = fileObject.get("GPUS").getAsJsonArray();
            JsonArray cpus = fileObject.get("CPUS").getAsJsonArray();
            JsonArray conferences = fileObject.get("Conferences").getAsJsonArray();
            int tickTime = fileObject.get("TickTime").getAsInt();
            int duration = fileObject.get("Duration").getAsInt();

            for (JsonElement studentElement : students) {
                JsonObject studentObject = studentElement.getAsJsonObject();

                String name = studentObject.get("name").getAsString();
                String department = studentObject.get("department").getAsString();
                String statusString = studentObject.get("status").getAsString();
                Student.Degree status;
                if (statusString.equals("MSc"))
                    status = Student.Degree.MSc;
                else
                    status = Student.Degree.PhD;
                JsonArray modelArray = studentObject.get("models").getAsJsonArray();
                Vector<Model> modelVector = new Vector<>();
                Student newStudent = new Student(name, department, status);
                for (JsonElement modelElement : modelArray) {
                    JsonObject modelObject = modelElement.getAsJsonObject();
                    String modelName = modelObject.get("name").getAsString();
                    String dataTypeString = modelObject.get("type").getAsString();
                    Data.Type dataType;
                    if (dataTypeString.equals("images"))
                        dataType = Data.Type.Images;
                    else if (dataTypeString.equals("Text"))
                        dataType = Data.Type.Text;
                    else
                        dataType = Data.Type.Tabular;
                    int size = modelObject.get("size").getAsInt();
                    Model model = new Model(modelName, new Data(dataType, size), newStudent);
                    modelVector.add(model);
                }
                newStudent.addModels(modelVector);
                studentList.push(newStudent);
                StudentService studentService = new StudentService(name+"Service", msgb, newStudent);
                Thread t = new Thread(studentService);
                t.start();

            }
//            Cluster cluster1 = new Cluster();
            LinkedList<GPU> GPUS = new LinkedList<>();
            CPU[] CPUS = new CPU[cpus.size()];
            int gpuNumber =0;
            for (JsonElement gpuElement : gpus) {
                String gpuTypeString = gpuElement.getAsString();
                GPU newGpu = new GPU(gpuTypeString, cluster);
                GPUService gpuService = new GPUService("GpuService" + gpuNumber,newGpu,msgb);
                Thread t = new Thread(gpuService);
                t.start();
                GPUS.push(newGpu);
                gpuNumber++;
            }
            int cpuNumber = 0;
            for (JsonElement cpuElement : cpus) {
                int cpuCores = cpuElement.getAsInt();
                CPU newCpu = new CPU(cpuCores, cluster, cpuNumber);
                CPUService cpuService = new CPUService("CpuService" + cpuNumber,newCpu, cpuNumber,msgb);
                Thread t = new Thread(cpuService);
                t.start();
                CPUS[cpuNumber] = newCpu;
                cpuNumber++;
            }
            cluster.addGPUS(GPUS);
            cluster.addCPUS(CPUS);

            int startDate =0;

            for (JsonElement conferenceElement: conferences) {
                JsonObject conferenceObject = conferenceElement.getAsJsonObject();
                String conName = conferenceObject.get("name").getAsString();
                int endDate = conferenceObject.get("date").getAsInt();
                ConfrenceInformation newConference = new ConfrenceInformation(conName, endDate, startDate);
                conferenceList.push(newConference);
                ConferenceService conferenceService = new ConferenceService("conSer1", msgb, newConference);
                Thread t = new Thread(conferenceService);
                t.start();
                startDate = endDate;
            }

            TimeService timeService = new TimeService(tickTime,duration,msgb);
            Thread t = new Thread(timeService);
            t.start();

            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e){}

            GetStarted gs = new GetStarted();
            msgb.sendBroadcast(gs);

            while (!timeService.isTerminated())
            {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e){}
            }

        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }



//        FileWriter writer = new FileWriter("C:\\Users\\Gil\\Downloads\\assigenment2_2_2_2\\example_output.json")

        Statistics statistics = cluster.getStatistics();

        JsonObject objectOut = new JsonObject();
        JsonArray studentsOut = new JsonArray();
        for (Student student : studentList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", student.getName());
            jsonObject.addProperty("department", student.getDepartment());
            jsonObject.addProperty("status", student.getStatus().toString());
            jsonObject.addProperty("publications", student.getPublications());
            jsonObject.addProperty("papersRead", student.getPapersRead());
            JsonArray student_models = new JsonArray();
            for (Model model : student.getFinishedModels()) {
                JsonObject modelObject = new JsonObject();
                modelObject.addProperty("name", model.getName());
                JsonObject data_j = new JsonObject();
                data_j.addProperty("type", model.getData().getType().toString());
                data_j.addProperty("size", model.getData().getSize());
                modelObject.add("data", data_j);
                modelObject.addProperty("status", model.getStatus().toString());
                modelObject.addProperty("result", model.getResults().toString());
                student_models.add(modelObject);

            }
            jsonObject.add("trainedModels", student_models);
            studentsOut.add(jsonObject);
        }
        JsonArray conferenceOut = new JsonArray();
        for (ConfrenceInformation confrenceInformation : conferenceList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", confrenceInformation.getName());
            jsonObject.addProperty("date", confrenceInformation.getDate());
            JsonArray conference_models = new JsonArray();
            for (Model model : confrenceInformation.getTestedModels()) {
                JsonObject modelObject = new JsonObject();
                modelObject.addProperty("name", model.getName());
                JsonObject data_j = new JsonObject();
                data_j.addProperty("type", model.getData().getType().toString());
                data_j.addProperty("size", model.getData().getSize());
                modelObject.add("data", data_j);
                modelObject.addProperty("status", model.getStatus().toString());
                modelObject.addProperty("result", model.getResults().toString());
                conference_models.add(modelObject);
            }
            jsonObject.add("publications", conference_models);
            conferenceOut.add(jsonObject);
        }
        objectOut.add("students",studentsOut);
        objectOut.add("conferences" , conferenceOut);
        objectOut.addProperty("cpuTimeUsed" , statistics.getCpuTimeUnits());
        objectOut.addProperty("gpuTimeUsed" , statistics.getGpuTimeUnits());
        objectOut.addProperty("batchesProcessed" , statistics.getProcessedBatches());
        Gson gson = new Gson();

        try {
//            FileWriter fileWriter = new FileWriter("C:\\Users\\Gil\\Downloads\\assigenment2_2_2_2\\example_output.json");
            FileWriter fileWriter = new FileWriter("output.txt");
            fileWriter.write(gson.toJson(objectOut));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
