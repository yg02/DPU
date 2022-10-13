package algorithm.CS;

import algorithm.PSO.PSO;
import task.DpuSystem;
import task.Task;
import task.TaskQueue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class CS {
    private int pop_size=30;
    private int MATRIX=2000;
    private double MAX=2;
    private double MIN=0;
    private double pa =0.25;
    private double stepCoefficient = 0.01; //步长系数
    // levy指数和系数
    private double beta = 1.5;
    // levy指数和系数
    private double sigma_u = Math.pow((gamma(1 + beta, 0.00001) * Math.sin(Math.PI * beta / 2)) / (beta * gamma((1 + beta) / 2, 0.00001) * Math.pow(2, (beta - 1) / 2)), (1.0 / beta)); // sigma_u就是标准差
    private double sigma_v = 1.0;
    //private Instance instance;
    private TaskQueue taskQueue;
    private int taskNum;
    //private Algorithm algorithm;
    private ArrayList<Task> baseTaskList;
    private ArrayList<Cuckoo> cuckoos;
    private Cuckoo best;
    private DpuSystem sys = new DpuSystem();

    public CS(){}

    public CS(TaskQueue taskQueue){
//        this.instance = instance;
//        taskNum = instance.getTaskNum();
//        baseTaskList = instance.getBaseTaskList();
//        algorithm = new Algorithm(instance);

        this.taskQueue = taskQueue;
        baseTaskList = taskQueue.getTask();
        taskNum = taskQueue.getTaskNum();
    }
    public CS(TaskQueue taskQueue, double pa, double stepCoefficient, double beta){
//        this.instance = instance;
//        taskNum = instance.getTaskNum();
//        baseTaskList = instance.getBaseTaskList();
//        algorithm = new Algorithm(instance);
        this.taskQueue = taskQueue;
        baseTaskList = taskQueue.getTask();
        taskNum = taskQueue.getTaskNum();

        this.pa = pa;
        this.stepCoefficient = stepCoefficient;
        this.beta = beta;
        sigma_u = Math.pow((gamma(1 + beta, 0.00001) * Math.sin(Math.PI * beta / 2)) / (beta * gamma((1 + beta) / 2, 0.00001) * Math.pow(2, (beta - 1) / 2)), (1.0 / beta));
    }
    public void run() throws IOException {
        initial();
        for (int t=0;t<MATRIX;t++){
            int id = (int)(Math.random()*pop_size);
            Cuckoo cuckoo = levyFly(cuckoos.get(id));
            evalue(cuckoo);
            int jd = (int)(Math.random()*pop_size);
            if (cuckoo.getFitness()>cuckoos.get(jd).getFitness())
                cuckoos.set(jd,cuckoo);
            abandonAndRebirth();
            storeBest();
            sortAndFind();
        }
    }
    /*
    * 初始化
    * */
    public void initial() throws IOException {
        cuckoos = new ArrayList<>();
        for (int i=0;i<pop_size;i++){
            ArrayList<Double> xs = new ArrayList<>();
            ArrayList<Task> tasks = new ArrayList<>();
            for (int j=0;j<taskNum;j++){
                xs.add(getRandom(MIN,MAX));
                Task task = (Task) baseTaskList.get(j).clone();
                tasks.add(task);
            }
            Cuckoo cuckoo = new Cuckoo(xs,tasks);
            evalue(cuckoo);
            cuckoos.add(cuckoo);
        }
    }
    /*
    * levy飞行
    * */
    public Cuckoo levyFly(Cuckoo cuckoo){
        ArrayList<Double> xs = new ArrayList<>();
        for (int i=0;i<taskNum;i++){
            double x = cuckoo.getX().get(i);
            Random random = new Random();
            double u = sigma_u * random.nextGaussian(); // 均值为a,方差为b的高斯分布
            double v = sigma_v * random.nextGaussian(); // 标准正态分布

            double step = u / Math.pow(Math.abs(v), (1.0 / beta)); // 步长
            // 使用mantegna算法来实现
            double stepSize = stepCoefficient * step;
            x += stepSize; // 莱维飞行的基础公式
            if (x>MAX)x=MAX;
            if (x<MIN)x=MIN;
            xs.add(x);
        }
        ArrayList<Task> tasks = new ArrayList<>();
        for (int i=0;i<taskNum;i++){
            Task task = (Task) baseTaskList.get(i).clone();
            tasks.add(task);
        }
        Cuckoo newCuckoo = new Cuckoo(xs,tasks);
        return newCuckoo;
    }
    // 递归实现伽马函数
    public static double gamma(double x, double setAbsRelaErr) {
        // setAbsRelaErr 相对误差绝对值
        // 递归结束条件
        if (x < 0) {
            return gamma(x + 1, setAbsRelaErr) / x;
        }
        if (Math.abs(1.0 - x) < 0.00001) {
            return 1;
        }
        if (Math.abs(0.5 - x) < 0.00001) {
            return Math.sqrt(3.1415926);
        }

        if (x > 1.0) {
            return (x - 1) * gamma(x - 1, setAbsRelaErr);
        }

        double res = 0.0;
        double temp = 1.0;
        double check = 0.0;
        int i = 1;
        while (Math.abs((check - temp) / temp) > setAbsRelaErr) {
            check = temp;
            temp *= i / (x - 1 + i);
            i++;
        }
        res = temp * Math.pow(i, x - 1);
        return res;
    }
    /*
    * 评估
    * */
    public void evalue(Cuckoo cuckoo) throws IOException {
        rovMapping(cuckoo);

        int taskNum = cuckoo.getTasks().size();
        TaskQueue tempQueue = new TaskQueue(taskNum, 1, sys);
        for (int j = 0; j < taskNum; j++)
            tempQueue.getTask().set(j,cuckoo.getTasks().get(j));

        tempQueue.getTimeDone(taskNum);
        //algorithm.schemeTwo(cuckoo.getTasks());
        cuckoo.setFitness(tempQueue.getTimeDone());
    }
    public void evalue() throws IOException {
        rovMapping();
        for (int i=0;i<pop_size;i++){
            Cuckoo cuckoo = cuckoos.get(i);
            if (cuckoo.getFitness()==0){

                int taskNum = cuckoo.getTasks().size();
                TaskQueue tempQueue = new TaskQueue(taskNum, 1, sys);
                for (int j = 0; j < taskNum; j++)
                    tempQueue.getTask().set(j,cuckoo.getTasks().get(j));

                tempQueue.getTimeDone(taskNum);
                //algorithm.schemeTwo(cuckoo.getTasks());
                cuckoo.setFitness(tempQueue.getTimeDone());
            }

        }
    }
    /*
     * ROV算子
     * */
    public void rovMapping(){
        for (int i=0;i<pop_size;i++){
            ArrayList<RovBase> rovBases = new ArrayList<>();
            Cuckoo cuckoo = cuckoos.get(i);
            for (int j=0;j<taskNum;j++){
                RovBase rovBase = new RovBase(cuckoo.getTasks().get(j).getTaskID(),cuckoo.getX().get(j));
                rovBases.add(rovBase);
            }
            sort(rovBases);
            ArrayList<Task> tasks = new ArrayList<>();
            for (int j=0;j<taskNum;j++){
                Task task = (Task) baseTaskList.get(rovBases.get(j).getTask()).clone();
                tasks.add(task);
            }
            cuckoo.setTasks(tasks);
        }
    }
    public void rovMapping(Cuckoo cuckoo){
            ArrayList<RovBase> rovBases = new ArrayList<>();
            for (int j=0;j<taskNum;j++){
                RovBase rovBase = new RovBase(cuckoo.getTasks().get(j).getTaskID(),cuckoo.getX().get(j));
                rovBases.add(rovBase);
            }
            sort(rovBases);
            ArrayList<Task> tasks = new ArrayList<>();
            for (int j=0;j<taskNum;j++){
                Task task = (Task) baseTaskList.get(rovBases.get(j).getTask()).clone();
                tasks.add(task);
            }
            cuckoo.setTasks(tasks);
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
    private void abandonAndRebirth(){
        for (int i=0;i<pop_size;i++){
            Cuckoo cuckoo = cuckoos.get(i);
            if (Math.random()<pa){
                int jc = (int)(Math.random()*pop_size);
                int kc = (int)(Math.random()*pop_size);
                Cuckoo cJ = cuckoos.get(jc);
                Cuckoo cK = cuckoos.get(kc);
                ArrayList<Double> xs = new ArrayList<>();
                ArrayList<Task> tasks = new ArrayList<>();
                for (int j=0;j<taskNum;j++){
                    double x = cuckoo.getX().get(j);
                    x+= cJ.getX().get(j)- cK.getX().get(j);
                    if (x>MAX)x=MAX;
                    if (x<MIN)x=MIN;
                    xs.add(x);
                    Task task = (Task) baseTaskList.get(cuckoo.getTasks().get(j).getTaskID()).clone();
                    tasks.add(task);
                }
                Cuckoo cuckoo1 = new Cuckoo(xs,tasks);
                cuckoos.set(i,cuckoo1);
            }
        }
    }
    /*
    * 保留最优解
    * */
    public void storeBest() throws IOException {
        evalue();
        int index = -1;
        double max=-2;
        for (int i=0;i<pop_size;i++){
            Cuckoo cuckoo = cuckoos.get(i);
            if (cuckoo.getFitness()>max){
                max = cuckoo.getFitness();
                index =i;
            }
        }
        best = (Cuckoo) cuckoos.get(index).clone();
    }
    /*
    * 排序找出当前最佳
    * */
    public void sortAndFind(){
        for (int i=0;i<pop_size-1;i++){
            for (int j=0;j<pop_size-i-1;j++){
                if (cuckoos.get(j).getFitness()<cuckoos.get(j+1).getFitness()){
                    Cuckoo t = cuckoos.get(j);
                    cuckoos.set(j,cuckoos.get(j+1));
                    cuckoos.set(j+1,t);
                }
            }
        }
    }

    public double getRandom(double min, double max){
        return min+Math.random()*(max-min);
    }

    public Cuckoo getBest(){
        return best;
    }

    public static void main(String[] args) throws IOException {

        int[] taskNum = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        int instanceNum = 10; //10
        int testNum = 5; //5

        DpuSystem sys = new DpuSystem();
        DecimalFormat format = new DecimalFormat("0.00000");

        String fileName = "results/Results_CS_.txt";
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

                    CS cs = new CS(taskQueue, 0.25, 0.01,1.5);
                    cs.run();

                    long endTime=System.currentTimeMillis() - startTime;

                    //String timeDone = format.format(taskQueue.getTimeDone());
                    System.out.println("第【" + k + "】轮最优解");
                    System.out.println(cs.getBest().getEnergy());

                    String timeDone = format.format(cs.getBest().getEnergy());

                    writer.write( taskNum[i] + "\t" + j + "\t" + k + "\t" +  timeDone + "\t" + endTime + "\r\n");
                    writer.flush();

                }
            }

        }

        writer.close();

