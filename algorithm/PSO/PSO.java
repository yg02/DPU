package algorithm.PSO;

import task.DpuSystem;
import task.Task;
import task.TaskQueue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

//import com.sun.org.apache.bcel.internal.generic.ALOAD;
//import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
//import sun.misc.VM;

public class PSO {
    private int MAXTRIX = 2000;
    private int pop_size = 30;
    private double c1=2;
    private double c2=2;
    private double w=0.5;
    private double XMAX=4;
    private double XMIN=-4;
    private double VMAX=2;
    private double VMIN=-2;
    private int taskNum;
    //private Algorithm algorithm;
    private TaskQueue taskQueue;
    //private Instance instance;
    private ArrayList<Task> baseTasks;
    private ArrayList<Particle> particles;
    private Particle gbest;
    private DpuSystem sys = new DpuSystem();

    public PSO(){}

    public PSO(TaskQueue taskQueue){
//        this.instance = instance;
//        baseTasks = instance.getBaseTaskList();
//        taskNum = instance.getTaskNum();
//        algorithm = new Algorithm(instance);

        this.taskQueue = taskQueue;
        baseTasks = taskQueue.getTask();
        taskNum = taskQueue.getTaskNum();
    }

    public PSO(TaskQueue taskQueue, double w, double c1, double c2){
//        this.instance = instance;
//        baseTasks = instance.getBaseTaskList();
//        taskNum = instance.getTaskNum();
//        algorithm = new Algorithm(instance);
        this.taskQueue = taskQueue;
        baseTasks = taskQueue.getTask();
        taskNum = taskQueue.getTaskNum();

        this.w=w;
        this.c1 = c1;
        this.c2 = c2;
    }
    public void run() throws IOException {
        initial();
        rovMapping();
        evalueAndGbest();
        int t=0;
        while (t<MAXTRIX){
            update();
            rovMapping();
            evalue();
            updatePbest();
            updateGbest();
            t++;
        }
    }
    /*
    * 初始化
    * */
    public void initial(){
        particles = new ArrayList<>();
        gbest = new Particle();
        for (int i = 0;i<pop_size;i++){
            ArrayList<Double> xs = new ArrayList<>();
            ArrayList<Double> vs = new ArrayList<>();
            ArrayList<Task> tasks = new ArrayList<>();
            for (int j = 0;j<taskNum;j++){
                double x = getRandom(XMIN,XMAX);
                double v = getRandom(VMIN,VMAX);
                xs.add(x);
                vs.add(v);
                tasks.add((Task) baseTasks.get(j).clone());
            }
            Particle particle = new Particle(vs,xs,tasks);
            particles.add(particle);
        }
    }
    /*
    * ROV算子
    * */
    public void rovMapping(){
        for (int i=0;i<pop_size;i++){
            ArrayList<RovBase> rovBases = new ArrayList<>();
            Particle particle = particles.get(i);
            for (int j=0;j<taskNum;j++){
                RovBase rovBase = new RovBase(particle.getTasks().get(j).getTaskID(),particle.getX().get(j));
                rovBases.add(rovBase);
            }
            sort(rovBases);
            ArrayList<Task> tasks = new ArrayList<>();
            for (int j=0;j<taskNum;j++){
                Task task = (Task) baseTasks.get(rovBases.get(j).getTask()).clone();
                tasks.add(task);
            }
            particle.setTasks(tasks);
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
    * 评估并获取全局最优
    * */
    private void evalueAndGbest() throws IOException {
        int index=-1;
        double max=-2;
        for (int i=0;i<pop_size;i++){
            Particle particle = particles.get(i);

            int taskNum = particles.get(i).getTasks().size();
            TaskQueue tempQueue = new TaskQueue(taskNum, 1, sys);
            for (int j = 0; j < taskNum; j++)
                tempQueue.getTask().set(j,particles.get(i).getTasks().get(j));

            tempQueue.getTimeDone(taskNum);

//            algorithm.schemeTwo(particles.get(i).getTasks());
            particle.setFitness(tempQueue.getTimeDone());


            double fit = particle.getFitness();
            if (fit>max){
                max =fit;
                index = i;
            }
            particle.setPbest(particle.getX());
            particle.setPfitness(fit);
        }
        gbest = (Particle) particles.get(index).clone();
    }
    /*
    * 更新每个粒子的速度和位置
    * */
    public void update(){
        for (int i=0;i<pop_size;i++){
            Particle particle = particles.get(i);
            for (int j=0;j<taskNum;j++){
                double v = particle.getV().get(j);
                double x = particle.getX().get(j);
                double px = particle.getPbest().get(j);
                double gx = gbest.getX().get(j);
                v = w*v + c1*Math.random()*(px-x) + c2*Math.random()*(gx-x);
                if (v>VMAX)v=VMAX;
                if (v<VMIN)v=VMIN;
                x += v;
                if (x>XMAX)x=XMAX;
                if (x<XMIN)x=XMIN;
                particle.getX().set(j,x);
                particle.getV().set(j,v);
            }
        }
    }
    /*
    * 评估每个粒子
    * */
    public void evalue() throws IOException {
        for (int i=0;i<pop_size;i++){

            int taskNum = particles.get(i).getTasks().size();
            TaskQueue tempQueue = new TaskQueue(taskNum, 1, sys);
            for (int j = 0; j < taskNum; j++)
                tempQueue.getTask().set(j,particles.get(i).getTasks().get(j));

            tempQueue.getTimeDone(taskNum);

            //algorithm.schemeTwo(particles.get(i).getTasks());
            particles.get(i).setFitness(tempQueue.getTimeDone());
        }
    }
    /*
    * 更新每个粒子的历史最优位置
    * */
    public void updatePbest(){
        for (int i=0;i<pop_size;i++){
            Particle particle = particles.get(i);
            double fit = particle.getFitness();
            if (particle.getPfitness()<fit){
                particle.setPfitness(fit);
                ArrayList<Double> xs = new ArrayList<>();
                for (int j=0;j<taskNum;j++){
                    double x = particle.getX().get(j);
                    xs.add(x);
                }
                particle.setPbest(xs);
            }
        }
    }
    /*
    * 更新粒子的全局最优位置
    * */
    public void updateGbest(){
        int index =-1;
        double max = gbest.getFitness();
        for (int i=0;i<pop_size;i++){
            double fit = particles.get(i).getFitness();
            if (fit>max){
                max = fit;
                index = i;
            }
        }
        if (index!=-1){
            gbest = (Particle) particles.get(index).clone();
        }
    }
    /*
    * 获取随机数
    * */
    public double getRandom(double min, double max){
        return min+Math.random()*(max-min);
    }

    public Particle getGbest(){
        return gbest;
    }

    public static void main(String[] args) throws IOException {

        int[] taskNum = {110, 120};
        int instanceNum = 10; //10
        int testNum = 5; //5

        DpuSystem sys = new DpuSystem();
        DecimalFormat format = new DecimalFormat("0.00000");

        String fileName = "results/Results_PSO_Num110.txt";
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

                    PSO pso = new PSO(taskQueue, 0.5, 2, 2);
                    pso.run();

                    long endTime=System.currentTimeMillis() - startTime;

                    //String timeDone = format.format(taskQueue.getTimeDone());
                    System.out.println("第【" + k + "】轮最优解");
                    System.out.println(pso.getGbest().getEnergy());

                    String timeDone = format.format(pso.getGbest().getEnergy());

                    writer.write( taskNum[i] + "\t" + j + "\t" + k + "\t" +  timeDone + "\t" + endTime + "\r\n");
                    writer.flush();

                }
            }

        }

