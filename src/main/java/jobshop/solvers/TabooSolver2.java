package jobshop.solvers;

import java.util.ArrayList;
import java.util.List;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class TabooSolver2 implements Solver {

	static class Element {
		final ResourceOrder r;
		final int makespan;
		final Swap s;
		public Element(ResourceOrder res, Swap sw)
		{
			this.r = res;
			this.makespan = this.r.toSchedule().makespan();
			this.s = sw;
		}
		
		public Element(ResourceOrder res, int mk, Swap sw)
		{
			this.r = res;
			this.makespan = mk;
			this.s = sw;
		}
	}
	/** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
	 * This class identifies a block in a ResourceOrder representation.
	 *
	 * Consider the solution in ResourceOrder representation
	 * machine 0 : (0,1) (1,2) (2,2)
	 * machine 1 : (0,2) (2,1) (1,1)
	 * machine 2 : ...
	 *
	 * The block with : machine = 1, firstTask= 0 and lastTask = 1
	 * Represent the task sequence : [(0,2) (2,1)]
	 *
	 * */
	static class Block {
		/** machine on which the block is identified */
		final int machine;
		/** index of the first task of the block */
		final int firstTask;
		/** index of the last task of the block */
		final int lastTask;

		Block(int machine, int firstTask, int lastTask) {
			this.machine = machine;
			this.firstTask = firstTask;
			this.lastTask = lastTask;
		}
	}

	/**
	 * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
	 *
	 * Consider the solution in ResourceOrder representation
	 * machine 0 : (0,1) (1,2) (2,2)
	 * machine 1 : (0,2) (2,1) (1,1)
	 * machine 2 : ...
	 *
	 * The swap with : machine = 1, t1= 0 and t2 = 1
	 * Represent inversion of the two tasks : (0,2) and (2,1)
	 * Applying this swap on the above resource order should result in the following one :
	 * machine 0 : (0,1) (1,2) (2,2)
	 * machine 1 : (2,1) (0,2) (1,1)
	 * machine 2 : ...
	 */
	static class Swap {
		// machine on which to perform the swap
		final int machine;
		// index of one task to be swapped
		final int t1;
		// index of the other task to be swapped
		final int t2;

		Swap(int machine, int t1, int t2) {
			this.machine = machine;
			this.t1 = t1;
			this.t2 = t2;
		}

		/** Apply this swap on the given resource order, transforming it into a new solution. */
		public void applyOn(ResourceOrder order) {
			ResourceOrder r_O_temp = order.copy();
			order.tasksByMachine[this.machine][this.t1] = r_O_temp.tasksByMachine[this.machine][this.t2];
			order.tasksByMachine[this.machine][this.t2] = r_O_temp.tasksByMachine[this.machine][this.t1];
		}
	}
	
	
	public int[][] sTabou;
	private final int maxIter = 100;
	private final int dureeTabou = 10;


	@Override
	public Result solve(Instance instance, long deadline) {
		sTabou = new int[instance.numJobs * instance.numMachines][instance.numJobs * instance.numMachines];
		//Arrays.fill(sTabou, 0);
		GreedySolverEST_LRPT solver = new GreedySolverEST_LRPT();
		Result s_init = solver.solve(instance, deadline);
		ResourceOrder r_star = new ResourceOrder(s_init.schedule);
		int makespan_star = r_star.toSchedule().makespan();
		ResourceOrder r_courant = r_star.copy();
		int k = 0;
		
		while(k <= maxIter && deadline - System.currentTimeMillis() > 1)
		{
			k++;
			List<Block> blocks = blocksOfCriticalPath(r_courant);
			//Meilleur voisin 
			Element best_voisin = null;
			for (Block block : blocks) {
				List<Swap> swaps = neighboors(block);
				for (Swap s : swaps){
					ResourceOrder new_r = r_courant.copy();
					s.applyOn(new_r);
					int t1 = (s.machine * instance.numJobs) + s.t1;
	                int t2 = (s.machine * instance.numJobs) + s.t2;
	                if(best_voisin == null)
	                {
	                	if(this.sTabou[t1][t2] < k)
	                		best_voisin = new Element(new_r, s);
	                }
	                else if(best_voisin != null)
					{
	                	int mk = new_r.toSchedule().makespan();
	                	if(mk < best_voisin.makespan && this.sTabou[t1][t2] < k)
	                		best_voisin = new Element(new_r, mk, s);
					}
				}       
			}
			
			//Ajout du meilleur voisin
			if(best_voisin != null)
			{
				int t1 = (best_voisin.s.machine * instance.numJobs) + best_voisin.s.t1;
	            int t2 = (best_voisin.s.machine * instance.numJobs) + best_voisin.s.t2;
	            //System.out.println(" t1 : "+t1+ " t2 : "+t2);
				this.sTabou[t1][t2] = k + dureeTabou;
				//Affectation r_courant
				r_courant = best_voisin.r.copy();
				
				//Affectation r_star;
				if(best_voisin.makespan <= makespan_star)
				{
					r_star = best_voisin.r.copy();
					makespan_star = best_voisin.makespan;
				}
			}	
		}
		return new Result(instance, r_star.toSchedule(), Result.ExitCause.Blocked);
	}
	
	/** Returns a list of all blocks of the critical path. */
	List<Block> blocksOfCriticalPath(ResourceOrder order) {
		ArrayList<Block> blocks = new ArrayList<Block>();
		List<Task> critPath = order.toSchedule().criticalPath();
		Task startTask = null;
		if(!critPath.isEmpty())
		{
			startTask = critPath.get(0);
			for(int index=1; index<critPath.size();index++)
			{
				Task tempTask = critPath.get(index);
				if(order.instance.machine(tempTask.job,tempTask.task) == order.instance.machine(startTask.job,startTask.task))
				{
					int firstTask = FindIndexOnResource(order,order.instance.machine(tempTask.job,tempTask.task),startTask);
					boolean onTheSameblock = true;
					while(onTheSameblock && index<critPath.size())
					{
						tempTask = critPath.get(index);
						if(order.instance.machine(tempTask.job,tempTask.task) != order.instance.machine(startTask.job,startTask.task))
						{
							onTheSameblock = false;
							tempTask = critPath.get(index-1);
						}
						else
						{
							index++;
						}
					}
					int lastTask = FindIndexOnResource(order,order.instance.machine(tempTask.job,tempTask.task),tempTask);
					if(lastTask > firstTask)
						blocks.add(new Block(order.instance.machine(tempTask.job,tempTask.task),firstTask,lastTask));
				}
				if(index<critPath.size()-1)
					startTask = critPath.get(index);	
			}	
		}
		return blocks;
	}

	private int FindIndexOnResource(ResourceOrder o, int machine, Task task_find) {
		int index = 0;
		for (Task task : o.tasksByMachine[machine] ) {
			if(task.equals(task_find))
				return index;
			index++;
		}
		return -1;
		
	}

	/** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
	List<Swap> neighboors(Block block) {
		ArrayList<Swap> swaps = new ArrayList<Swap>();
		int size = block.lastTask - block.firstTask;
		if(size == 1)
		{
			swaps.add(new Swap(block.machine,block.firstTask,block.lastTask));
		}
		else
		{
			swaps.add(new Swap(block.machine,block.firstTask,block.firstTask +1));
			swaps.add(new Swap(block.machine,block.lastTask-1,block.lastTask));
		}
		return swaps;
	}

}
