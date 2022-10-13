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

public class EDAall {

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


//        System.out.println("顺序数组： ");
//        for (int i = 0; i < groupSize; i++) {
//        	System.out.print("{");
//            for (int j = 0; j < taskNum; j++) {
//                System.out.print(taskOrder[i][j]);
//                if(j != taskNum - 1)
//                	System.out.print(", ");
//            }
//            System.out.print("},");
//            System.out.println();
//        }

        TaskQueue originQueue = new TaskQueue(taskNum, instanceID, sys);//原始任务序列
        TaskQueue bestQueue = new TaskQueue(taskNum, instanceID, sys);//最优解
        //TaskQueue secondQueue = new TaskQueue(taskNum, instanceID, sys);//次优解
        bestQueue.setTimeDone(9999999);
        ArrayList<TaskQueue> solution = new ArrayList<TaskQueue>();

        //生成初始种群，计算适应度值
        for (int i = 0; i < groupSize; i++) {
            TaskQueue tempQueue = new TaskQueue(taskNum, instanceID, sys);
            tempQueue.setQueueID(i);
            solution.add(tempQueue);

            //System.out.println("-------个体【" + i + "】-------");
            for (int j = 0; j < taskNum; j++) {
                solution.get(i).getTask().set(j,originQueue.getTask().get(taskOrder[i][j]));
                //System.out.print(solution.get(i).getTask().get(j).getTaskID());
                //System.out.print(" ");
            }
            solution.get(i).getTimeDone(taskNum);
            //System.out.println("【完工时间】:  " + solution.get(i).getTimeDone());
        }

        //完工时间升序排序
        Collections.sort(solution, new Comparator<TaskQueue>() {
            @Override
            public int compare(TaskQueue t1, TaskQueue t2) {
                return t1.getTimeDone() > t2.getTimeDone() ? 1 : -1;
            }
        });

        //生成初始最优解
        //长作业优先
        Collections.sort(originQueue.getTask(), new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return t1.getdurationTrans() < t2.getdurationTrans() ? 1 : -1;
            }
        });

        originQueue.getTimeDone(taskNum);

//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(originQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        for (int i = 0; i < taskNum; i++) {
//            System.out.println(originQueue.getTask().get(i).getdurationTrans());
//        }
//        System.out.println(originQueue.getTimeDone());


        bestQueue = clone(bestQueue, originQueue,taskNum, instanceID, sys);
        //secondQueue = clone(secondQueue, solution.get(0),taskNum, instanceID, sys);

//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        for (int i = 0; i < taskNum; i++) {
//            System.out.println(bestQueue.getTask().get(i).getdurationTrans());
//        }

//        ArrayList<TaskQueue> bestTwoQueues = new ArrayList<TaskQueue>();
//        bestTwoQueues.add(bestQueue);
//        bestTwoQueues.add(secondQueue);

//        System.out.println("初始最优解：");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("【完工时间】:  "+ bestQueue.getTimeDone());
//
//        System.out.println("初始次优解：");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(secondQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("【完工时间】:  "+ secondQueue.getTimeDone());

        //开始迭代
        for (int i = 0; i < loopNum; i++) {
            //bestQueue = processEDA(solution, groupSize, taskNum, instanceID, i, bestQueue, secondQueue, sys);
            bestQueue = processEDA(solution, groupSize, taskNum, instanceID, i, bestQueue, sys);
        }

        //System.out.println("最优解");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("【完工时间】:  "+ bestQueue.getTimeDone());
        print(bestQueue, taskNum);

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

        solution.clear(); //清空种群

