package algorithm.CS;

import task.Task;

import java.util.ArrayList;

public class Cuckoo implements Cloneable{
    private ArrayList<Double> x;
    private ArrayList<Task> tasks;
    private double energy;
    private double fitness;
    public Cuckoo(){}
    public Cuckoo(ArrayList<Double> x, ArrayList<Task> tasks){
        this.x = x;
        this.tasks =tasks;
    }
    public Object clone(){
        Cuckoo cuckoo = null;
        try {
            cuckoo = (Cuckoo) super.clone();
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Task> tasks = new ArrayList<>();
            for (int i=0;i<cuckoo.getX().size();i++){
                double value = cuckoo.getX().get(i);
                x.add(value);
                tasks.add((Task) cuckoo.getTasks().get(i).clone());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return cuckoo;
    }
    public ArrayList<Double> getX() {
        return x;
    }

    public void setX(ArrayList<Double> x) {
        this.x = x;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
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
}
