package algorithm.PSO;

public class RovBase implements Cloneable{
    private int task;
    private double value;

    public RovBase(){

    }
    public RovBase(int task, double value){
        this.task = task;
        this.value = value;
    }
    public Object clone(){
        RovBase rovBase = null;
        try {
            rovBase = (RovBase) super.clone();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return rovBase;
    }
    public int getTask() {
        return task;
    }

    public void setTask(int task) {
        this.task = task;
    }

    public double getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
