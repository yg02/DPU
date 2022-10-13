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

        int[][] taskOrder = new int[groupSize][taskNum];//����˳������Ⱥ������������

        //����˳���б�
        List<Integer> numbers = new ArrayList<Integer>();
        for (int i = 0; i < taskNum; i++)
            numbers.add(i);

        //ϴ�ƣ�������ɳ�ʼ˳������
        for (int i = 0; i < groupSize; i++) {
            Collections.shuffle(numbers);
            for (int j = 0; j < taskNum; j++)
                taskOrder[i] = numbers.stream().mapToInt(Integer::valueOf).toArray();
        }


//        System.out.println("˳�����飺 ");
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

        TaskQueue originQueue = new TaskQueue(taskNum, instanceID, sys);//ԭʼ��������
        TaskQueue bestQueue = new TaskQueue(taskNum, instanceID, sys);//���Ž�
        //TaskQueue secondQueue = new TaskQueue(taskNum, instanceID, sys);//���Ž�
        bestQueue.setTimeDone(9999999);
        ArrayList<TaskQueue> solution = new ArrayList<TaskQueue>();

        //���ɳ�ʼ��Ⱥ��������Ӧ��ֵ
        for (int i = 0; i < groupSize; i++) {
            TaskQueue tempQueue = new TaskQueue(taskNum, instanceID, sys);
            tempQueue.setQueueID(i);
            solution.add(tempQueue);

            //System.out.println("-------���塾" + i + "��-------");
            for (int j = 0; j < taskNum; j++) {
                solution.get(i).getTask().set(j,originQueue.getTask().get(taskOrder[i][j]));
                //System.out.print(solution.get(i).getTask().get(j).getTaskID());
                //System.out.print(" ");
            }
            solution.get(i).getTimeDone(taskNum);
            //System.out.println("���깤ʱ�䡿:  " + solution.get(i).getTimeDone());
        }

        //�깤ʱ����������
        Collections.sort(solution, new Comparator<TaskQueue>() {
            @Override
            public int compare(TaskQueue t1, TaskQueue t2) {
                return t1.getTimeDone() > t2.getTimeDone() ? 1 : -1;
            }
        });

        //���ɳ�ʼ���Ž�
        //����ҵ����
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

