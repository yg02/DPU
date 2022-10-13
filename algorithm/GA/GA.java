package algorithm.GA;

import task.DpuSystem;
import task.Task;
import task.TaskQueue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GA {
    private int pop_size=30;
    private int MAXTRIX=2000;
    private double pc=0.1;//交叉率
    private double pm=0.01;//变异率
    //private Instance instance;
    private TaskQueue taskQueue;
    private int taskNum;
    private ArrayList<Task> baseTasks;
    //private Algorithm algorithm;
    private ArrayList<Chromosome> chromosomes;
    private ArrayList<ArrayList<Task>> taskList;
    private Chromosome best;
    private ArrayList<Task> bestTaskList;
    private DpuSystem sys = new DpuSystem();

    public GA(){}

    public GA(TaskQueue taskQueue){
//        this.instance = instance;
//        baseTasks = instance.getBaseTaskList();
//        taskNum = instance.getTaskNum();
//        algorithm = new Algorithm(instance);

        this.taskQueue = taskQueue;
        baseTasks = taskQueue.getTask();
        taskNum = taskQueue.getTaskNum();
    }
    public GA(TaskQueue taskQueue, double pc, double pm){
//        this.instance = instance;
//        baseTasks = instance.getBaseTaskList();
//        taskNum = instance.getTaskNum();
//        algorithm = new Algorithm(instance);
        this.taskQueue = taskQueue;
        baseTasks = taskQueue.getTask();
        taskNum = taskQueue.getTaskNum();

        this.pc = pc;
        this.pm = pm;
    }
    public void run() throws IOException {
        initial();
        int t=0;
        while (t<MAXTRIX){
            cross();
            mutation();
            select();
            t++;
        }
        checkBest();
    }
    /*
    * 初始化
    * */
    public void initial(){
        chromosomes = new ArrayList<>();
        taskList = new ArrayList<>();
        for (int i=0;i<pop_size;i++){
            ArrayList<Task> tasks = new ArrayList<>();
            ArrayList<Integer> ids = new ArrayList<>();
            for (int j=0;j<taskNum;j++){
                ids.add(j);
            }
            taskList.add(tasks);
            Chromosome chromosome = new Chromosome(ids);
            chromosomes.add(chromosome);
        }
    }
    /*
    * 交叉
    * */
    public void cross(){
        ArrayList<Chromosome> crossSet = new ArrayList<>();
        for (Chromosome chromosome : chromosomes){
            if (Math.random()<pc){
                crossSet.add(chromosome);
            }
        }
        for (int i=0;i<crossSet.size();i+=2){
            int j=i+1;
            if (j<crossSet.size()){
                multiCross(crossSet.get(i),crossSet.get(j));
            }
        }
    }
    /*
    * 多点位交叉
    * */
    public void multiCross(Chromosome one, Chromosome two){
        int index1 = (int) (Math.random()*taskNum);
        int index2 = (int)(Math.random()*taskNum);
        int left=0,right=0;
        if (index1>index2){
            right = index1;
            left = index2;
        }else {
            left = index1;
            right = index2;
        }
        ArrayList<Integer> oneTask = one.getTasks(),twoTask = two.getTasks();
        Set<Integer> repetSet = new HashSet<>();
        ArrayList<Integer> oneSet = new ArrayList<>(), twoSet = new ArrayList<>();
        //寻找保留片段中的重复
        for (int i=left;i<=right;i++)
            for (int j = left;j<=right;j++)
                if (oneTask.get(i).equals(twoTask.get(j)))
                    repetSet.add(oneTask.get(i));

        //寻找不重复
        for (int i=left;i<=right;i++){
            int v1 = oneTask.get(i), v2 = twoTask.get(i);
            if (!repetSet.contains(oneTask.get(i)))oneSet.add(v1);
            if (!repetSet.contains(twoTask.get(i)))twoSet.add(v2);
        }
        Collections.shuffle(oneSet);
        Collections.shuffle(twoSet);
        for (int i=left;i<=right;i++){
            int task = oneTask.get(i);
            oneTask.set(i,twoTask.get(i));
            twoTask.set(i,task);
        }
        ArrayList<Integer> oneClash = new ArrayList<>(), twoClash = new ArrayList<>();
        for (int i=0;i<taskNum;i++){
            if (i==left){
                i=right;
                continue;
            }
            if (twoSet.contains(oneTask.get(i)))oneClash.add(i);
            if (oneSet.contains(twoTask.get(i)))twoClash.add(i);
        }
        for (int i=0;i<oneClash.size();i++){
            oneTask.set(oneClash.get(i),oneSet.get(i));
            twoTask.set(twoClash.get(i),twoSet.get(i));
        }
    }
    /*
    * 变异
    * */
    public void mutation(){
        for (Chromosome chromosome : chromosomes){
            if (Math.random()<pm){
                int index1 = (int) (Math.random()*taskNum);
                int index2 = (int) (Math.random()*taskNum);
                ArrayList<Integer> tasks = chromosome.getTasks();
                int task = tasks.get(index1);
                tasks.set(index1,tasks.get(index2));
                tasks.set(index2,task);
            }
        }
    }
    /*
    * 评估
    * */
    public void evalue() throws IOException {
        for (int i=0;i<pop_size;i++){
            Chromosome chromosome = chromosomes.get(i);
            ArrayList<Task> tasks = new ArrayList<>();
            for (int j =0;j<taskNum;j++){
                tasks.add((Task) baseTasks.get(chromosome.getTasks().get(j)).clone());
            }
            taskList.set(i,tasks);

            int taskNum = tasks.size();
            TaskQueue tempQueue = new TaskQueue(taskNum, 1, sys);
            for (int j = 0; j < taskNum; j++)
                tempQueue.getTask().set(j,tasks.get(j));

            tempQueue.getTimeDone(taskNum);
            //algorithm.schemeTwo(tasks);
            chromosome.setFitness(tempQueue.getTimeDone());
        }
    }
    /*
    * 计算每个染色体被选中的概率
    * */
    public void computeProbability(){
        double sum=0;
        for (Chromosome chromosome : chromosomes)
            sum += chromosome.getFitness();
        for (Chromosome chromosome : chromosomes)
            chromosome.setProbability(chromosome.getFitness()/sum);
    }
    /*
    * 轮盘赌
    * */
    public int roulette(){
        double r = Math.random(),p=0;
        int i=0;
        for (;i<pop_size;i++){
            p+=chromosomes.get(i).getProbability();
            if (r<=p)break;
        }
        return i;
    }
    /*
    * 选择
    * */
    public void select() throws IOException {
        evalue();
        computeProbability();
        ArrayList<Chromosome> tmp = new ArrayList<>();
        for (int i=0;i<pop_size;i++){
            ArrayList<Integer> tasks = new ArrayList<>();
            int index = roulette();
            Chromosome t = chromosomes.get(index);
            for (int j=0;j<taskNum;j++){
                tasks.add(t.getTasks().get(j));
            }
            Chromosome chromosome = new Chromosome(tasks);
            tmp.add(chromosome);
        }
        chromosomes = tmp;
    }
    /*
    * 输出最优
    * */
    public void checkBest() throws IOException {
        evalue();
        double max = -2;
        for (int i=0;i<pop_size;i++){
            Chromosome chromosome = chromosomes.get(i);
            double fit = chromosome.getFitness();
            if (fit>max){
                best = chromosome;
                bestTaskList = taskList.get(i);
                max = fit;
            }
        }
    }
    public ArrayList<Task> getBestTaskList(){
        return this.bestTaskList;
    }

    public Chromosome getBest(){
        return best;
    }

    public static void main(String[] args) throws IOException {

        int[] taskNum = {110, 120};
        int instanceNum = 10; //10
        int testNum = 5; //5

        DpuSystem sys = new DpuSystem();
        DecimalFormat format = new DecimalFormat("0.00000");

        String fileName = "results/Results_GA_Num110.txt";
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

                    GA ga = new GA(taskQueue,0.9,0.1);
                    ga.run();

                    long endTime=System.currentTimeMillis() - startTime;

                    //String timeDone = format.format(taskQueue.getTimeDone());
                    System.out.println("第【" + k + "】轮最优解");
                    System.out.println(ga.getBest().getEnergy());

                    String timeDone = format.format(ga.getBest().getEnergy());

                    writer.write( taskNum[i] + "\t" + j + "\t" + k + "\t" +  timeDone + "\t" + endTime + "\r\n");
                    writer.flush();

                }
            }

        }

        writer.close();

