package algorithm.FA;

import task.Task;

import java.util.ArrayList;

public class Firefly implements Cloneable{
    private ArrayList<Double> x;
    private ArrayList<Task> taskList;
    private double energy;
    private double fitness;
    public Firefly(){}
    public Firefly(ArrayList<Double> x, ArrayList<Task> taskList){
        this.x = x;
        this.taskList = taskList;
    }

    public Object clone(){
        Firefly firefly = null;
        try {
            firefly = (Firefly) super.clone();
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Task> taskList = new ArrayList<>();
            for (int i=0;i<firefly.getX().size();i++){
                double value = firefly.getX().get(i);
                x.add(value);
                Task task = (Task) firefly.getTaskList().get(i).clone();
                taskList.add(task);
            }
            firefly.setX(x);
            firefly.setTaskList(taskList);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return firefly;
    }
    public ArrayList<Double> getX() {
        return x;
    }
    public void setX(ArrayList<Double> x) {
        this.x = x;
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

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }
}
