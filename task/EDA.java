package task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EDA {

    public static TaskQueue processAlgorithm(int groupSize, int taskNum, int instanceID, int loopNum, DpuSystem sys) throws IOException {

        int[][] taskOrder = new int[groupSize][taskNum];//任务顺序，行种群数，列任务数

        //生成顺序列表
        List<Integer> numbers = new ArrayList<Integer>();
        for (int i = 0; i < taskNum; i++)
            numbers.add(i);

        //洗牌，随机生成初始顺序数组
        for (int i = 0; i < groupSize; i++) {
            Collections.shuffle(numbers);
            for (int j = 0; j < taskNum; j++)
                taskOrder[i] = numbers.stream().mapToInt(Integer::valueOf).toArray();
        }

        //TaskQueue originQueue = new TaskQueue(taskNum, instanceID, sys);//原始任务序列
        TaskQueue bestQueue = new TaskQueue(taskNum, instanceID, sys);//最优解, 初始为原始顺序序列
        //bestQueue.setTimeDone(9999999);
        ArrayList<TaskQueue> solution = new ArrayList<TaskQueue>();

        //生成初始种群，计算适应度值
        for (int i = 0; i < groupSize; i++) {
            TaskQueue tempQueue = new TaskQueue(taskNum, instanceID, sys);
            tempQueue.setQueueID(i);
            solution.add(tempQueue);

            for (int j = 0; j < taskNum; j++) {
                solution.get(i).getTask().set(j,bestQueue.getTask().get(taskOrder[i][j]));
            }
            solution.get(i).getTimeDone(taskNum);
            //print("-------个体【" + i + "】-------", solution.get(i), taskNum);
        }

        //完工时间升序排序
        Collections.sort(solution, new Comparator<TaskQueue>() {
            @Override
            public int compare(TaskQueue t1, TaskQueue t2) {
                return t1.getTimeDone() > t2.getTimeDone() ? 1 : -1;
            }
        });

        //生成初始最优解
        //短作业优先
        Collections.sort(bestQueue.getTask(), new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return t1.getdurationMobile() > t2.getdurationMobile() ? 1 : -1;
            }
        });

        bestQueue.getTimeDone(taskNum);


        //bestQueue = clone(bestQueue, originQueue, taskNum, instanceID, sys);
        //bestQueue = clone(bestQueue, solution.get(0), taskNum, instanceID, sys);
        print("初始最优解：", bestQueue, taskNum);

        //开始迭代
        for (int i = 0; i < loopNum; i++) {
            bestQueue = processEDA(solution, groupSize, taskNum, instanceID, i, bestQueue, sys);
        }

        //System.out.println("全局最优解：");
        //print(bestQueue, taskNum);

        return bestQueue;
    }


    //EDA算法
    public static TaskQueue processEDA(ArrayList<TaskQueue> solution, int groupSize, int taskNum, int instanceID, int loopNum, TaskQueue bestQueue, DpuSystem sys) throws IOException {

        int selectSize = groupSize/2;
        double pro[][] = new double[taskNum][taskNum];//概率矩阵
        double a = 1;//学习率

        DecimalFormat df = new DecimalFormat("#0.00");

        //生成概率矩阵
        for (int i = 0; i < taskNum; i++) {
            for (int j = 0; j < taskNum; j++) {
                int sum =0;
                for (int k = 0; k < selectSize; k++) {
                    if(solution.get(k).getTask().get(j).getTaskID() == i)
                        //if(solution.get(i).getTask().get(j).getTaskID() == solution.get(k).getTask().get(j).getTaskID())
                        sum++;
                }
                double sumD = (double)sum;
                double selectSizeD = (double)selectSize;

                if(loopNum == 0)
                    pro[i][j] = sumD/selectSizeD;
                else
                    pro[i][j] = (1-a)*pro[i][j] + a*sumD/selectSizeD;
                //System.out.print(sum); System.out.print(" ");
                //System.out.print(df.format(pro[i][j]));System.out.print(" ");
            }
            //System.out.println();
        }

//        double selectSizeD = (double)selectSize;
//        for (int j = 0; j < taskNum; j++) {
//            for (int i = 0; i < selectSize; i++) {
//                int tID = solution.get(i).getTask().get(j).getTaskID();
//                pro[tID][j] +=1;
//            }
//
//            for (int i = 0; i < taskNum; i++) {
//                pro[i][j] /= selectSizeD;
//            }
//        }

//        for (int i = 0; i < taskNum; i++) {
//            for (int j = 0; j < taskNum; j++) {
//                    pro[i][j] /= selectSizeD;
//            }
//        }




        solution.clear(); //清空种群

        for (int i = 0; i < groupSize; i++) {

            TaskQueue tempQueue = new TaskQueue(taskNum, instanceID, sys);
            tempQueue.setQueueID(i);
//            for (int k = 0; k < taskNum; k++) {
//                tempQueue.getTask().get(k).setTaskID(-1); //标记该位置是否被重新分配任务
//            }

            TaskQueue bestQueueCopy = new TaskQueue(taskNum, instanceID, sys);
            bestQueueCopy = clone(bestQueueCopy, bestQueue, taskNum, instanceID, sys);

            //映射过程

            //System.out.println("----------生成子代个体【" + i+ "】----------");
            tempQueue = mapOntoSolutionX(pro, i%taskNum, tempQueue, taskNum, bestQueueCopy);

            tempQueue.getTimeDone(taskNum);

            solution.add(tempQueue);

        }

        //完工时间升序排序
        Collections.sort(solution, new Comparator<TaskQueue>() {
            @Override
            public int compare(TaskQueue t1, TaskQueue t2) {
                return t1.getTimeDone() > t2.getTimeDone() ? 1 : -1;
            }
        });

        System.out.println("更新后：");
        for (int i = 0; i < selectSize; i++) {
            System.out.print("个体【" + solution.get(i).getQueueID() + "】:  ");
            print(solution.get(i), taskNum);
        }

        //更新最优解
        if(solution.get(0).getTimeDone() < bestQueue.getTimeDone()){
            bestQueue = clone(bestQueue, solution.get(0),taskNum, instanceID, sys);
            bestQueue.getTimeDone(taskNum);
            //System.out.println("----------【new】-----------------------------------------------------------------------");
        }

        System.out.println("第【" + loopNum + "】轮最优解");
        print(bestQueue, taskNum);

        return  bestQueue;
    }


    //固定概率a继承最优解, 然后概率矩阵继承最优解，最后随机
    public static TaskQueue mapOntoSolutionX(double[][] pro, int row, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print(bestQueue, taskNum);

        double a = 0.86;

        List<Integer> remainLocation = new ArrayList<Integer>();
        for (int i = 0; i < taskNum; i++)
            remainLocation.add(i);

        for (int i = taskNum-1; i >= 0; i--) {

            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = bestQueue.getTask().get(i);

            if(lamda <= a){
                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(i);
                remainLocation.remove(i);
            }
            else {
                lamda = 0 + (double)(Math.random() * ((1 - 0)));
                if(lamda <= pro[row][i]){
                    tempQueue.getTask().set(i, task);
                    bestQueue.getTask().remove(i);
                    remainLocation.remove(i);
                }
            }
        }

        //print("最优解：", bestQueue, bestQueue.getTask().size());
        //print("temp：", tempQueue, taskNum);

        Collections.shuffle(bestQueue.getTask());

        //print("最优解：", bestQueue, bestQueue.getTask().size());
        //System.out.println(bestQueue.getTask().size());

        for (int i = 0; i < remainLocation.size(); i++) {
            tempQueue.getTask().set(remainLocation.get(i), bestQueue.getTask().get(i));
        }

//        if(!bestQueue.getTask().isEmpty()){
//            //System.out.println(bestQueue.getTask().size());
//            int m = 0;
//
//            for (int i = 0; i < taskNum; i++) {
//                //System.out.println("当前任务序号" + tempQueue.getTask().get(i).getTaskID() );
//                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
//                    //System.out.println("m:" + m);
//                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
//                    m++;
//                }
//            }
//        }

        return tempQueue;
    }

    //复制队列 ！！为降低运行时间，只拷贝序列，不拷贝（计算）适应度值
    public static TaskQueue clone(TaskQueue newQueue, TaskQueue oldQueue, int taskNum, int instanceID, DpuSystem sys) throws IOException {

        newQueue = new TaskQueue(taskNum, instanceID, sys);
        for (int i = 0; i < taskNum; i++) {
            newQueue.getTask().set(i,oldQueue.getTask().get(i));
            //System.out.print(newQueue.getTask().get(i).getTaskID());System.out.print(" ");
        }
        newQueue.setQueueID(oldQueue.getQueueID());
        //newQueue.getTimeDone(taskNum);
        return newQueue;
    }


    public static void print(TaskQueue queue, int taskNum){

        for (int i = 0; i < taskNum; i++) {
            System.out.print(queue.getTask().get(i).getTaskID());
            System.out.print(" ");
        }
        System.out.println("【完工时间】:  "+ queue.getTimeDone());
    }

    public static void print(String queueName, TaskQueue queue, int taskNum){

        System.out.print(queueName);
        for (int i = 0; i < taskNum; i++) {
            System.out.print(queue.getTask().get(i).getTaskID());
            System.out.print(" ");
        }
        //queue.getTimeDone(taskNum);
        System.out.println("【完工时间】:  "+ queue.getTimeDone());
    }


    public static void main(String[] args) throws IOException {

        int[] taskNum = {30};
        int instanceNum = 1;
        int loopNum = 10;
        int testNum = 1;
        int groupSize = 10;

//        int[] taskNum = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120};
//        int instanceNum = 10;
//        int loopNum = 1000;
//        int testNum = 5;
//        int groupSize = 30;

        DpuSystem sys = new DpuSystem();

        DecimalFormat format = new DecimalFormat("0.00000");

        String fileName = "results/Results_test.txt";
        File file = new File(fileName);
        //System.out.println(fileName);
        File fileParent = file.getParentFile();//返回的是File类型,可以调用exsit()等方法
        if (!fileParent.exists()) {
            fileParent.mkdirs();// 能创建多级目录
        }
        if (!file.exists())
            file.createNewFile();//有路径才能创建文件
        else{
            file.delete();
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file,true);
        BufferedWriter writer = new BufferedWriter(fileWriter);

        for (int i = 0; i < taskNum.length; i++) {

            for (int j = 0; j < instanceNum; j++) {
                TaskQueue bestQueue = new TaskQueue(taskNum[i], j, sys);

                System.out.println("---------------task【"+ taskNum[i] + "_" + j + "】---------------");

                for (int k = 0; k < testNum; k++) {

                    long startTime=System.currentTimeMillis();   //获取开始时间
                    bestQueue = processAlgorithm(groupSize, taskNum[i], j, loopNum, sys);
                    long endTime=System.currentTimeMillis() - startTime; //获取结束时间

                    String timeDone = format.format(bestQueue.getTimeDone());

                    System.out.println("第【" + k + "】轮最优解" + timeDone);

                    writer.write( taskNum[i] + "\t" + j + "\t" + k + "\t" + timeDone  + "\t" + endTime + "\r\n");
                    writer.flush();

                }
            }

        }

        writer.close();
    }
}