package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.util.*;

public class GreedySolverLRPT implements Solver {

	ResourceOrder r;
	@Override
	public Result solve(Instance instance, long deadline) {
        ArrayList<Task> t_Realisable = new ArrayList<Task>(); 
        r = new ResourceOrder(instance);

        int[] releaseTimeByJob = new int[instance.numJobs];
        Arrays.fill(releaseTimeByJob,0);
       
        // initialisation
        for( int j = 0 ; j < instance.numJobs  ; j ++) 
        {
            for(int t = 0 ; t < instance.numTasks ; t++) 
            {
            	releaseTimeByJob[j] += instance.duration(j,t);
            }
        }
        
        for( int j = 0 ; j < instance.numJobs  ; j ++) 
        {
            t_Realisable.add(new Task(j, 0));
        }
        

        while(!t_Realisable.isEmpty()){
            Task t_toTreat = t_Realisable.get(0);
            int size = t_Realisable.size();
            for(int i=1;i<size;i++)
            {
            	//System.out.println(releaseTimeByJob[t_Realisable.get(i).job]);
            	int t1 = releaseTimeByJob[t_toTreat.job] - instance.duration(t_toTreat.job,t_toTreat.task);
            	int t2 = releaseTimeByJob[t_Realisable.get(i).job] - instance.duration(t_Realisable.get(i).job,t_Realisable.get(i).task);
            	
            	if (t1 < t2) 
            	{
            		t_toTreat=t_Realisable.get(i);
                }
            }
            releaseTimeByJob[t_toTreat.job] -= instance.duration(t_toTreat.job,t_toTreat.task);
            PlacerTache(t_toTreat,instance);
            t_Realisable.remove(t_toTreat);
            if(t_toTreat.task < instance.numTasks-1){
            	t_Realisable.add(new Task(
            						t_toTreat.job,
            						t_toTreat.task+1
            							));
            }
            //System.out.println(t_toTreat);
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