//        DecimalFormat format = new DecimalFormat("0.0000");
//        int deviceNum[] = {5,20,50}, coreNum[] = {10,20,30}, index = 5,round = 5;
//            String srcString = "src\\Result\\newga2000"+"_Results.txt";
//            BufferedWriter writer = new BufferedWriter(new FileWriter(srcString));
//            long projectStart = System.currentTimeMillis();
//            for (int deNum : deviceNum)
//                for (int coNum : coreNum)
//                    for (int i = 0; i < index; i++)
//                        for (int j = 0; j < round; j++) {
//                            Instance instance = new Instance(deNum, coNum, 1, i);
//                            instance.initTasks();
//                            long start = System.currentTimeMillis();
//                            GA ga = new GA(instance,0.9,0.1);
//                            ga.run();
//                            long end = System.currentTimeMillis();
//                            String result = deNum + " " + coNum + " " + i + " " + j + " " + ga.getBest().getEnergy() + " " + format.format((end - start) * 1e-3) + "\n";
//                            System.out.print(result);
//                            writer.write(result);
//                            writer.flush();
//                        }
//            long projectEnd = System.currentTimeMillis();
//            String str = "Time: " + format.format((projectEnd - projectStart) * 1e-3) + "\n";
//            writer.write(str);
//            writer.flush();
//            writer.close();

    }
}