//        System.out.println("��ʼ���Ž⣺");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("���깤ʱ�䡿:  "+ bestQueue.getTimeDone());
//
//        System.out.println("��ʼ���Ž⣺");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(secondQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("���깤ʱ�䡿:  "+ secondQueue.getTimeDone());

        //��ʼ����
        for (int i = 0; i < loopNum; i++) {
            //bestQueue = processEDA(solution, groupSize, taskNum, instanceID, i, bestQueue, secondQueue, sys);
            bestQueue = processEDA(solution, groupSize, taskNum, instanceID, i, bestQueue, sys);
        }

        //System.out.println("���Ž�");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("���깤ʱ�䡿:  "+ bestQueue.getTimeDone());
        print(bestQueue, taskNum);

        return bestQueue;

    }

    //EDA�㷨
    public static TaskQueue processEDA(ArrayList<TaskQueue> solution, int groupSize, int taskNum, int instanceID, int loopNum, TaskQueue bestQueue, DpuSystem sys) throws IOException {

        int selectSize = groupSize/2;
        double pro[][] = new double[taskNum][taskNum];//���ʾ���
        double a = 1;//ѧϰ��

        DecimalFormat df = new DecimalFormat("#0.00");

        //���ɸ��ʾ���
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

        solution.clear(); //�����Ⱥ

//        for (int j = 0; j < 2; j++) {  //���ֲ�������������Ⱥ����ΪN
//            for (int i = 0; i < selectSize; i++)

        for (int i = 0; i < groupSize; i++) {

            TaskQueue tempQueue = new TaskQueue(taskNum, instanceID, sys);
            tempQueue.setQueueID(i);
            for (int k = 0; k < taskNum; k++) {
                tempQueue.getTask().get(k).setTaskID(-1); //��Ǹ�λ���Ƿ����·�������
            }

            TaskQueue bestQueueCopy = new TaskQueue(taskNum, instanceID, sys);
            bestQueueCopy = clone(bestQueueCopy, bestQueue, taskNum, instanceID, sys);

            //ӳ�����
            //tempQueue = mapOntoSolutionII(i % taskNum, pro, tempQueue, taskNum, bestQueueCopy);
            //tempQueue = mapOntoSolutionIII(pro[i % taskNum], tempQueue, taskNum, bestQueueCopy);

            //TaskQueue secondQueueCopy = new TaskQueue(taskNum, instanceID, sys);
            //secondQueueCopy = clone(secondQueueCopy, secondQueue, taskNum, instanceID, sys);
            //tempQueue = mapOntoSolutionIV(pro[i % taskNum], tempQueue, taskNum, bestQueueCopy, secondQueueCopy, sys);

            double proCopy[][] = new double[taskNum][taskNum];//���ʾ���;
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

        //�깤ʱ����������
        Collections.sort(solution, new Comparator<TaskQueue>() {
            @Override
            public int compare(TaskQueue t1, TaskQueue t2) {
                return t1.getTimeDone() > t2.getTimeDone() ? 1 : -1;
            }
        });

        //System.out.println();

        //�������Ž�
        if(solution.get(0).getTimeDone() < bestQueue.getTimeDone()){
            //secondQueue = clone(secondQueue, bestQueue, taskNum, instanceID, sys);
            bestQueue = clone(bestQueue, solution.get(0),taskNum, instanceID, sys);
            //System.out.println("----------��new��-----------------------------------------------------------------------");
        }

//        DecimalFormat df = new DecimalFormat("#.0000");
//        System.out.println(df.format(bestQueue.getTimeDone()));

//        System.out.println("�ڡ�" + loopNum + "�������Ž�");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("���깤ʱ�䡿:  "+ bestQueue.getTimeDone());

//        System.out.println("���Ž⣺");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(secondQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println("���깤ʱ�䡿:  "+ secondQueue.getTimeDone());


//        System.out.println("���º�");
//        for (int i = 0; i < selectSize; i++) {
//            System.out.print("���塾" + solution.get(i).getQueueID() + "��:  ");
//            for (int j = 0; j < taskNum; j++) {
//                System.out.print(solution.get(i).getTask().get(j).getTaskID());
//                System.out.print(" ");
//            }
//            System.out.println("���깤ʱ�䡿:  " + solution.get(i).getTimeDone());
//    }

//        ArrayList<TaskQueue> bestTwoQueues = new ArrayList<TaskQueue>();
//        bestTwoQueues.add(bestQueue);
//        bestTwoQueues.add(secondQueue);

        return  bestQueue;

    }

    //ȫ�����̶� EDA_tradition
    public static TaskQueue mapOntoSolution(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print("temp��", tempQueue, taskNum);

        //System.out.println("------------------------New Round ------------------------");

        DecimalFormat df = new DecimalFormat("#0.00");
        //System.out.println(df.format());

//        System.out.println("��start��");
//        for (int i = 0; i < taskNum; i++) {
//            for (int j = 0; j < taskNum; j++) {
//                System.out.print(df.format(pro[i][j])); System.out.print(" ");
//            }
//            System.out.println();
//        }

        //ID��������
        Collections.sort(bestQueue.getTask(), new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return t1.getTaskID() > t2.getTaskID() ? 1 : -1;
            }
        });

        //print("���Ž⣺", bestQueue, bestQueue.getTask().size());

        double[] sumSet = new  double[taskNum];
        for (int i = 0; i < taskNum; i++)
            sumSet[i] = 1.0;

        //ĳ�и��ʿ��ܻᱻȫ������
        int[] flag = new  int[taskNum];
        for (int i = 0; i < taskNum; i++)
            flag[i] = 0;

        int remainNum = taskNum;

        //���̶�
        for (int i = 0; i < taskNum; i++) {

//            for (int j = 0; j < taskNum; j++) {
//                System.out.print(pro[j][i]);
//                System.out.print(" ");
//            }
//            System.out.println();

            //System.out.println("��" + i + "��");

            double sum = 0.0;
            double r = Math.random()*1;
            //System.out.print(r);
            int tempID = -1;
            for (int j = 0; j < taskNum; j++) {
                sum += pro[j][i];
                if(sum > r){
                    //System.out.println(" < " + sum + " ����" + j);
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

//            System.out.println("��during��");
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
                //System.out.println("����sum:" + sum);
                if(sum == 0){
                    for (int k = 0; k < taskNum; k++) {
                        if(flag[k] != 1){
                            pro[k][j] = 1.0/(double)remainNum;
                        }
                    }
                }
            }

//            System.out.println("��final��");
//            for (int k = 0; k < taskNum; k++) {
//                for (int j = 0; j < taskNum; j++) {
//                    System.out.print(df.format(pro[k][j])); System.out.print(" ");
//                }
//                System.out.println();
//            }

            //print("temp��", tempQueue, taskNum);

        }

        return tempQueue;
    }


    //���ʾ���̳����Ž⣬Ȼ�����
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

