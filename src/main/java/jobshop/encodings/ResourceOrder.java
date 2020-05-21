package jobshop.encodings;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

public class ResourceOrder extends Encoding {
	/*Ici faire comme schedule sauf que la matrice 
	 *va contenir une representation par ordre de passage cf sujet
	 */
	//public final Task[][] resources;
	 // for each machine m, taskByMachine[m] is an array of tasks to be
    // executed on this machine in the same order
    public final Task[][] tasksByMachine;

    // for each machine, indicate on many tasks have been initialized
    public final int[] nextFreeSlot;
    
    /** Creates a new empty resource order. */
    public ResourceOrder(Instance instance)
    {
        super(instance);

        // matrix of null elements (null is the default value of objects)
        tasksByMachine = new Task[instance.numMachines][instance.numJobs];

        // no task scheduled on any machine (0 is the default value)
        nextFreeSlot = new int[instance.numMachines];
    }

	/*public ResourceOrder(Instance pb) {
		super(pb);
		this.resources = new Task[pb.numMachines][pb.numJobs];
	}*/
	
	/*public ResourceOrder(Schedule s) {
		super(s.pb);
		this.resources = new Task[s.pb.numMachines][];
		
		for(int m = 0 ; m<s.pb.numMachines ; m++) {
            final int machine = m;

            // for this machine, find all tasks that are executed on it and sort them by their start time
            this.resources[m] =
                    IntStream.range(0, s.pb.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, s.pb.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> s.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachin
            
        }
	}*/
    
    

