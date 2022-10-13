package algorithm.GA;

import java.util.ArrayList;

public class Chromosome implements Cloneable{
    private ArrayList<Integer> tasks;
    private double energy;
    private double fitness;
    private double probability;
    public Chromosome(){}
    public Chromosome(ArrayList<Integer> tasks){
        this.tasks = tasks;
    }
    public Object clone(){
        Chromosome chromosome = null;
        try {
            chromosome = (Chromosome) super.clone();
            ArrayList<Integer> tasks = new ArrayList<>();
            for (int i=0;i<tasks.size();i++){
                int t = chromosome.getTasks().get(i);
                tasks.add(t);
            }
            chromosome.setTasks(tasks);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return chromosome;
    }
    public ArrayList<Integer> getTasks() {
        return tasks;
    }
    public void setTasks(ArrayList<Integer> tasks) {
        this.tasks = tasks;
    }
    public double getEnergy() {
        return energy;
    }
    public void setEnergy(double energy) {
        this.energy = energy;
    }
    public double getFitness() {
        return fitness;
    }
    public void setFitness(double energy) {
        this.energy = energy;
        this.fitness = 1/energy;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
