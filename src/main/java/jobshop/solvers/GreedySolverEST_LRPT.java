package jobshop.solvers;

import java.util.Arrays;
import java.util.HashSet;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class GreedySolverEST_LRPT implements Solver {
	
	ResourceOrder r;
    public Result solve(Instance instance, long deadline) {
        HashSet<Task> HashTasksRealisable = new HashSet<Task>();
        HashSet<Task> HashTasksMinimal = new HashSet<Task>();
        r = new ResourceOrder(instance);
        
        int [][] startTimes = new int [instance.numJobs][instance.numTasks];
        for(int i=0;i<instance.numJobs;i++){
            for(int j=0;j<instance.numTasks;j++){
                startTimes[i][j]=0;
            }
        }
        int[] releaseTimeOfMachine = new int[instance.numMachines];
        Arrays.fill(releaseTimeOfMachine,0);
        //init
        int duration=0;
        for(int job = 0 ; job<(instance.numJobs) ; job++) {
            Task t = new Task(job,0);
            HashTasksRealisable.add(t);
        }
        int [] RemainingTime = new int[instance.numJobs ];

        // initialisation Remaining Time
        for( int i = 0 ; i< instance.numJobs  ; i ++) {
            for(int j = 0 ; j < instance.numTasks ; j++) {
                RemainingTime[i] += instance.duration(i,j);
            }
        }
        while(!HashTasksRealisable.isEmpty()){
            HashTasksMinimal.clear();
            int minStartTime =-1;

            for(Task t : HashTasksRealisable) {
                if(minStartTime== -1){
                    minStartTime=t.task == 0 ? 0 : startTimes[t.job][t.task - 1] + instance.duration(t.job, t.task - 1);
                    minStartTime = Math.max(minStartTime, releaseTimeOfMachine[instance.machine(t.job,t.task)]);
                    //System.out.println("min start=  " + minStartTime);
                    HashTasksMinimal.add(t);
                }
                else {
                    //est1
                    int estT = t.task == 0 ? 0 : startTimes[t.job][t.task - 1] + instance.duration(t.job, t.task - 1);
                    estT = Math.max(estT, releaseTimeOfMachine[instance.machine(t.job,t.task)]);

                    if (estT < minStartTime) {
                        HashTasksMinimal.clear();
                        HashTasksMinimal.add(t);
                        minStartTime=estT;
                        //System.out.println("new min " + minStartTime);
                    }
                    else if (estT == minStartTime) {
                        HashTasksMinimal.add(t);
                        //System.out.println("ajout tache");
                    }

                }

            }

            Task taskMin = null;
            for ( Task t : HashTasksMinimal) {
                if(taskMin == null) {
                    taskMin = t;
                    duration = RemainingTime[t.job];
                }
                else if (duration < RemainingTime[t.job] ) {
                    taskMin = t;
                    duration = RemainingTime[t.job];
                }
            }
            startTimes[taskMin.job][taskMin.task]=minStartTime;
            releaseTimeOfMachine[instance.machine(taskMin.job,taskMin.task)]=minStartTime+instance.duration(taskMin.job,taskMin.task);
            PlacerTache(taskMin,instance);
            HashTasksRealisable.remove(taskMin);
            RemainingTime[taskMin.job]-=instance.duration(taskMin.job,taskMin.task);

            if(instance.numTasks-1>taskMin.task){
                HashTasksRealisable.add(new Task(taskMin.job,(taskMin.task +1)));
            }

        }
        return new Result(instance, r.toSchedule(), Result.ExitCause.Blocked);
    }
    
    private void PlacerTache(Task task_treated, Instance i) {
		int machine = i.machine(task_treated.job, task_treated.task);
		boolean placed = false;
		for(int j=0;((j<r.tasksByMachine[machine].length)&&(!placed));j++)
		{
			if(r.tasksByMachine[machine][j]==null)
			{
				r.tasksByMachine[machine][j] = task_treated;
				placed = true;
			}
		}
	}
}