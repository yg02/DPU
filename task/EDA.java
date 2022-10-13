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

        //TaskQueue originQueue = new TaskQueue(taskNum, instanceID, sys);//ԭʼ��������
        TaskQueue bestQueue = new TaskQueue(taskNum, instanceID, sys);//���Ž�, ��ʼΪԭʼ˳������
        //bestQueue.setTimeDone(9999999);
        ArrayList<TaskQueue> solution = new ArrayList<TaskQueue>();

        //���ɳ�ʼ��Ⱥ��������Ӧ��ֵ
        for (int i = 0; i < groupSize; i++) {
            TaskQueue tempQueue = new TaskQueue(taskNum, instanceID, sys);
            tempQueue.setQueueID(i);
            solution.add(tempQueue);

            for (int j = 0; j < taskNum; j++) {
                solution.get(i).getTask().set(j,bestQueue.getTask().get(taskOrder[i][j]));
            }
            solution.get(i).getTimeDone(taskNum);
            //print("-------���塾" + i + "��-------", solution.get(i), taskNum);
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
        Collections.sort(bestQueue.getTask(), new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return t1.getdurationMobile() > t2.getdurationMobile() ? 1 : -1;
            }
        });

        bestQueue.getTimeDone(taskNum);


        //bestQueue = clone(bestQueue, originQueue, taskNum, instanceID, sys);
        //bestQueue = clone(bestQueue, solution.get(0), taskNum, instanceID, sys);
        print("��ʼ���Ž⣺", bestQueue, taskNum);

        //��ʼ����
        for (int i = 0; i < loopNum; i++) {
            bestQueue = processEDA(solution, groupSize, taskNum, instanceID, i, bestQueue, sys);
        }

        //System.out.println("ȫ�����Ž⣺");
        //print(bestQueue, taskNum);

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




        solution.clear(); //�����Ⱥ

        for (int i = 0; i < groupSize; i++) {

            TaskQueue tempQueue = new TaskQueue(taskNum, instanceID, sys);
            tempQueue.setQueueID(i);
//            for (int k = 0; k < taskNum; k++) {
//                tempQueue.getTask().get(k).setTaskID(-1); //��Ǹ�λ���Ƿ����·�������
//            }

            TaskQueue bestQueueCopy = new TaskQueue(taskNum, instanceID, sys);
            bestQueueCopy = clone(bestQueueCopy, bestQueue, taskNum, instanceID, sys);

            //ӳ�����

            //System.out.println("----------�����Ӵ����塾" + i+ "��----------");
            tempQueue = mapOntoSolutionX(pro, i%taskNum, tempQueue, taskNum, bestQueueCopy);

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

        System.out.println("���º�");
        for (int i = 0; i < selectSize; i++) {
            System.out.print("���塾" + solution.get(i).getQueueID() + "��:  ");
            print(solution.get(i), taskNum);
        }

        //�������Ž�
        if(solution.get(0).getTimeDone() < bestQueue.getTimeDone()){
            bestQueue = clone(bestQueue, solution.get(0),taskNum, instanceID, sys);
            bestQueue.getTimeDone(taskNum);
            //System.out.println("----------��new��-----------------------------------------------------------------------");
        }

        System.out.println("�ڡ�" + loopNum + "�������Ž�");
        print(bestQueue, taskNum);

        return  bestQueue;
    }


    //�̶�����a�̳����Ž�, Ȼ����ʾ���̳����Ž⣬������
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

        //print("���Ž⣺", bestQueue, bestQueue.getTask().size());
        //print("temp��", tempQueue, taskNum);

        Collections.shuffle(bestQueue.getTask());

        //print("���Ž⣺", bestQueue, bestQueue.getTask().size());
        //System.out.println(bestQueue.getTask().size());

        for (int i = 0; i < remainLocation.size(); i++) {
            tempQueue.getTask().set(remainLocation.get(i), bestQueue.getTask().get(i));
        }

//        if(!bestQueue.getTask().isEmpty()){
//            //System.out.println(bestQueue.getTask().size());
//            int m = 0;
//
//            for (int i = 0; i < taskNum; i++) {
//                //System.out.println("��ǰ�������" + tempQueue.getTask().get(i).getTaskID() );
//                if(tempQueue.getTask().get(i).getTaskID() == -1 ){
//                    //System.out.println("m:" + m);
//                    tempQueue.getTask().set(i, bestQueue.getTask().get(m));
//                    m++;
//                }
//            }
//        }

        return tempQueue;
    }

    //���ƶ��� ����Ϊ��������ʱ�䣬ֻ�������У������������㣩��Ӧ��ֵ
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

                    long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��
                    bestQueue = processAlgorithm(groupSize, taskNum[i], j, loopNum, sys);
                    long endTime=System.currentTimeMillis() - startTime; //��ȡ����ʱ��

                    String timeDone = format.format(bestQueue.getTimeDone());

                    System.out.println("�ڡ�" + k + "�������Ž�" + timeDone);

                    writer.write( taskNum[i] + "\t" + j + "\t" + k + "\t" + timeDone  + "\t" + endTime + "\r\n");
                    writer.flush();

                }
            }

        }

        writer.close();
    }
}