//        System.out.print("���Ž⣺");
//        l = bestQueue.getTask().size();
//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.print("temp��");
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
                //System.out.println("��ǰ�������" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

        return tempQueue;
    }

    //���ʾ���̳����Ž⣬Ȼ�����̶�
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

//        System.out.print("���Ž⣺");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.print("temp��");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(tempQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();

        //���̶�
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

    //�̶�����a�̳����Ž�, Ȼ����ʾ���һ���ж�Ӧλ�ü̳����Ž⣬������
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

        //print("���Ž⣺", bestQueue, bestQueue.getTask().size());
        //print("temp��", tempQueue, taskNum);

        Collections.shuffle(bestQueue.getTask());

        //print("���Ž⣺", bestQueue, bestQueue.getTask().size());
        //System.out.println(bestQueue.getTask().size());

        if(!bestQueue.getTask().isEmpty()){
            //System.out.println(bestQueue.getTask().size());
            int m = 0;

            for (int i = 0; i < taskNum; i++) {
                //System.out.println("��ǰ�������" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

        return tempQueue;
    }

    //���ʾ���̳����Ž�,�ټ̳д��Ž⣬������
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
                //System.out.println("best�̳У���" + task.getTaskID() + "��");
                Task tempTask = new Task(9999,0,0,0, 0, sys);
                //ע��Ҫ�ĵ��Ǵ��Ž��ж�Ӧ���񣬲��Ƕ�Ӧλ��
                for (int j = 0; j < taskNum; j++) {
                    if(secondQueue.getTask().get(j).getTaskID() == task.getTaskID()){
                        secondQueue.getTask().set(j, tempTask);
                        break;
                    }
                }
            }
        }

//        System.out.print("��temp����");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(tempQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//
//        l = bestQueue.getTask().size();
//        System.out.println("�����Ž�before����" + l);
//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.println("�����Ž�before����" + l);
//        for (int i = 0; i < secondQueue.getTask().size(); i++) {
//            System.out.print(secondQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();

        for (int i = taskNum-1; i >= 0; i--) {
            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = secondQueue.getTask().get(i);
            //ע���ж����������������Ž����Ѿ��̳е�λ�ò�Ҫ��ѡ
            if(lamda <= pro[i] && task.getTaskID() != 9999 && tempQueue.getTask().get(i).getTaskID() == -1){
                tempQueue.getTask().set(i, task);
                Task tempTask = new Task(9999,0,0,0, 0, sys);
                secondQueue.getTask().set(i, tempTask);
                //System.out.println("second�̳У���" + task.getTaskID() + "��");
                for (int j = 0; j < bestQueue.getTask().size(); j++) {
                    if(task.getTaskID() == bestQueue.getTask().get(j).getTaskID())
                        bestQueue.getTask().remove(j);
                }
            }
        }

//        l = bestQueue.getTask().size();
//        System.out.println("�����Ž�after����" + l);
//        for (int i = 0; i < l; i++) {
//            System.out.print(bestQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.println("�����Ž�after����" );
//        for (int i = 0; i < secondQueue.getTask().size(); i++) {
//            System.out.print(secondQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();
//        System.out.print("��temp����");
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
                //System.out.println("��ǰ�������" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