//        DecimalFormat format = new DecimalFormat("0.0000");
//        int deviceNum[] = {5,20,50}, coreNum[] = {10,20,30}, index = 5,round = 5;
//        //double[] pa = {0.45,0.55,0.65,0.75};
//       // double[] pa = {0.15,0.25,0.35,0.45,0.55};
//        //double[] stepCoefficient = {0.001,0.01,0.02,0.03,0.04,0.05};
//        //double[] beta = {1,1.5,2,2.5,3,3.5};
//        //for (int di=0;di<beta.length;di++) {
//            String srcString = "src\\Result\\newcs2000"+"_Results.txt";
//            BufferedWriter writer = new BufferedWriter(new FileWriter(srcString));
//            long projectStart = System.currentTimeMillis();
//            for (int deNum : deviceNum)
//                for (int coNum : coreNum)
//                    for (int i = 0; i < index; i++)
//                        for (int j = 0; j < round; j++) {
//                            Instance instance = new Instance(deNum, coNum, 1, i);
//                            instance.initTasks();
//                            long start = System.currentTimeMillis();
//                            CS cs = new CS(instance, 0.25, 0.01,1.5);
//                            cs.run();
//                            long end = System.currentTimeMillis();
//                            String result = deNum + " " + coNum + " " + i + " " + j + " " + cs.getBest().getEnergy() + " " + format.format((end - start) * 1e-3) + "\n";
//                            System.out.print(result);
//                            writer.write(result);
//                            writer.flush();
//                        }
//            long projectEnd = System.currentTimeMillis();
//            String str = "Time: " + format.format((projectEnd - projectStart) * 1e-3) + "\n";
//            writer.write(str);
//            writer.flush();
//            writer.close();
//        //}
//    }
    }
}
