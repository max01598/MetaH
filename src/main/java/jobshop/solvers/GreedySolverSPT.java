package jobshop.solvers;

import java.util.ArrayList;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
//SPT => donne priorité à la tâche la plus courte ;
public class GreedySolverSPT implements Solver {

	ArrayList<Task> set_feasible;//Set of feasible tasks
	ArrayList<Task> set_realized;//Set of realized tasks
	ResourceOrder r;
	
	@Override
	public Result solve(Instance instance, long deadline) {
		r = new ResourceOrder(instance);
		set_feasible = new ArrayList<Task>();
		set_realized = new ArrayList<Task>();
		
		initialisation(instance);
		
		//Boucle
		while(!set_feasible.isEmpty())
		{
			Task task_to_treat = GetTask(instance);
			//System.out.println(task_to_treat);
			PlacerTache(task_to_treat,instance);
			set_realized.add(task_to_treat);
			set_feasible.remove(task_to_treat);
			GetTaskFeasible(instance);
		}
		return new Result(instance, r.toSchedule(), Result.ExitCause.Blocked);
	}

	private Task GetTask(Instance i) {
		Task taskRtrn = set_feasible.get(0);
		for(int t=0;t<set_feasible.size();t++)
		{
			if(i.duration(taskRtrn.job, taskRtrn.task) > i.duration(set_feasible.get(t).job, set_feasible.get(t).task) )
			{
				taskRtrn = set_feasible.get(t);
			}
		}
		return taskRtrn;
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

	private void GetTaskFeasible(Instance i) 
	{
		int nbJob = i.numJobs;
		int nbTask = i.numTasks;
		ArrayList<Task> temp_feasible = set_feasible;
		for(int j=0;j<nbJob;j++)
		{
			for(int t=0;t<nbTask;t++)
			{
				if(!set_realized.contains(new Task(j,t)) && !temp_feasible.contains(new Task(j,t)))
				{
					if(set_realized.contains(new Task(j,t-1)))
					{
						set_feasible.add(new Task(j,t));
						//System.out.println("Tache ajout liste realisable :" + new Task(j,t));
					}
				}
			}
		}
	}
	
	
	private void initialisation(Instance instance)
	{
		int nbJob = instance.numJobs;
		for(int j=0;j<nbJob;j++)
		{
			set_feasible.add(new Task(j,0));
			//System.out.println("Tache ajout liste realisable init :" + new Task(j,0));
		}
	}
}