        writer.close();

//        DecimalFormat format = new DecimalFormat("0.0000");
//        int deviceNum[] = {5, 20, 50}, coreNum[] = {10, 20, 30}, index = 5, round = 5;
//
//        String srcString = "src\\Result\\newpso2000"+"_Results.txt";
//        BufferedWriter writer = new BufferedWriter(new FileWriter(srcString));
//        long projectStart = System.currentTimeMillis();
//        for (int deNum : deviceNum)
//            for (int coNum : coreNum)
//                for (int i = 0; i < index; i++)
//                    for (int j = 0; j < round; j++) {
//                        Instance instance = new Instance(deNum, coNum, 1, i);
//                        instance.initTasks();
//                        long start = System.currentTimeMillis();
//                        PSO pso = new PSO(instance, 0.5, 2, 2);
//                        pso.run();
//                        long end = System.currentTimeMillis();
//                        String result = deNum + " " + coNum + " " + i + " " + j + " " + pso.getGbest().getEnergy() + " " + format.format((end - start) * 1e-3) + "\n";
//                        System.out.print(result);
//                        writer.write(result);
//                        writer.flush();
//                    }
//        long projectEnd = System.currentTimeMillis();
//        String str = "Time: " + format.format((projectEnd - projectStart) * 1e-3) + "\n";
//        writer.write(str);
//        writer.flush();
//        writer.close();

    }
}
