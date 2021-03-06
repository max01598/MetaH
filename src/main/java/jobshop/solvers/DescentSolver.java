package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

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
	
	@Override
    public Result solve(Instance instance, long deadline) {
        GreedySolverEST_LRPT glouton = new GreedySolverEST_LRPT();
        // initialisation
        Result sinit = glouton.solve(instance,deadline);
        int setoile = sinit.schedule.makespan();
        boolean amelioration = true;
        ResourceOrder r = new ResourceOrder(sinit.schedule);
        
        while(amelioration && (deadline - System.currentTimeMillis() > 1)){
            List<Block> blocks = blocksOfCriticalPath(r);
            ResourceOrder voisin = null;
            ResourceOrder meilleurVoisin = r;
            for (Block block : blocks ) {
                List<Swap> swaps = neighboors(block);
                for (Swap swap : swaps ) {
                    voisin=r.copy();
                    swap.applyOn(voisin);
                    if(meilleurVoisin.toSchedule().makespan() > voisin.toSchedule().makespan()){
                        meilleurVoisin=voisin.copy();
                    }
                }
            }
            if(meilleurVoisin.toSchedule().makespan() < setoile) {
                setoile=meilleurVoisin.toSchedule().makespan();
                r = meilleurVoisin;
                amelioration=true;
            }
            else {
                amelioration=false;
            }
        }
        return new Result(instance, r.toSchedule(), Result.ExitCause.Blocked);
    }

	/** Returns a list of all blocks of the critical path. */
	List<Block> blocksOfCriticalPath(ResourceOrder order) {
		ArrayList<Block> blocks = new ArrayList<DescentSolver.Block>();
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
					if(lastTask>firstTask)
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
		ArrayList<Swap> swaps = new ArrayList<DescentSolver.Swap>();
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