//        for (int j = 0; j < 2; j++) {  //两轮采样，生成新种群数量为N
//            for (int i = 0; i < selectSize; i++)

        for (int i = 0; i < groupSize; i++) {

            TaskQueue tempQueue = new TaskQueue(taskNum, instanceID, sys);
            tempQueue.setQueueID(i);
            for (int k = 0; k < taskNum; k++) {
                tempQueue.getTask().get(k).setTaskID(-1); //标记该位置是否被重新分配任务
            }

            TaskQueue bestQueueCopy = new TaskQueue(taskNum, instanceID, sys);
            bestQueueCopy = clone(bestQueueCopy, bestQueue, taskNum, instanceID, sys);

            //映射过程
            //tempQueue = mapOntoSolutionII(i % taskNum, pro, tempQueue, taskNum, bestQueueCopy);
            //tempQueue = mapOntoSolutionIII(pro[i % taskNum], tempQueue, taskNum, bestQueueCopy);

            //TaskQueue secondQueueCopy = new TaskQueue(taskNum, instanceID, sys);
            //secondQueueCopy = clone(secondQueueCopy, secondQueue, taskNum, instanceID, sys);
            //tempQueue = mapOntoSolutionIV(pro[i % taskNum], tempQueue, taskNum, bestQueueCopy, secondQueueCopy, sys);

            double proCopy[][] = new double[taskNum][taskNum];//概率矩阵;
            for (int k = 0; k < taskNum; k++) {
                for (int l = 0; l < taskNum; l++) {
                    proCopy[k][l] = pro[k][l];
                }
            }

            tempQueue = mapOntoSolution(proCopy, tempQueue, taskNum, bestQueueCopy);
//            tempQueue = mapOntoSolutionIX(proCopy, tempQueue,taskNum, bestQueueCopy);
//            tempQueue = mapOntoSolutionVI(proCopy, tempQueue, taskNum, bestQueueCopy);

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

        //System.out.println();

        //更新最优解
        if(solution.get(0).getTimeDone() < bestQueue.getTimeDone()){
            //secondQueue = clone(secondQueue, bestQueue, taskNum, instanceID, sys);
            bestQueue = clone(bestQueue, solution.get(0),taskNum, instanceID, sys);
            //System.out.println("----------【new】-----------------------------------------------------------------------");
        }

//        DecimalFormat df = new DecimalFormat("#.0000");
//        System.out.println(df.format(bestQueue.getTimeDone()));

//        System.out.println("第【" + loopNum + "】轮最优解");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("【完工时间】:  "+ bestQueue.getTimeDone());

//        System.out.println("次优解：");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(secondQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("【完工时间】:  "+ secondQueue.getTimeDone());


//        System.out.println("更新后：");
//        for (int i = 0; i < selectSize; i++) {
//            System.out.print("个体【" + solution.get(i).getQueueID() + "】:  ");
//            for (int j = 0; j < taskNum; j++) {
//                System.out.print(solution.get(i).getTask().get(j).getTaskID());
//                System.out.print(" ");
//            }
//            System.out.println("【完工时间】:  " + solution.get(i).getTimeDone());
//    }

//        ArrayList<TaskQueue> bestTwoQueues = new ArrayList<TaskQueue>();
//        bestTwoQueues.add(bestQueue);
//        bestTwoQueues.add(secondQueue);

        return  bestQueue;

    }

    //全局轮盘赌 EDA_tradition
    public static TaskQueue mapOntoSolution(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print("temp：", tempQueue, taskNum);

        //System.out.println("------------------------New Round ------------------------");

        DecimalFormat df = new DecimalFormat("#0.00");
        //System.out.println(df.format());

//        System.out.println("【start】");
//        for (int i = 0; i < taskNum; i++) {
//            for (int j = 0; j < taskNum; j++) {
//                System.out.print(df.format(pro[i][j])); System.out.print(" ");
//            }
//            System.out.println();
//        }

        //ID升序排序
        Collections.sort(bestQueue.getTask(), new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return t1.getTaskID() > t2.getTaskID() ? 1 : -1;
            }
        });

        //print("最优解：", bestQueue, bestQueue.getTask().size());

        double[] sumSet = new  double[taskNum];
        for (int i = 0; i < taskNum; i++)
            sumSet[i] = 1.0;

        //某列概率可能会被全部清零
        int[] flag = new  int[taskNum];
        for (int i = 0; i < taskNum; i++)
            flag[i] = 0;

        int remainNum = taskNum;

        //轮盘赌
        for (int i = 0; i < taskNum; i++) {

//            for (int j = 0; j < taskNum; j++) {
//                System.out.print(pro[j][i]);
//                System.out.print(" ");
//            }
//            System.out.println();

            //System.out.println("【" + i + "】");

            double sum = 0.0;
            double r = Math.random()*1;
            //System.out.print(r);
            int tempID = -1;
            for (int j = 0; j < taskNum; j++) {
                sum += pro[j][i];
                if(sum > r){
                    //System.out.println(" < " + sum + " 任务" + j);
                    tempQueue.getTask().set(i,bestQueue.getTask().get(j));
                    tempID = j;
                    remainNum --;
                    flag[j] = 1;
                    break;
                }
            }
            //System.out.println("tempID:" + tempID);

            for (int j = 0; j < taskNum; j++) {
                sumSet[j] -= pro[tempID][j];
                pro[tempID][j] = 0;
            }

//            System.out.println("【during】");
//            for (int k = 0; k < taskNum; k++) {
//                for (int j = 0; j < taskNum; j++) {
//                    System.out.print(df.format(pro[k][j])); System.out.print(" ");
//                }
//                System.out.println();
//            }

//            for (int j = 0; j < taskNum; j++) {
//                System.out.println("j: " + sumSet[j]);
//            }

            for (int j = 0; j < taskNum; j++) {
                sum = 0.0;
                for (int k = 0; k < taskNum; k++) {
                    if(pro[k][j] != 0){
                        pro[k][j] = pro[k][j] / sumSet[j];
                        sum += pro[k][j];
                        //System.out.println(pro[k][j]);
                    }
                }
                sumSet[j] = 1;
                //System.out.println("重置sum:" + sum);
                if(sum == 0){
                    for (int k = 0; k < taskNum; k++) {
                        if(flag[k] != 1){
                            pro[k][j] = 1.0/(double)remainNum;
                        }
                    }
                }
            }

//            System.out.println("【final】");
//            for (int k = 0; k < taskNum; k++) {
//                for (int j = 0; j < taskNum; j++) {
//                    System.out.print(df.format(pro[k][j])); System.out.print(" ");
//                }
//                System.out.println();
//            }

            //print("temp：", tempQueue, taskNum);

        }

        return tempQueue;
    }


    //概率矩阵继承最优解，然后随机
    public static TaskQueue mapOntoSolutionI(double[] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        int l = bestQueue.getTask().size();
//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();


        for (int i = taskNum-1; i >= 0; i--) {

            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = bestQueue.getTask().get(i);
            if(lamda <= pro[i]){
                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(i);
            }
        }

//        System.out.print("最优解：");
//        l = bestQueue.getTask().size();
//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.print("temp：");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(tempQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();


        Collections.shuffle(bestQueue.getTask());

//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();

        //System.out.println(bestQueue.getTask().size());

        if(!bestQueue.getTask().isEmpty()){
            //System.out.println(bestQueue.getTask().size());
            int m = 0;

            for (int i = 0; i < taskNum; i++) {
                //System.out.println("当前任务序号" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

        return tempQueue;
    }

    //概率矩阵继承最优解，然后轮盘赌
    public static TaskQueue mapOntoSolutionII(int index, double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){


        int l = bestQueue.getTask().size();
//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();


        for (int i = taskNum-1; i >= 0; i--) {

            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = bestQueue.getTask().get(i);
            if(lamda <= pro[index][i]){
                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(i);
            }
        }

//        System.out.print("最优解：");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.print("temp：");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(tempQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();

        //轮盘赌
        for (int i = 0; i < taskNum; i++) {
            if(tempQueue.getTask().get(i).getTaskID() == -1){

                double sum = 0.0;
                double r = Math.random()*1;
                int flag = 0;
                for (int j = 0; j < taskNum; j++){
                    sum += pro[j][i];
                    if(sum > r){
                        for (int k = 0; k < bestQueue.getTask().size(); k++) {
                            if(bestQueue.getTask().get(k).getTaskID() == j){
                                tempQueue.getTask().set(i, bestQueue.getTask().get(k));
                                bestQueue.getTask().remove(k);
                                flag = 1;
                                break;
                            }

                        }

                    }
                    if(flag == 1) break;
                }
            }
        }

        Collections.shuffle(bestQueue.getTask());

        if(!bestQueue.getTask().isEmpty()){
            int m = 0;

            for (int i = 0; i < taskNum; i++) {
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

        return tempQueue;
    }

    //固定概率a继承最优解, 然后概率矩阵一行中对应位置继承最优解，最后随机
    public static TaskQueue mapOntoSolutionIII(double[] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print(bestQueue, taskNum);

        double a = 0.72;

        for (int i = taskNum-1; i >= 0; i--) {

            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = bestQueue.getTask().get(i);

            if(lamda <= a){
                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(i);
            }
            else {
                lamda = 0 + (double)(Math.random() * ((1 - 0)));
                if(lamda <= pro[i]){
                    tempQueue.getTask().set(i, task);
                    bestQueue.getTask().remove(i);
                }
            }
        }

        //print("最优解：", bestQueue, bestQueue.getTask().size());
        //print("temp：", tempQueue, taskNum);

        Collections.shuffle(bestQueue.getTask());

        //print("最优解：", bestQueue, bestQueue.getTask().size());
        //System.out.println(bestQueue.getTask().size());

        if(!bestQueue.getTask().isEmpty()){
            //System.out.println(bestQueue.getTask().size());
            int m = 0;

            for (int i = 0; i < taskNum; i++) {
                //System.out.println("当前任务序号" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

        return tempQueue;
    }

    //概率矩阵继承最优解,再继承次优解，最后随机
    public static TaskQueue mapOntoSolutionIV(double[] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue, TaskQueue secondQueue, DpuSystem sys){

        int l = bestQueue.getTask().size();
//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        for (int i = 0; i < l; i++) {
//            System.out.print(secondQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();


        for (int i = taskNum-1; i >= 0; i--) {

            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = bestQueue.getTask().get(i);
            if(lamda <= pro[i]){
                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(i);
                //System.out.println("best继承：【" + task.getTaskID() + "】");
                Task tempTask = new Task(9999,0,0,0, 0, sys);
                //注意要改的是次优解中对应任务，不是对应位置
                for (int j = 0; j < taskNum; j++) {
                    if(secondQueue.getTask().get(j).getTaskID() == task.getTaskID()){
                        secondQueue.getTask().set(j, tempTask);
                        break;
                    }
                }
            }
        }

//        System.out.print("【temp】：");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(tempQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//
//        l = bestQueue.getTask().size();
//        System.out.println("【最优解before】：" + l);
//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.println("【次优解before】：" + l);
//        for (int i = 0; i < secondQueue.getTask().size(); i++) {
//            System.out.print(secondQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();

        for (int i = taskNum-1; i >= 0; i--) {
            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = secondQueue.getTask().get(i);
            //注意判断条件！！！从最优解中已经继承的位置不要再选
            if(lamda <= pro[i] && task.getTaskID() != 9999 && tempQueue.getTask().get(i).getTaskID() == -1){
                tempQueue.getTask().set(i, task);
                Task tempTask = new Task(9999,0,0,0, 0, sys);
                secondQueue.getTask().set(i, tempTask);
                //System.out.println("second继承：【" + task.getTaskID() + "】");
                for (int j = 0; j < bestQueue.getTask().size(); j++) {
                    if(task.getTaskID() == bestQueue.getTask().get(j).getTaskID())
                        bestQueue.getTask().remove(j);
                }
            }
        }

//        l = bestQueue.getTask().size();
//        System.out.println("【最优解after】：" + l);
//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.println("【次优解after】：" );
//        for (int i = 0; i < secondQueue.getTask().size(); i++) {
//            System.out.print(secondQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.print("【temp】：");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(tempQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();


        Collections.shuffle(bestQueue.getTask());

//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();

        //System.out.println(bestQueue.getTask().size());

        if(!bestQueue.getTask().isEmpty()){
            //System.out.println(bestQueue.getTask().size());
            int m = 0;

            for (int i = 0; i < taskNum; i++) {
                //System.out.println("当前任务序号" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

//        System.out.print("【temp FINAL】：");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(tempQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();

        return tempQueue;
    }

    //固定概率a继承最优解, 然后概率矩阵选择每一剩余位置最大概率任务  ！！！会导致子代个体相同
    public static TaskQueue mapOntoSolutionV(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print(bestQueue, taskNum);
        //printPro(pro, taskNum);

        double a = 0.72;

        for (int i = taskNum-1; i >= 0; i--) {

            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = bestQueue.getTask().get(i);

            if(lamda <= a){
                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(i);

                int taskID = task.getTaskID();
                for (int j = 0; j < taskNum; j++) {
                    pro[taskID][j] = 0; //当前任务不再可选
                    pro[j][i] = 0; //当前位置不再可选
                }
            }
        }

        //print("最优解：", bestQueue, bestQueue.getTask().size());
        //print("temp：", tempQueue, taskNum);

        List<Integer> remainTaskID = new ArrayList<Integer>();
        int l = bestQueue.getTask().size();
        for (int i = 0; i < l; i++) {
            remainTaskID.add(bestQueue.getTask().get(i).getTaskID());
        }

        List<Integer> remainLocation = new ArrayList<Integer>();
        for (int i = 0; i < taskNum; i++) {
            if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                remainLocation.add(i);
            }
        }

        //printInt(remainLocation);
        //printInt(remainTaskID);
        //printPro(pro, taskNum);

        while(!bestQueue.getTask().isEmpty()){

            double maxPro = 0;
            int tempTask = 0;
            int tempLocation = remainLocation.get(0);

            for (int i : remainLocation) {
                for (int j : remainTaskID) {
                    //System.out.println("i：" + i + "  j：" + j + "  Pro: " + pro[j][i]);
                    if(pro[j][i] > maxPro){

                        tempLocation = i; //i就是剩余位置
                        tempTask = remainTaskID.indexOf(j); //j是剩余任务ID，取index才是该任务在最优解序列中的位置
                        maxPro = pro[j][i];
                        //System.out.println("位置：" + tempLocation + "  任务：" + j + "  Pro: " + pro[j][i]);
                    }
                }
            }

            Task task = bestQueue.getTask().get(tempTask);
            tempQueue.getTask().set(tempLocation, task);

            int taskID = task.getTaskID();
            int lo = remainLocation.indexOf(tempLocation);
            //System.out.println("位置：" + tempLocation + "  任务：" + taskID);

            bestQueue.getTask().remove(tempTask);
            remainTaskID.remove(tempTask);
            remainLocation.remove(lo);
            //printInt(remainLocation);
            //printInt(remainTaskID);

            //print("最优解：", bestQueue, bestQueue.getTask().size());
            //print("temp：", tempQueue, taskNum);

            for (int j = 0; j < taskNum; j++) {
                pro[taskID][j] = 0; //当前任务不再可选
                pro[j][tempLocation] = 0; //当前位置不再可选
            }
        }
        return tempQueue;
    }

    //固定概率a继承最优解, 然后基于概率矩阵轮盘赌 EDA_pure
    public static TaskQueue mapOntoSolutionVI(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print(bestQueue, taskNum);
        //printPro(pro, taskNum);

        double a = 0.72;

        for (int i = taskNum-1; i >= 0; i--) { //遍历位置

            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = bestQueue.getTask().get(i);

            if(lamda <= a){
                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(i);

                int taskID = task.getTaskID();
                for (int j = 0; j < taskNum; j++) {
                    pro[taskID][j] = 0; //当前任务不再可选
                    pro[j][i] = 0; //当前位置不再可选
                }
            }
        }

        //print("最优解：", bestQueue, bestQueue.getTask().size());
        //print("temp：", tempQueue, taskNum);

        List<Integer> remainTaskID = new ArrayList<Integer>();
        int l = bestQueue.getTask().size();
        for (int i = 0; i < l; i++)
            remainTaskID.add(bestQueue.getTask().get(i).getTaskID());


        List<Integer> remainLocation = new ArrayList<Integer>();
        for (int i = 0; i < taskNum; i++)
            if(tempQueue.getTask().get(i).getTaskID() == -1)
                remainLocation.add(i);

        Collections.shuffle(remainLocation);

//        List<Integer> remainLocationCopy = new ArrayList<Integer>();
//        for (int i : remainLocation)
//            remainLocationCopy.add(i);


//        printInt(remainTaskID);
//        printInt(remainLocation);
//        printPro(pro, taskNum);
//        System.out.println();

        adjustPro(pro, remainTaskID, remainLocation);
        //printPro(pro, taskNum);

        for (int i = 0; i < taskNum; i++) {

            if(remainLocation.contains(i)){
                int taskIndex = Roulette(pro, i, remainTaskID);

                //System.out.println("taskIndex: " + taskIndex);
                //printInt(remainTaskID);

                Task task = bestQueue.getTask().get(taskIndex);

                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(taskIndex);


                int taskID = task.getTaskID();
                for (int j : remainLocation)
                    pro[taskID][j] = 0; //当前任务不再可选

                for (int j : remainTaskID)
                    pro[j][i] = 0; //当前位置不再可选

                remainLocation.remove(remainLocation.indexOf(i));
                remainTaskID.remove(taskIndex);

                adjustPro(pro, remainTaskID, remainLocation);
            }
        }

        //print("temp：", tempQueue, taskNum);

        return tempQueue;
    }

    //概率矩阵轮盘赌，然后随机
    public static TaskQueue mapOntoSolutionVII(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print(bestQueue, taskNum);
        Collections.shuffle(bestQueue.getTask());

//        print("固定概率后剩余解：", bestQueue, bestQueue.getTask().size());
//        print("temp1：", tempQueue, taskNum);

        List<Integer> remainTaskID = new ArrayList<Integer>();
        int l = bestQueue.getTask().size();
        for (int i = 0; i < l; i++)
            remainTaskID.add(bestQueue.getTask().get(i).getTaskID());

        List<Integer> remainLocation = new ArrayList<Integer>();
        for (int i = 0; i < taskNum; i++)
            //if(tempQueue.getTask().get(i).getTaskID() == -1)
                remainLocation.add(i);

        for (int j : remainLocation) {

            double lamda =  Math.random()*1;
            double sum = 0.0;
            for (int i = 0; i < taskNum; i++) {
                sum += pro[i][j];
                if(sum > lamda){
                    if(remainTaskID.contains(i)){
                        int taskIndex = remainTaskID.indexOf(i);
                        Task task = bestQueue.getTask().get(taskIndex);
                        tempQueue.getTask().set(j, task);
                        bestQueue.getTask().remove(taskIndex);
                        remainTaskID.remove(taskIndex);
                    }
                    break;
                }

            }

        }

//        print("轮盘赌后剩余解：", bestQueue, bestQueue.getTask().size());
//        print("temp2：", tempQueue, taskNum);

        Collections.shuffle(bestQueue.getTask());

        if(!bestQueue.getTask().isEmpty()){
            //System.out.println(bestQueue.getTask().size());
            int m = 0;

            for (int i = 0; i < taskNum; i++) {
                //System.out.println("当前任务序号" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

        //print("temp3：", tempQueue, taskNum);

        return tempQueue;
    }

    //固定概率a继承最优解, 然后概率矩阵继承轮盘赌，最后随机
    public static TaskQueue mapOntoSolutionVIII(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print(bestQueue, taskNum);

        double a = 0.72;

        for (int i = taskNum-1; i >= 0; i--) {

            double lamda =  Math.random()*1;
            Task task = bestQueue.getTask().get(i);

            if(lamda <= a){
                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(i);
            }
        }

        Collections.shuffle(bestQueue.getTask());

//        print("固定概率后剩余解：", bestQueue, bestQueue.getTask().size());
//        print("temp1：", tempQueue, taskNum);

        List<Integer> remainTaskID = new ArrayList<Integer>();
        int l = bestQueue.getTask().size();
        for (int i = 0; i < l; i++)
            remainTaskID.add(bestQueue.getTask().get(i).getTaskID());

        List<Integer> remainLocation = new ArrayList<Integer>();
        for (int i = 0; i < taskNum; i++)
            if(tempQueue.getTask().get(i).getTaskID() == -1)
                remainLocation.add(i);

        for (int j : remainLocation) {

            double lamda =  Math.random()*1;
            double sum = 0.0;
            for (int i = 0; i < taskNum; i++) {
                sum += pro[i][j];
                if(sum > lamda){
                    if(remainTaskID.contains(i)){
                        int taskIndex = remainTaskID.indexOf(i);
                        Task task = bestQueue.getTask().get(taskIndex);
                        tempQueue.getTask().set(j, task);
                        bestQueue.getTask().remove(taskIndex);
                        remainTaskID.remove(taskIndex);
                    }
                    break;
                }

            }

        }

//        print("轮盘赌后剩余解：", bestQueue, bestQueue.getTask().size());
//        print("temp2：", tempQueue, taskNum);

        Collections.shuffle(bestQueue.getTask());

        if(!bestQueue.getTask().isEmpty()){
            //System.out.println(bestQueue.getTask().size());
            int m = 0;

            for (int i = 0; i < taskNum; i++) {
                //System.out.println("当前任务序号" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

        //print("temp3：", tempQueue, taskNum);

        return tempQueue;
    }

    //基于概率矩阵轮盘赌，每确定一个任务后调整概率矩阵
    public static TaskQueue mapOntoSolutionIX(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //printPro(pro, taskNum);
        //print("最优解：", bestQueue, taskNum);
        //print("temp：", tempQueue, taskNum);

        List<Integer> remainTaskID = new ArrayList<Integer>();
        int l = bestQueue.getTask().size();
        for (int i = 0; i < l; i++)
            remainTaskID.add(bestQueue.getTask().get(i).getTaskID());


        List<Integer> remainLocation = new ArrayList<Integer>();
        for (int i = 0; i < taskNum; i++)
            //if(tempQueue.getTask().get(i).getTaskID() == -1)
                remainLocation.add(i);

        Collections.shuffle(remainLocation);

//        List<Integer> remainLocationCopy = new ArrayList<Integer>();
//        for (int i : remainLocation)
//            remainLocationCopy.add(i);


//        printInt(remainTaskID);
//        printInt(remainLocation);
//        printPro(pro, taskNum);
//        System.out.println();

        adjustPro(pro, remainTaskID, remainLocation);
        //printPro(pro, taskNum);

        for (int i = 0; i < taskNum; i++) {

            if(remainLocation.contains(i)){
                int taskIndex = Roulette(pro, i, remainTaskID);

                //System.out.println("taskIndex: " + taskIndex);
                //printInt(remainTaskID);

                Task task = bestQueue.getTask().get(taskIndex);

                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(taskIndex);


                int taskID = task.getTaskID();
                for (int j : remainLocation)
                    pro[taskID][j] = 0; //当前任务不再可选

                for (int j : remainTaskID)
                    pro[j][i] = 0; //当前位置不再可选

                remainLocation.remove(remainLocation.indexOf(i));
                remainTaskID.remove(taskIndex);

                adjustPro(pro, remainTaskID, remainLocation);
            }
        }

        //print("temp：", tempQueue, taskNum);

        return tempQueue;
    }


    //调整概率矩阵，各列相应扩大，使和为1
    public static double[][] adjustPro(double[][] pro, List<Integer> remainTaskID, List<Integer> remainLocation){

        int remainLocationNum = remainLocation.size();
        int remainTaskNum = remainTaskID.size();
        double[] sumSet = new  double[remainLocationNum];

        for (int i = 0; i < remainLocationNum; i++) {
            sumSet[i] = 0.0;
            for (int j = 0; j < remainTaskNum; j++) {
                sumSet[i] += pro[remainTaskID.get(j)][remainLocation.get(i)]; //行：位置， 列：任务
            }
        }

        for (int i : remainLocation) {
            if(sumSet[remainLocation.indexOf(i)] == 0){
                for (int j : remainTaskID){
                    pro[j][i] = 1.0/(double)remainTaskNum;
                }
            }
            else{
                for (int j : remainTaskID){
                    pro[j][i] = pro[j][i] / sumSet[remainLocation.indexOf(i)];
                }
            }
        }

        return pro;
    }


    //轮盘赌
    public static int Roulette(double pro[][], int location, List<Integer> remainTaskID){

        double sum = 0.0;
        double r = Math.random()*1;
        int taskIndex = -1;

        for (int i : remainTaskID) {
            sum += pro[i][location];
            if(sum > r){
                taskIndex = remainTaskID.indexOf(i);
                break;
            }
        }
        return taskIndex;
    }


    //复制队列
    public static TaskQueue clone(TaskQueue newQueue, TaskQueue oldQueue, int taskNum, int instanceID, DpuSystem sys) throws IOException {

        newQueue = new TaskQueue(taskNum, instanceID, sys);
        for (int i = 0; i < taskNum; i++) {
            newQueue.getTask().set(i,oldQueue.getTask().get(i));
            //System.out.print(newQueue.getTask().get(i).getTaskID());System.out.print(" ");
        }
        newQueue.setQueueID(oldQueue.getQueueID());
        newQueue.getTimeDone(taskNum);
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

    public static void printInt(List<Integer> list){

        for (int i = 0; i < list.size(); i++) {
            System.out.print(list.get(i));
            System.out.print(" ");
        }
        System.out.println();
    }

    public static void printPro(double[][] pro, int taskNum){

        DecimalFormat df = new DecimalFormat("#0.000");
        for (int i = 0; i < taskNum; i++) {
            for (int j = 0; j < taskNum; j++) {
                System.out.print(df.format(pro[i][j])); System.out.print(" ");
            }
            System.out.println();
        }

    }


    public static void main(String[] args) throws IOException {

//        int[] taskNum = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
//        int instanceNum = 10;
//        int loopNum = 1000;
//        int testNum = 5;
//        int groupSize = 30;


        int[] taskNum = {110, 120};
        int instanceNum = 10;
        int loopNum = 1000;
        int testNum = 5;
        int groupSize = 30;

        DpuSystem sys = new DpuSystem();

        DecimalFormat format = new DecimalFormat("0.00000");

        String fileName = "results/Results_EDA_tradition.txt";
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
                    System.out.println("第【" + k + "】轮最优解");

                    long startTime=System.currentTimeMillis();   //获取开始时间
                    bestQueue = processAlgorithm(groupSize, taskNum[i], j, loopNum, sys);
                    long endTime=System.currentTimeMillis() - startTime; //获取结束时间

                    String timeDone = format.format(bestQueue.getTimeDone());

                    writer.write( taskNum[i] + "\t" + j + "\t" + k + "\t" + timeDone  + "\t" + endTime + "\r\n");
                    writer.flush();

                }
            }

        }

        writer.close();
    }
}
//
//    public static void main(String[] args) throws IOException {
//
//        int[] taskNum = {10, 20, 30, 40, 50};
//        int instanceNum = 10;
//        int loopNum = 1000;
//        int testNum = 5;
//
//        int groupSize = 30;
//        DpuSystem sys = new DpuSystem();
//
//        //开始写入excel,创建模型文件头
//        String[] excelTitle = {"taskNum","instanceID","testRound","最优解完工时间"};
//        //创建Excel文件，B库CD表文件
//        File fileA = new File("Result.xls");
//        if(fileA.exists()){
//            //如果文件存在就删除
//            fileA.delete();
//        }
//        try {
//            fileA.createNewFile();
//            //创建工作簿
//            WritableWorkbook workbookA = Workbook.createWorkbook(fileA);
//            //创建sheet
//            WritableSheet sheetA = workbookA.createSheet("sheet1", 0);
//            Label labelA = null;
//            //设置列名
//            for (int i = 0; i < excelTitle.length; i++) {
//                labelA = new Label(i,0,excelTitle[i]);
//                sheetA.addCell(labelA);
//            }
//            //获取数据源
//
//            int line = 1;
//
//            for (int i = 0; i < taskNum.length; i++) {
//
//                for (int j = 0; j < instanceNum; j++) {
//                    TaskQueue bestQueue = new TaskQueue(taskNum[i], j, sys);
//
//                    System.out.println("---------------task【"+ taskNum[i] + "_" + j + "】---------------");
//
//                    for (int k = 0; k < testNum; k++) {
//                        System.out.println("第【" + k + "】轮最优解");
//                        bestQueue = processAlgorithm(groupSize, taskNum[i], j, loopNum, sys);
//
//                        labelA = new Label(0,line,taskNum[i]+" ");
//                        sheetA.addCell(labelA);
//                        labelA = new Label(1,line,j+" ");
//                        sheetA.addCell(labelA);
//                        labelA = new Label(2,line,k+" ");
//                        sheetA.addCell(labelA);
//                        labelA = new Label(3,line,bestQueue.getTimeDone()+" ");
//                        sheetA.addCell(labelA);
//
//                        line++;
//                    }
//                }
//
//            }
//
//            workbookA.write();//写入数据?? ??? ?
//            workbookA.close(); //关闭连接
//
//        } catch (Exception e) {
//            System.out.println("文件写入失败，报异常...");
//        }
//    }
//}
