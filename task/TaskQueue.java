package task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TaskQueue {

    private int taskNum;        //��������
    private int taskNumMobile;  //�ƶ��豸��������
    private int taskNumServer;  //��������������

    private ArrayList<Task> task; //�������
    private ArrayList<Task> taskM;//�ƶ��豸���������
    private ArrayList<Task> taskS;//���������������

    private double timeMobile; //�ƶ��豸��������н���ʱ��
    private double timeServer; //��������������н���ʱ��
    private double timeDone;   //�������ʱ��

    private double Esum;    //ϵͳ���ܺ�
    private double Emax;    //ϵͳ����ܺ�Լ��

    private int serverNum; //��������
    private int dpuNum;    //CPU��
    private int cpuNum;    //CPU��

    private double DPU[][];  //�б�ʾ���������б�ʾ�÷������µ�DPU��ֵΪDPU�����������ʱ���
    private double CPU[][];  //�б�ʾ���������б�ʾ�÷������µ�CPU��ֵΪCPU�����������ʱ���

    private double q;  //�ܺ����Ʋ��� [0.25 , 0.45]
    private int breakNum;  //�ܺĳ�������  ��/�� 1/0

    private int queueID; //���б�ţ�������ţ�

    public TaskQueue(int taskNum, int instanceID, DpuSystem sys) throws IOException {

        this.taskNum = taskNum;
        this.taskNumMobile = 0;
        this.taskNumServer = 0;

        this.task = new ArrayList<Task>();
        generateTask(task, taskNum, instanceID, sys);
        this.taskM = new ArrayList<Task>();
        this.taskS = new ArrayList<Task>();

        this.timeMobile = 0;
        this.timeServer = 0;
        this.timeDone = 0;

        this.Esum = 0;
        //this.Emax = sys.Emax; //��change��

        this.serverNum = sys.getServerNum();
        this.dpuNum = sys.getDpuNum();
        this.cpuNum = sys.getCpuNum();

        this.DPU = new double[serverNum][dpuNum];
        this.CPU = new double[serverNum][cpuNum];

        this.breakNum = 0;
        this.queueID = 0;
    }

    public void generateTask(ArrayList<Task> task, int taskNum, int instanceID, DpuSystem sys) throws IOException {

        String taskNumString = String.valueOf(this.taskNum);

        String fileName = "src/testingInstances/Tasks/" + taskNumString + "/" + taskNumString + "_" + instanceID + ".txt";
        File file = new File(fileName);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);

        //System.out.println("----------task��"+ taskNumString + "_" + instanceID + "��--------------");

        String line;

        line = br.readLine();
        q = Double.parseDouble(line); //�ܺ����Ʋ���
        //System.out.println(q);

        for(int i = 0;(line = br.readLine()) != null; i++){
            //System.out.println(line);
            String[] str = line.split("\t");
            double dataSize = Double.parseDouble(str[1]);
            double computationCPU = Double.parseDouble(str[2]);
            double computationDPU = Double.parseDouble(str[3]);
            double computationMobile = Double.parseDouble(str[4]);

            Task taskTempt = new Task(i, dataSize, computationCPU,computationDPU,computationMobile,sys);
            task.add(taskTempt);

//            System.out.println("-----����"+i+":-----");
//            System.out.println("����ʱ��:  "+ task.get(i).getdurationTrans());
//            System.out.println("����ʱ��:  "+ task.get(i).getdurationMobile());
//            System.out.println("�����ܺ�:  "+ task.get(i).getEtrans());
//            System.out.println("�����ܺ�:  "+ task.get(i).getEmobile());


//            double t = task[i].durationCPU + task[i].durationDPU + task[i].durationTrans;
//            System.out.println("������ʱ��:  "+ t);

        }
        br.close();
    }

    public void getTimeDone(int taskNum) {

        double Eworst = 0; //�����ܺ�Լ����

        for(int i = 0; i < taskNum; i++) {
            //�����������У�ѡ��ǰ�ܺ���С�ķ�ʽ���ɳ�ʼ�������
            if(task.get(i).getEtrans() < task.get(i).getEmobile()) {
                taskS.add(task.get(i));  //������ӵ�����������
                taskNumServer++;
                Esum += task.get(i).getEtrans(); //����ϵͳ���ܺ�

                Eworst += task.get(i).getEmobile();//�����ܺ�Լ����
            }
            else {
                taskM.add(task.get(i));  //������ӵ��ƶ��豸����
                taskNumMobile++;
                Esum += task.get(i).getEmobile(); //����ϵͳ���ܺ�
                timeMobile += task.get(i).getdurationMobile(); //�����ƶ��豸������н���ʱ��

                Eworst += task.get(i).getEtrans();//�����ܺ�Լ����
            }
        }

        //System.out.println("����ܺģ�" + Eworst);
        //q = 0.35;
        Emax = Eworst*q; //ϵͳ�ܺ�����


        if(taskNumServer > 0){ //�������Ͽ���һ������Ҳû�У�������change��
            taskS.get(0).setTimeTrans(taskS.get(0).getdurationTrans()) ; //��ʼ����һ������
            timeServer = getTimeServer(taskS.get(0));
        }

        for(int i = 1; i < taskNumServer; i++) {
            taskS.get(i).setTimeTrans(taskS.get(i-1).getTimeTrans() + taskS.get(i).getdurationTrans()); //�����ϴ�����ʱ���
            timeServer = getTimeServer(taskS.get(i));  //���·�����������н���ʱ��
        }

//        System.out.println("����ʼ״̬����");
//        System.out.println("�ƶ��豸������:  " + taskNumMobile);
//        System.out.println("������������:  " + taskNumServer);
//        System.out.println("timeMobile:  " + timeMobile);
//        System.out.println("timeServer:  " + timeServer);
//        System.out.println("Esum:        " + Esum);
//        System.out.println("Emax:        " + Emax);

        if(timeMobile == timeServer){ //�����г������Ϊ��������
            timeDone = timeMobile;
            return ;
        }

        else if(timeMobile > timeServer) {  //�������ɳ����е����̶�����
            timeDone = timeMobile;
            for(int i = taskNumMobile-1; i >= 0; i--) {
                //�����ƶ��豸��������е����񣬽������ж�ص�������������
                double Etemp = Esum - taskM.get(i).getEmobile() + taskM.get(i).getEtrans(); //��ǰϵͳ�����ܺ�

                if(Etemp > Emax){
                    //System.out.println();
                    //System.out.println("�ܺ�>Emax  " + Emax + "  !!!!!!!!!!");
                    //System.out.println();
                    breakNum = 1;
                    break; //�ܺĳ������ƣ�ֹͣж��
                }

                else {
                    //timeMobile -= taskM[i].durationMobile; //�����ƶ��豸������н���ʱ�䡾delete��
                    double timeMobileTempt = 0;//��ʱ�����ݴ���Ⱥ�ʱ�䡾change��
                    if(i != 0) //���һ������Ҫ���㣬���ߺ�timeMobile=0
                        timeMobileTempt = timeMobile - taskM.get(i).getdurationMobile();

                    //taskS[taskNumServer++] = taskM[i]; //�����������
                    //taskNumMobile--;
                    taskS.add(taskM.get(i));
                    taskNumServer++;
                    taskM.remove(i);
                    taskNumMobile--;

                    double timeServerTempt = timeServer;   //��ʱ�����ݴ����ǰʱ�䡾change��
                    timeServer = getTimeServer(taskM.get(i));  //���·�����������н���ʱ��

                    double timeTemp = Math.max(timeMobileTempt, timeServer); //��ǰ�������ʱ��
                    if(timeTemp <= timeDone) { //��Ϊ<=�����Ⱥ����ʱ�䲻�䣬ֻҪʱ��δ���Ӿ�Ӧ�������ȣ�����������change��
                        timeMobile = timeMobileTempt; //�����ƶ��豸������н���ʱ��
                        timeDone = timeTemp;   //�����������ʱ��
                        Esum = Etemp; //����ϵͳ�ܺ�

//                        System.out.println("-----move M to S-----");
//                        System.out.println("��tempt���ƶ��豸������:  " + taskNumMobile);
//                        System.out.println("��tempt��������������:  " + taskNumServer);
//                        System.out.println("��tempt��timeMobile:  " + timeMobile);
//                        System.out.println("��tempt��timeServer:  " + timeServer);
                    }
                    else{
                        //taskM[taskNumMobile++] = taskS[i]; //�ָ�������С�change��
                        //taskNumServer--; //��change��
                        taskM.add(taskS.get(taskNumServer-1));
                        taskNumMobile++;
                        taskS.remove(taskNumServer-1);
                        taskNumServer--;

                        timeServer = timeServerTempt; //�ָ�������������н���ʱ�䡾change��
                        break; //���Ⱥ���ʱ�����ӣ�ֹͣж��
                    }
                }
            }
        }
        else {
            timeDone = timeServer;
            for(int i = taskNumServer-1; i >= 0; i--) {
                //������������������е����񣬽�������ƶ����ƶ��豸������
                double Etemp = Esum - taskS.get(i).getEtrans() + taskS.get(i).getEmobile();  //��ǰϵͳ�����ܺ�

                if(Etemp > Emax){
                    //System.out.println();
                    //System.out.println("�ܺ�>Emax  " + Emax + "  !!!!!!!!!!");
                    //System.out.println();
                    breakNum = 1;
                    break; //�ܺĳ������ƣ�ֹͣж��
                }
                else {
                    //timeMobile += taskS[i].durationMobile; //�����ƶ��豸������н���ʱ�䡾delete��
                    double timeMobileTempt = timeMobile + taskS.get(i).getdurationMobile(); //��ʱ�����ݴ���Ⱥ�ʱ�䡾change��
                    //taskM[taskNumMobile++] = taskS[i]; //�����������
                    //taskNumServer--;
                    taskM.add(taskS.get(i));
                    taskNumMobile++;
                    taskS.remove(i);
                    taskNumServer--;

//                    //ֱ�Ӽ�����CPU��DPUʱ�������
//                    //timeServer =0; //��delete��
//                    double timeServerTempt = 0; //��ʱ�����ݴ���Ⱥ�ʱ�䡾change��
//                    DPU[taskS[i].serverID][taskS[i].dpuID] -= taskS[i].durationDPU;
//                    CPU[taskS[i].serverID][taskS[i].cpuID] -= taskS[i].durationCPU;
//                    for(int j=0;j<serverNum;j++) {  //�������·�����������н���ʱ��
//                        for(int l=0;l<serverNum;l++)
//                            if(CPU[j][l]> timeServerTempt)
//                                timeServerTempt = CPU[j][l];
//                    }

                    double timeServerTempt = timeServer; //��ʱ�����ݴ����ǰʱ�䡾change��
                    timeServer  = 0; //timeServerҪ��0��������change��
                    DPU = new double[serverNum][dpuNum]; //������顾change��
                    CPU = new double[serverNum][cpuNum]; //������顾change��

                    if(i != 0) //���һ������������Ҫ���㣬���ߺ�timeServer=0��������change��
                        timeServer = getTimeServer(taskS.get(0));
                    for(int j = 1; j < taskNumServer; j++) {
                        //���·�����������н���ʱ�䣬ȫ���㣬ֱ�Ӽ�����CPU��DPU����ʱ������ɾ�
                        taskS.get(j).setTimeTrans(taskS.get(j-1).getTimeTrans() + taskS.get(j).getdurationTrans());
                        timeServer = getTimeServer(taskS.get(j));
                    }

                    double timeTemp = Math.max(timeMobileTempt, timeServer); //��ǰ�������ʱ��
                    if(timeTemp <= timeDone) { //��Ϊ<=�����Ⱥ����ʱ�䲻�䣬ֻҪʱ��δ���Ӿ�Ӧ�������ȣ�����������change��
                        timeMobile = timeMobileTempt; //�����ƶ��豸������н���ʱ��
                        //timeServer = timeServerTempt; //���·�����������н���ʱ��
                        timeDone = timeTemp;   //�����������ʱ��
                        Esum = Etemp; //����ϵͳ�ܺ�

//                        System.out.println("-----move S to M-----");
//                        System.out.println("��tempt���ƶ��豸������:  " + taskNumMobile);
//                        System.out.println("��tempt��������������:  " + taskNumServer);
//                        System.out.println("��tempt��timeMobile:  " + timeMobile);
//                        System.out.println("��tempt��timeServer:  " + timeServer);
                    }
                    else{
                        //taskS[taskNumServer++] = taskM[i]; //�ָ�������С�change��
                        //taskNumMobile--; //��change��
                        taskS.add(taskM.get(taskNumMobile-1));
                        taskNumServer++;
                        taskM.remove(taskNumMobile-1);
                        taskNumMobile--;

                        timeServer = timeServerTempt; //�ָ�������������н���ʱ�䡾change��
                        break; //���Ⱥ���ʱ�����ӣ�ֹͣж��
                    }
                }
            }
        }
    }

    public double getTimeServer(Task t) {

        int dpuDone = 0; //��¼DPU�Ƿ���������
        int cpuDone = 0; //��¼CPU�Ƿ���������

        double minDpuTime = 99999; //��ǰ���DPU�������ʱ��
        double minCpuTime = 99999; //��ǰ���CPU�������ʱ��

        int minServerID = 0; //��ǰ�������ʱ�����DPU���ڷ��������
        int minDpuID = 0; //��ǰ�������ʱ�����DPU���
        int minCpuID = 0; //��ǰ�������ʱ�����CPU���

        for(int s = 0; s < serverNum; s++) {         //����������
            minDpuTime = 99999;
            for(int i = 0; i < 2; i++) {     //����DPU

                if(DPU[s][i] <= t.getTimeTrans()) {  //���DPUi����, ����DPU���鼰DPU����״̬
                    t.setTimeDPU(t.getTimeTrans() + t.getDurationDPU());
                    DPU[s][i] = t.getTimeDPU();
                    t.setServerID(s);
                    t.setDpuID(i);;
                    dpuDone = 1;
                }
                else if(DPU[s][i] < minDpuTime) { //���DPUi��æ���������DPU�������ʱ�估�豸���
                    minServerID = s;
                    minDpuID = i;
                    minDpuTime = DPU[s][i];
                }

                if(dpuDone == 1) {  //������������DPU����
                    minCpuTime = 99999;
                    for(int j = 0; j < cpuNum; j++) {  //����CPU

                        if(CPU[s][j] <= t.getTimeDPU()) {  //���CPUj����, ����CPU���鼰CPU����״̬
                            t.setTimeCPU(t.getTimeDPU() + t.getDurationCPU());
                            CPU[s][j] = t.getTimeCPU();
                            t.setCpuID(j);
                            cpuDone = 1;

                            if(t.getTimeCPU() > timeServer)
                                timeServer = t.getTimeCPU();
                            break; //�����������ֹͣ����CPU
                        }
                        else if(CPU[s][j] < minCpuTime) { //���DPUi��æ���������CPU�������ʱ�估�豸���
                            minCpuID = j;
                            minCpuTime = CPU[s][j];
                        }
                    }
                    if(cpuDone == 0) { //���������s��û�п���CPU
                        t.setTimeCPU(CPU[s][minCpuID] + t.getDurationCPU());  //Ϊ����ѡ����������һ�������CPU
                        CPU[s][minCpuID] = t.getTimeCPU();
                        t.setCpuID(minCpuID);
                        cpuDone = 1;

                        if(t.getTimeCPU() > timeServer)
                            timeServer = t.getTimeCPU();
                    }
                }
                if(cpuDone == 1)
                    break; //ֹͣ����DPU
            }
            if(cpuDone == 1)
                break; //ֹͣ����������
        }
        if(dpuDone == 0) {  //�����ǰû�п���DPU
            t.setTimeDPU(DPU[minServerID][minDpuID] + t.getDurationDPU());  //Ϊ����ѡ����������һ�������DPU
            DPU[minServerID][minDpuID] = t.getTimeDPU();
            t.setServerID(minServerID);
            t.setDpuID(minDpuID);
            minCpuTime = 99999;
            for(int j = 0; j < cpuNum; j++) {  //����CPU

                if(CPU[minServerID][j] <= t.getTimeDPU()) {  //���CPUj����, ����CPU���鼰CPU����״̬
                    t.setTimeCPU(t.getTimeDPU() + t.getDurationCPU());
                    CPU[minServerID][j] = t.getTimeCPU();
                    t.setCpuID(j);
                    cpuDone = 1;

                    if(t.getTimeCPU() > timeServer)
                        timeServer = t.getTimeCPU();
                    break; //�����������ֹͣ����CPU
                }
                else if(CPU[minServerID][j] < minCpuTime) { //���DPUi��æ���������CPU�������ʱ�估�豸���
                    minCpuID = j;
                    minCpuTime = CPU[minServerID][j];
                }
            }
            if(cpuDone == 0) { //���������s��û�п���CPU
                t.setTimeCPU(CPU[minServerID][minCpuID] + t.getDurationCPU()); ;  //Ϊ����ѡ����������һ�������CPU
                CPU[minServerID][minCpuID] = t.getTimeCPU();
                t.setCpuID(minCpuID);

                if(t.getTimeCPU() > timeServer)
                    timeServer = t.getTimeCPU();
            }
        }
        return timeServer;
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    public int getTaskNumMobile() {
        return taskNumMobile;
    }

    public void setTaskNumMobile(int taskNumMobile) {
        this.taskNumMobile = taskNumMobile;
    }

    public int getTaskNumServer() {
        return taskNumServer;
    }

    public void setTaskNumServer(int taskNumServer) {
        this.taskNumServer = taskNumServer;
    }

    public ArrayList<Task> getTask() {
        return task;
    }

    public void setTask(ArrayList<Task> task) {
        this.task = task;
    }

    public ArrayList<Task> getTaskM() {
        return taskM;
    }

    public void setTaskM(ArrayList<Task> taskM) {
        this.taskM = taskM;
    }

    public ArrayList<Task> getTaskS() {
        return taskS;
    }

    public void setTaskS(ArrayList<Task> taskS) {
        this.taskS = taskS;
    }

    public double getTimeMobile() {
        return timeMobile;
    }

    public void setTimeMobile(double timeMobile) {
        this.timeMobile = timeMobile;
    }

    public double getTimeServer() {
        return timeServer;
    }

    public void setTimeServer(double timeServer) {
        this.timeServer = timeServer;
    }

    public double getTimeDone() {
        return timeDone;
    }

    public void setTimeDone(double timeDone) {
        this.timeDone = timeDone;
    }

    public double getEsum() {
        return Esum;
    }

    public void setEsum(double esum) {
        Esum = esum;
    }

    public double getEmax() {
        return Emax;
    }

    public void setEmax(double emax) {
        Emax = emax;
    }

    public int getServerNum() {
        return serverNum;
    }

    public void setServerNum(int serverNum) {
        this.serverNum = serverNum;
    }

    public int getDpuNum() {
        return dpuNum;
    }

    public void setDpuNum(int dpuNum) {
        this.dpuNum = dpuNum;
    }

    public int getCpuNum() {
        return cpuNum;
    }

    public void setCpuNum(int cpuNum) {
        this.cpuNum = cpuNum;
    }

    public double[][] getDPU() {
        return DPU;
    }

    public void setDPU(double[][] DPU) {
        this.DPU = DPU;
    }

    public double[][] getCPU() {
        return CPU;
    }

    public void setCPU(double[][] CPU) {
        this.CPU = CPU;
    }

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public int getBreakNum() {
        return breakNum;
    }

    public void setBreakNum(int breakNum) {
        this.breakNum = breakNum;
    }

    public int getQueueID() {
        return queueID;
    }

    public void setQueueID(int queueID) {
        this.queueID = queueID;
    }

}