//        System.out.print("��temp FINAL����");
//        for (int i = 0; i < taskNum; i++) {
//            System.out.print(tempQueue.getTask().get(i).getTaskID());System.out.print(" ");
//        }
//        System.out.println();

        return tempQueue;
    }

    //�̶�����a�̳����Ž�, Ȼ����ʾ���ѡ��ÿһʣ��λ������������  �������ᵼ���Ӵ�������ͬ
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
                    pro[taskID][j] = 0; //��ǰ�����ٿ�ѡ
                    pro[j][i] = 0; //��ǰλ�ò��ٿ�ѡ
                }
            }
        }

        //print("���Ž⣺", bestQueue, bestQueue.getTask().size());
        //print("temp��", tempQueue, taskNum);

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
                    //System.out.println("i��" + i + "  j��" + j + "  Pro: " + pro[j][i]);
                    if(pro[j][i] > maxPro){

                        tempLocation = i; //i����ʣ��λ��
                        tempTask = remainTaskID.indexOf(j); //j��ʣ������ID��ȡindex���Ǹ����������Ž������е�λ��
                        maxPro = pro[j][i];
                        //System.out.println("λ�ã�" + tempLocation + "  ����" + j + "  Pro: " + pro[j][i]);
                    }
                }
            }

            Task task = bestQueue.getTask().get(tempTask);
            tempQueue.getTask().set(tempLocation, task);

            int taskID = task.getTaskID();
            int lo = remainLocation.indexOf(tempLocation);
            //System.out.println("λ�ã�" + tempLocation + "  ����" + taskID);

            bestQueue.getTask().remove(tempTask);
            remainTaskID.remove(tempTask);
            remainLocation.remove(lo);
            //printInt(remainLocation);
            //printInt(remainTaskID);

            //print("���Ž⣺", bestQueue, bestQueue.getTask().size());
            //print("temp��", tempQueue, taskNum);

            for (int j = 0; j < taskNum; j++) {
                pro[taskID][j] = 0; //��ǰ�����ٿ�ѡ
                pro[j][tempLocation] = 0; //��ǰλ�ò��ٿ�ѡ
            }
        }
        return tempQueue;
    }

    //�̶�����a�̳����Ž�, Ȼ����ڸ��ʾ������̶� EDA_pure
    public static TaskQueue mapOntoSolutionVI(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print(bestQueue, taskNum);
        //printPro(pro, taskNum);

        double a = 0.72;

        for (int i = taskNum-1; i >= 0; i--) { //����λ��

            double lamda = 0 + (double)(Math.random() * ((1 - 0)));
            Task task = bestQueue.getTask().get(i);

            if(lamda <= a){
                tempQueue.getTask().set(i, task);
                bestQueue.getTask().remove(i);

                int taskID = task.getTaskID();
                for (int j = 0; j < taskNum; j++) {
                    pro[taskID][j] = 0; //��ǰ�����ٿ�ѡ
                    pro[j][i] = 0; //��ǰλ�ò��ٿ�ѡ
                }
            }
        }

        //print("���Ž⣺", bestQueue, bestQueue.getTask().size());
        //print("temp��", tempQueue, taskNum);

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
                    pro[taskID][j] = 0; //��ǰ�����ٿ�ѡ

                for (int j : remainTaskID)
                    pro[j][i] = 0; //��ǰλ�ò��ٿ�ѡ

                remainLocation.remove(remainLocation.indexOf(i));
                remainTaskID.remove(taskIndex);

                adjustPro(pro, remainTaskID, remainLocation);
            }
        }

        //print("temp��", tempQueue, taskNum);

        return tempQueue;
    }

    //���ʾ������̶ģ�Ȼ�����
    public static TaskQueue mapOntoSolutionVII(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //print(bestQueue, taskNum);
        Collections.shuffle(bestQueue.getTask());

//        print("�̶����ʺ�ʣ��⣺", bestQueue, bestQueue.getTask().size());
//        print("temp1��", tempQueue, taskNum);

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

//        print("���̶ĺ�ʣ��⣺", bestQueue, bestQueue.getTask().size());
//        print("temp2��", tempQueue, taskNum);

        Collections.shuffle(bestQueue.getTask());

        if(!bestQueue.getTask().isEmpty()){
            //System.out.println(bestQueue.getTask().size());
            int m = 0;

            for (int i = 0; i < taskNum; i++) {
                //System.out.println("��ǰ�������" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

        //print("temp3��", tempQueue, taskNum);

        return tempQueue;
    }

    //�̶�����a�̳����Ž�, Ȼ����ʾ���̳����̶ģ�������
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

//        print("�̶����ʺ�ʣ��⣺", bestQueue, bestQueue.getTask().size());
//        print("temp1��", tempQueue, taskNum);

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

//        print("���̶ĺ�ʣ��⣺", bestQueue, bestQueue.getTask().size());
//        print("temp2��", tempQueue, taskNum);

        Collections.shuffle(bestQueue.getTask());

        if(!bestQueue.getTask().isEmpty()){
            //System.out.println(bestQueue.getTask().size());
            int m = 0;

            for (int i = 0; i < taskNum; i++) {
                //System.out.println("��ǰ�������" + tempQueue.getTask().get(i).getTaskID() );
                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
                    //System.out.println("m:" + m);
                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
                    m++;
                }
            }
        }

        //print("temp3��", tempQueue, taskNum);

        return tempQueue;
    }

    //���ڸ��ʾ������̶ģ�ÿȷ��һ�������������ʾ���
    public static TaskQueue mapOntoSolutionIX(double[][] pro, TaskQueue tempQueue, int taskNum, TaskQueue bestQueue){

        //printPro(pro, taskNum);
        //print("���Ž⣺", bestQueue, taskNum);
        //print("temp��", tempQueue, taskNum);

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
                    pro[taskID][j] = 0; //��ǰ�����ٿ�ѡ

                for (int j : remainTaskID)
                    pro[j][i] = 0; //��ǰλ�ò��ٿ�ѡ

                remainLocation.remove(remainLocation.indexOf(i));
                remainTaskID.remove(taskIndex);

                adjustPro(pro, remainTaskID, remainLocation);
            }
        }

        //print("temp��", tempQueue, taskNum);

        return tempQueue;
    }


    //�������ʾ��󣬸�����Ӧ����ʹ��Ϊ1
    public static double[][] adjustPro(double[][] pro, List<Integer> remainTaskID, List<Integer> remainLocation){

        int remainLocationNum = remainLocation.size();
        int remainTaskNum = remainTaskID.size();
        double[] sumSet = new  double[remainLocationNum];

        for (int i = 0; i < remainLocationNum; i++) {
            sumSet[i] = 0.0;
            for (int j = 0; j < remainTaskNum; j++) {
                sumSet[i] += pro[remainTaskID.get(j)][remainLocation.get(i)]; //�У�λ�ã� �У�����
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


    //���̶�
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


    //���ƶ���
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
        System.out.println("���깤ʱ�䡿:  "+ queue.getTimeDone());
    }

    public static void print(String queueName, TaskQueue queue, int taskNum){

        System.out.print(queueName);
        for (int i = 0; i < taskNum; i++) {
            System.out.print(queue.getTask().get(i).getTaskID());
            System.out.print(" ");
        }
        //queue.getTimeDone(taskNum);
        System.out.println("���깤ʱ�䡿:  "+ queue.getTimeDone());
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
        File fileParent = file.getParentFile();//���ص���File����,���Ե���exsit()�ȷ���
        if (!fileParent.exists()) {
            fileParent.mkdirs();// �ܴ����༶Ŀ¼
        }
        if (!file.exists())
            file.createNewFile();//��·�����ܴ����ļ�
        else{
            file.delete();
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file,true);
        BufferedWriter writer = new BufferedWriter(fileWriter);

        for (int i = 0; i < taskNum.length; i++) {

            for (int j = 0; j < instanceNum; j++) {
                TaskQueue bestQueue = new TaskQueue(taskNum[i], j, sys);

                System.out.println("---------------task��"+ taskNum[i] + "_" + j + "��---------------");

                for (int k = 0; k < testNum; k++) {
                    System.out.println("�ڡ�" + k + "�������Ž�");

                    long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��
                    bestQueue = processAlgorithm(groupSize, taskNum[i], j, loopNum, sys);
                    long endTime=System.currentTimeMillis() - startTime; //��ȡ����ʱ��

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
//        //��ʼд��excel,����ģ���ļ�ͷ
//        String[] excelTitle = {"taskNum","instanceID","testRound","���Ž��깤ʱ��"};
//        //����Excel�ļ���B��CD���ļ�
//        File fileA = new File("Result.xls");
//        if(fileA.exists()){
//            //����ļ����ھ�ɾ��
//            fileA.delete();
//        }
//        try {
//            fileA.createNewFile();
//            //����������
//            WritableWorkbook workbookA = Workbook.createWorkbook(fileA);
//            //����sheet
//            WritableSheet sheetA = workbookA.createSheet("sheet1", 0);
//            Label labelA = null;
//            //��������
//            for (int i = 0; i < excelTitle.length; i++) {
//                labelA = new Label(i,0,excelTitle[i]);
//                sheetA.addCell(labelA);
//            }
//            //��ȡ����Դ
//
//            int line = 1;
//
//            for (int i = 0; i < taskNum.length; i++) {
//
//                for (int j = 0; j < instanceNum; j++) {
//                    TaskQueue bestQueue = new TaskQueue(taskNum[i], j, sys);
//
//                    System.out.println("---------------task��"+ taskNum[i] + "_" + j + "��---------------");
//
//                    for (int k = 0; k < testNum; k++) {
//                        System.out.println("�ڡ�" + k + "�������Ž�");
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
//            workbookA.write();//д������?? ??? ?
//            workbookA.close(); //�ر�����
//
//        } catch (Exception e) {
//            System.out.println("�ļ�д��ʧ�ܣ����쳣...");
//        }
//    }
//}
