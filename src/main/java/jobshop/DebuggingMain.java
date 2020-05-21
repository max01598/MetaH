package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.io.IOException;
import java.nio.file.Paths;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            JobNumbers enc = new JobNumbers(instance);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
    
            ResourceOrder enc2 = new ResourceOrder(instance);
            enc2.tasksByMachine[0][0] = new Task(0,0);
            enc2.tasksByMachine[0][1] = new Task(1,1);
            enc2.tasksByMachine[1][0] = new Task(1,0);
            enc2.tasksByMachine[1][1] = new Task(0,1);
            enc2.tasksByMachine[2][0] = new Task(0,2);
            enc2.tasksByMachine[2][1] = new Task(1,2);
     

            System.out.println("\nENCODING: " + enc);
            System.out.println("\nENCODING 2 : \n" + enc2);

            Schedule sched = enc.toSchedule();
            Schedule sched2 = enc2.toSchedule();
            // TODO: make it print something meaningful
            // by implementing the toString() method
            System.out.println("SCHEDULE: " + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan() + "\n");
            
            System.out.println("____________________________________________________________________________");
            System.out.println("SCHEDULE 2: " + sched2);
            System.out.println("VALID: " + sched2.isValid());
            System.out.println("MAKESPAN: " + sched2.makespan());
            
            System.out.println("____________________________________________________________________________");
            ResourceOrder re = new ResourceOrder(instance);
            //re = re.fromSchedule(sched);
            System.out.println(re.toString());
            
            System.out.println("____________________________________________________________________________");
            JobNumbers jN = new JobNumbers(instance);
            //jN.fromSchedule(sched);
            System.out.println(jN);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
    
    
}
