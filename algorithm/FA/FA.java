package algorithm.FA;

import task.DpuSystem;
import task.Task;
import task.TaskQueue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class FA {

    private int pop_size=30;
    private int MATRIX=2000;
    private double MAX=2;
    private double MIN=-2;
    private double alpha=0.8;
   // private double lamda=1.5;
    private double beta = 1;
    private double gamma = 2;

    private TaskQueue taskQueue;
    //private Instance instance;
    private ArrayList<Task> baseTasks;
    //private Algorithm algorithm;
    private int taskNum;
    private ArrayList<Firefly> fireflies;
    private Firefly best;
    private DpuSystem sys = new DpuSystem();

    public FA(){}

    public FA(TaskQueue taskQueue){
        this.taskQueue = taskQueue;
        baseTasks = taskQueue.getTask();
        taskNum = taskQueue.getTaskNum();
        //algorithm = new Algorithm(instance);
    }

    public FA(TaskQueue taskQueue, double alpha, double beta, double gamma){
        this.taskQueue = taskQueue;
        baseTasks = taskQueue.getTask();
        taskNum = taskQueue.getTaskNum();
        //algorithm = new Algorithm(instance);
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }
    public void run() throws IOException {
        initial();
        for (int t=0;t<MATRIX;t++){
            update();
            updateOptimal();
        }

    }
    /*
    * 初始化
    * */
    public void initial() throws IOException {
        fireflies = new ArrayList<>();
        for (int i=0;i<pop_size;i++){
            ArrayList<Task> tasks = new ArrayList<>();
            ArrayList<Double> xs = new ArrayList<>();
            for (int j=0;j<taskNum;j++){
                Task task = (Task) baseTasks.get(j).clone();
                tasks.add(task);
                double x = getRandom(MIN,MAX);
                xs.add(x);
            }
            Firefly firefly = new Firefly(xs,tasks);
            fireflies.add(firefly);
        }
        evalue();
    }
    /*
    * 更新萤火虫的位置
    * */
    public void update() throws IOException {
        evalue();
        sortAndFindBest();
        for (int i=1;i<pop_size;i++){
            Firefly firefly = fireflies.get(i);
            double r = 0;
            for (int j=0;j<taskNum;j++){
                double value = firefly.getX().get(j)-best.getX().get(j);
                r+=value*value;
            }
            r=Math.sqrt(r);
            ArrayList<Double> xs = new ArrayList<>();
            ArrayList<Task> tasks = new ArrayList<>();
            for (int j=0;j<taskNum;j++){
                double x = firefly.getX().get(j);
                x+=beta*Math.pow(Math.E,-gamma*r)*(best.getX().get(j)-x)+alpha*(Math.random()-0.5);
                if (x>MAX)x=MAX;
                if (x<MIN)x=MIN;
                xs.add(x);
                tasks.add((Task) baseTasks.get(firefly.getTaskList().get(j).getTaskID()).clone());
            }
            Firefly newfly = new Firefly(xs,tasks);
            fireflies.set(i,firefly);
        }
    }
    /*
    * 更新萤火虫的位置
    * */
    public void updateOptimal(){
        Firefly firefly = fireflies.get(0);
        ArrayList<Double> xs = new ArrayList<>();
        ArrayList<Task> tasks = new ArrayList<>();
        int index = (int)(Math.random()*pop_size);
        for (int i=0;i<taskNum;i++){
            double value = firefly.getX().get(i);
            double x = value+Math.random()*(fireflies.get(index).getX().get(i)-value);
            if (x>MAX)x=MAX;
            if (x<MIN)x=MIN;
            xs.add(x);
            tasks.add((Task) baseTasks.get(firefly.getTaskList().get(i).getTaskID()).clone());
        }
        Firefly newFly = new Firefly(xs,tasks);
        fireflies.set(0,newFly);
    }
    /*
     * 计算亮度（评估）
     * */
    public void evalue() throws IOException {
        rovMapping();
        for (int i=0;i<pop_size;i++){

            int taskNum = fireflies.get(i).getTaskList().size();
            TaskQueue tempQueue = new TaskQueue(taskNum, 1, sys);
            for (int j = 0; j < taskNum; j++)
                tempQueue.getTask().set(j,fireflies.get(i).getTaskList().get(j));

            tempQueue.getTimeDone(taskNum);
            //algorithm.schemeTwo(fireflies.get(i).getTaskList());
            fireflies.get(i).setFitness(tempQueue.getTimeDone());
        }
    }
    /*
     * ROV算子
     * */
    public void rovMapping(){
        for (int i=0;i<pop_size;i++){
            ArrayList<RovBase> rovBases = new ArrayList<>();
            Firefly firefly = fireflies.get(i);
            for (int j=0;j<taskNum;j++){
                RovBase rovBase = new RovBase(firefly.getTaskList().get(j).getTaskID(),firefly.getX().get(j));
                rovBases.add(rovBase);
            }
            sort(rovBases);
            ArrayList<Task> tasks = new ArrayList<>();
            for (int j=0;j<taskNum;j++){
                Task task = (Task) baseTasks.get(rovBases.get(j).getTask()).clone();
                tasks.add(task);
            }
            firefly.setTaskList(tasks);
        }
    }
    private void sort(ArrayList<RovBase> bases){
        for (int i=0;i<bases.size()-1;i++)
            for (int j=0;j<bases.size()-i-1;j++)
                if (bases.get(j).getValue()>bases.get(j+1).getValue()){
                    RovBase t = (RovBase) bases.get(j).clone();
                    bases.set(j,(RovBase) bases.get(j+1).clone());
                    bases.set(j+1,t);
                }
    }
    /*
     * 寻找最优
     * */
    public void sortAndFindBest(){
        for (int i=0;i<pop_size-1;i++){
            for (int j=0;j<pop_size-i-1;j++){
                if (fireflies.get(j).getFitness()<fireflies.get(j+1).getFitness()){
                    Firefly tmp = fireflies.get(j);
                    fireflies.set(j,fireflies.get(j+1));
                    fireflies.set(j+1,tmp);
                }
            }
        }
        best = (Firefly) fireflies.get(0).clone();
    }

    public double getRandom(double min, double max){
        return min+Math.random()*(max-min);
    }

    public Firefly getBest(){
        return best;
    }

    public static void main(String[] args) throws IOException {

        int[] taskNum = {110, 120};
        int instanceNum = 10; //10
        int testNum = 5; //5

        DpuSystem sys = new DpuSystem();
        DecimalFormat format = new DecimalFormat("0.00000");

        String fileName = "results/Results_FA_Num110.txt";
        File file = new File(fileName);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }
        if (!file.exists())
            file.createNewFile();
        else{
            file.delete();
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file,true);
        BufferedWriter writer = new BufferedWriter(fileWriter);

        for (int i = 0; i < taskNum.length; i++) {

            for (int j = 0; j < instanceNum; j++) {
                TaskQueue taskQueue = new TaskQueue(taskNum[i], j, sys);

                System.out.println("---------------task【"+ taskNum[i] + "_" + j + "】---------------");

                for (int k = 0; k < testNum; k++) {

                    long startTime=System.currentTimeMillis();

                    FA fa = new FA(taskQueue,0.7,5,3);
                    fa.run();

                    long endTime=System.currentTimeMillis() - startTime;

                    //String timeDone = format.format(taskQueue.getTimeDone());
                    System.out.println("第【" + k + "】轮最优解");
                    System.out.println(fa.getBest().getEnergy());

                    String timeDone = format.format(fa.getBest().getEnergy());

                    writer.write( taskNum[i] + "\t" + j + "\t" + k + "\t" +  timeDone + "\t" + endTime + "\r\n");
                    writer.flush();

                }
            }

        }

        writer.close();

//        DecimalFormat format = new DecimalFormat("0.0000");
//        int deviceNum[] = {5,20,50}, coreNum[] = {10,20,30}, index = 5,round = 5;
//        String srcString = "src\\Result\\newfa20001"+"_Results.txt";
//        BufferedWriter writer = new BufferedWriter(new FileWriter(srcString));
//        long projectStart = System.currentTimeMillis();
//        for (int deNum : deviceNum)
//            for (int coNum : coreNum)
//                for (int i = 0; i < index; i++)
//                    for (int j = 0; j < round; j++) {
//                        TaskQueue taskQueue = new TaskQueue(deNum, coNum, 1, i);
//                        taskQueue.getTimeDone();
//                        long start = System.currentTimeMillis();
//                        FA fa = new FA(taskQueue,0.7,5,3);
//                        fa.run();
//                        long end = System.currentTimeMillis();
//                        String result = deNum + " " + coNum + " " + i + " " + j + " " + fa.getBest().getEnergy() + " " + format.format((end - start) * 1e-3) + "\n";
//                        System.out.print(result);
//                        writer.write(result);
//                        writer.flush();
//                    }
//            long projectEnd = System.currentTimeMillis();
//            String str = "Time: " + format.format((projectEnd - projectStart) * 1e-3) + "\n";
//            writer.write(str);
//            writer.flush();
//            writer.close();
     //   }
    }
}