    /** Creates a resource order from a schedule. */
    public ResourceOrder(Schedule schedule)
    {
        super(schedule.pb);
        Instance pb = schedule.pb;

        this.tasksByMachine = new Task[pb.numMachines][];
        this.nextFreeSlot = new int[instance.numMachines];

        for(int m = 0 ; m<schedule.pb.numMachines ; m++) {
            final int machine = m;

            // for thi machine, find all tasks that are executed on it and sort them by their start time
            tasksByMachine[m] =
                    IntStream.range(0, pb.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, pb.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> schedule.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachine

            // indicate that all tasks have been initialized for machine m
            nextFreeSlot[m] = instance.numJobs;
        }
    }
    
	/*@Override
	public Schedule toSchedule() {
		ArrayList<Task> taskScheduled = new ArrayList<Task>();
		int[][] startTimes = new int[instance.numJobs][instance.numTasks];
		while(taskScheduled.size()!= (this.instance.numTasks*this.instance.numJobs))
		{
			int machine_act = 0;
			for (Task[] taskByMachine : this.resources) {
				for(int indexOrder=0; indexOrder<this.instance.numJobs;indexOrder++)
				{
					Task t_act = taskByMachine[indexOrder];
					int job = t_act.job;
					int taskNumber = t_act.task;
					//Cas : il est le premier dans l'ordre de la machine et du job
					if(indexOrder==0 && taskNumber==0 && !taskScheduled.contains(t_act))
					{
						//Alors on l'ajoute au task schedulé et son temps de depart est 0
						taskScheduled.add(t_act);
						startTimes[job][taskNumber] = 0;
						//System.out.println("Tache ajouté index et task à 0 : "+t_act);
					}
					else {
						//Cas ou la tache precedente du job est dans la liste et que c'est la premiere tack sur la machine 
						if(isTaskOnTheList(taskScheduled,job,taskNumber-1) && indexOrder == 0 && !taskScheduled.contains(t_act))
						{
							//Alors on l'ajoute au task schedulé et son temps de depart est celui de la tache .... + son temps de travail
							taskScheduled.add(t_act);
							int task_departByJob = startTimes[job][taskNumber-1] + instance.duration(job, taskNumber-1);
							int task_departByMachine = 0;
					        int task_duration = Math.max(task_departByJob,task_departByMachine);
					        startTimes[job][taskNumber] = task_duration;
					        //System.out.println("Tache ajouté avec tache precedente deja effectué index 0 : "+t_act);
						}
						else if(indexOrder != 0)
						{
							//On recupere alors la tache precedente sur la machine
							Task previousTaskOnMachine = this.resources[machine_act][indexOrder-1];
							
							//Si elle est dans la liste ...
							if(isTaskOnTheList(taskScheduled, previousTaskOnMachine.job, previousTaskOnMachine.task) && !taskScheduled.contains(t_act))
							{
								//Alors on l'ajoute au task schedulé et son temps de depart est celui de la tache .... + son temps de travail
								
								if(taskNumber == 0)
								{
									int task_departByMachine = startTimes[previousTaskOnMachine.job][previousTaskOnMachine.task] + instance.duration(previousTaskOnMachine.job, previousTaskOnMachine.task);
									startTimes[job][taskNumber] = task_departByMachine;
									taskScheduled.add(t_act);
									//System.out.println("Tache ajouté avec celle precedente sur la machine effectué et task à 0 : "+t_act);
								}
								else if(isTaskOnTheList(taskScheduled, job, taskNumber-1))
								{
									int task_departByJob = startTimes[job][taskNumber-1] + instance.duration(job, taskNumber-1);
									int task_departByMachine = startTimes[previousTaskOnMachine.job][previousTaskOnMachine.task] + instance.duration(previousTaskOnMachine.job, previousTaskOnMachine.task);
							        int task_duration = Math.max(task_departByJob,task_departByMachine);
							        startTimes[job][taskNumber] = task_duration;
							        taskScheduled.add(t_act);
							        //System.out.println("Tache ajouté : "+t_act);
								}
							}
						}
					}
				}
				machine_act++;
			}
		}
        return new Schedule(instance, startTimes);
	}
	*/
	
	 @Override
	    public Schedule toSchedule() {
	        // indicate for each task that have been scheduled, its start time
	        int [][] startTimes = new int [instance.numJobs][instance.numTasks];

	        // for each job, how many tasks have been scheduled (0 initially)
	        int[] nextToScheduleByJob = new int[instance.numJobs];

	        // for each machine, how many tasks have been scheduled (0 initially)
	        int[] nextToScheduleByMachine = new int[instance.numMachines];

	        // for each machine, earliest time at which the machine can be used
	        int[] releaseTimeOfMachine = new int[instance.numMachines];


	        // loop while there remains a job that has unscheduled tasks
	        while(IntStream.range(0, instance.numJobs).anyMatch(m -> nextToScheduleByJob[m] < instance.numTasks)) {

	            // selects a task that has noun scheduled predecessor on its job and machine :
	            //  - it is the next to be schedule on a machine
	            //  - it is the next to be scheduled on its job
	            // if there is no such task, we have cyclic dependency and the solution is invalid
	            Optional<Task> schedulable =
	                    IntStream.range(0, instance.numMachines) // all machines ...
	                    .filter(m -> nextToScheduleByMachine[m] < instance.numJobs) // ... with unscheduled jobs
	                    .mapToObj(m -> this.tasksByMachine[m][nextToScheduleByMachine[m]]) // tasks that are next to schedule on a machine ...
	                    .filter(task -> task.task == nextToScheduleByJob[task.job])  // ... and on their job
	                    .findFirst(); // select the first one if any

	            if(schedulable.isPresent()) {
	                // we found a schedulable task, lets call it t
	                Task t = schedulable.get();
	                int machine = instance.machine(t.job, t.task);

	                // compute the earliest start time (est) of the task
	                int est = t.task == 0 ? 0 : startTimes[t.job][t.task-1] + instance.duration(t.job, t.task-1);
	                est = Math.max(est, releaseTimeOfMachine[instance.machine(t.job, t.task)]);
	                startTimes[t.job][t.task] = est;

	                // mark the task as scheduled
	                nextToScheduleByJob[t.job]++;
	                nextToScheduleByMachine[machine]++;
	                // increase the release time of the machine
	                releaseTimeOfMachine[machine] = est + instance.duration(t.job, t.task);
	            } else {
	                // no tasks are schedulable, there is no solution for this resource ordering
	                return null;
	            }
	        }
	        // we exited the loop : all tasks have been scheduled successfully
	        return new Schedule(instance, startTimes);
	    }
	
	 
	/*private Boolean isTaskOnTheList(ArrayList<Task> list,int task_job, int task_number)
	{
		int i = 0;
		for (Task task : list) {
			if(task.job==task_job && task.task==task_number)
			{
				return true;
			}
			i++;
		}
		return false;
	}
	
	public String toString()
	{
		String s = "";
		int machine_index = 0;
		for (Task[] tasks : resources) {
			s += "Machine : "+ machine_index + " | ";
			for(int t=0; t<tasks.length;t++)
			{
				s += tasks[t].toString() + " | ";
			}
			s+= "\n";
			machine_index++;
		}
		return s;
	}
	
	public ResourceOrder fromSchedule(Schedule s)
    {
    	ResourceOrder r = new ResourceOrder(s.pb);
    	int nbJob = r.instance.numJobs;
    	int nbMachine = r.instance.numMachines;
    	for(int machine = 0; machine < nbMachine;machine++)
    	{
    		Task[] tasksSameMachine = new Task[nbJob];
    		for(int job=0;job<nbJob;job++)
    		{
    			tasksSameMachine[job] = new Task(job,r.instance.task_with_machine(job, machine));
    		}
    		//On trie le tableau
    		Task[] tasksSorted = SortByTimeStart(s,tasksSameMachine);
    		r.resources[machine] = tasksSorted;
    	}
    	return r;
    }

	private Task[] SortByTimeStart(Schedule s, Task[] taskOnTheMachine) {
		Task[] tasksSorted = new Task[s.pb.numJobs];
		for(int i = 0;i<taskOnTheMachine.length - 1;i++)
		{
			Task t_act = taskOnTheMachine[i];
			Task next_act = taskOnTheMachine[i+1];
			if(s.startTime(t_act.job, t_act.task) > s.startTime(next_act.job, next_act.task))
			{
				Task temp = t_act;
				taskOnTheMachine[i] = next_act;
				taskOnTheMachine[i+1] = temp;
			}
			
		}
		tasksSorted = taskOnTheMachine;
		return tasksSorted;
	}*/
	
	/** Creates an exact copy of this resource order. */
    public ResourceOrder copy() {
        return new ResourceOrder(this.toSchedule());
    }
}
