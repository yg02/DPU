package algorithm.PSO;

import task.Task;

import java.util.ArrayList;

public class Particle implements Cloneable{
    private ArrayList<Double> v;
    private ArrayList<Double> x;
    private ArrayList<Task> tasks;
    private ArrayList<Double> pbest;
    private double energy;
    private double fitness;
    private double pfitness;
    public Particle(){}
    public Particle(ArrayList<Double> v, ArrayList<Double> x, ArrayList<Task> tasks){
        this.v = v;
        this.x = x;
        this.tasks = tasks;
    }
    public Object clone(){
        Particle particle = null;
        try {
            particle = (Particle) super.clone();
            ArrayList<Double> x = new ArrayList<>();
            ArrayList<Double> v = new ArrayList<>();
            ArrayList<Task> tasks = new ArrayList<>();
            ArrayList<Double> pbest = new ArrayList<>();
            for (int i=0;i<particle.getV().size();i++){
                double xvalue = particle.getX().get(i), vvalue = particle.getV().get(i);
                Task task = (Task) particle.getTasks().get(i).clone();
                double ptask = particle.getPbest().get(i);
                x.add(xvalue);
                v.add(vvalue);
                pbest.add(ptask);
                tasks.add(task);
            }
            particle.setX(x);
            particle.setV(v);
            particle.setTasks(tasks);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return particle;
    }
    public void setV(ArrayList<Double> v){
        this.v = v;
    }
    public ArrayList<Double> getV(){
        return v;
    }
    public void setX(ArrayList<Double> x){
        this.x = x;
    }
    public ArrayList<Double> getX(){
        return x;
    }
    public void setTasks(ArrayList<Task> tasks){
        this.tasks = tasks;
    }
    public ArrayList<Task> getTasks(){
        return tasks;
    }
    public void setEnergy(double energy){
        this.energy = energy;
    }
    public double getEnergy(){
        return energy;
    }
    public void setFitness(double energy){
        this.energy = energy;
        fitness = 1/energy;
    }
    public double getFitness(){
        return fitness;
    }

    public ArrayList<Double> getPbest() {
        return pbest;
    }

    public void setPbest(ArrayList<Double> pbest) {
        this.pbest = pbest;
    }

    public double getPfitness() {
        return pfitness;
    }

    public void setPfitness(double pfitness) {
        this.pfitness = pfitness;
    }
}
