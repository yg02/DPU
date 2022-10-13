package task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TaskQueue {

    private int taskNum;        //任务总数
    private int taskNumMobile;  //移动设备上任务数
    private int taskNumServer;  //服务器上任务数

    private ArrayList<Task> task; //任务队列
    private ArrayList<Task> taskM;//移动设备上任务队列
    private ArrayList<Task> taskS;//服务器上任务队列

    private double timeMobile; //移动设备上任务队列结束时间
    private double timeServer; //服务器上任务队列结束时间
    private double timeDone;   //任务完成时间

    private double Esum;    //系统总能耗
    private double Emax;    //系统最大能耗约束

    private int serverNum; //服务器数
    private int dpuNum;    //CPU数
    private int cpuNum;    //CPU数

    private double DPU[][];  //行表示服务器，列表示该服务器下的DPU，值为DPU上任务处理结束时间点
    private double CPU[][];  //行表示服务器，列表示该服务器下的CPU，值为CPU上任务处理结束时间点

    private double q;  //能耗限制参数 [0.25 , 0.45]
    private int breakNum;  //能耗超出限制  是/否 1/0

    private int queueID; //队列编号（个体序号）

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
        //this.Emax = sys.Emax; //【change】

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

        //System.out.println("----------task【"+ taskNumString + "_" + instanceID + "】--------------");

        String line;

        line = br.readLine();
        q = Double.parseDouble(line); //能耗限制参数
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

//            System.out.println("-----任务"+i+":-----");
//            System.out.println("传输时间:  "+ task.get(i).getdurationTrans());
//            System.out.println("计算时间:  "+ task.get(i).getdurationMobile());
//            System.out.println("传输能耗:  "+ task.get(i).getEtrans());
//            System.out.println("计算能耗:  "+ task.get(i).getEmobile());


//            double t = task[i].durationCPU + task[i].durationDPU + task[i].durationTrans;
//            System.out.println("服务器时间:  "+ t);

        }
        br.close();
    }

    public void getTimeDone(int taskNum) {

        double Eworst = 0; //计算能耗约束用

        for(int i = 0; i < taskNum; i++) {
            //遍历任务序列，选择当前能耗最小的方式生成初始任务队列
            if(task.get(i).getEtrans() < task.get(i).getEmobile()) {
                taskS.add(task.get(i));  //任务添加到服务器队列
                taskNumServer++;
                Esum += task.get(i).getEtrans(); //更新系统总能耗

                Eworst += task.get(i).getEmobile();//计算能耗约束用
            }
            else {
                taskM.add(task.get(i));  //任务添加到移动设备队列
                taskNumMobile++;
                Esum += task.get(i).getEmobile(); //更新系统总能耗
                timeMobile += task.get(i).getdurationMobile(); //更新移动设备任务队列结束时间

                Eworst += task.get(i).getEtrans();//计算能耗约束用
            }
        }

        //System.out.println("最大能耗：" + Eworst);
        //q = 0.35;
        Emax = Eworst*q; //系统能耗限制


        if(taskNumServer > 0){ //服务器上可能一个任务也没有！！！【change】
            taskS.get(0).setTimeTrans(taskS.get(0).getdurationTrans()) ; //初始化第一个任务
            timeServer = getTimeServer(taskS.get(0));
        }

        for(int i = 1; i < taskNumServer; i++) {
            taskS.get(i).setTimeTrans(taskS.get(i-1).getTimeTrans() + taskS.get(i).getdurationTrans()); //计算上传结束时间点
            timeServer = getTimeServer(taskS.get(i));  //更新服务器任务队列结束时间
        }

//        System.out.println("【初始状态】：");
//        System.out.println("移动设备任务数:  " + taskNumMobile);
//        System.out.println("服务器任务数:  " + taskNumServer);
//        System.out.println("timeMobile:  " + timeMobile);
//        System.out.println("timeServer:  " + timeServer);
//        System.out.println("Esum:        " + Esum);
//        System.out.println("Emax:        " + Emax);

        if(timeMobile == timeServer){ //两队列长度相等为理想最优
            timeDone = timeMobile;
            return ;
        }

        else if(timeMobile > timeServer) {  //将任务由长队列调到短队列中
            timeDone = timeMobile;
            for(int i = taskNumMobile-1; i >= 0; i--) {
                //遍历移动设备任务队列中的任务，将其逐个卸载到服务器队列中
                double Etemp = Esum - taskM.get(i).getEmobile() + taskM.get(i).getEtrans(); //当前系统整体能耗

                if(Etemp > Emax){
                    //System.out.println();
                    //System.out.println("能耗>Emax  " + Emax + "  !!!!!!!!!!");
                    //System.out.println();
                    breakNum = 1;
                    break; //能耗超出限制，停止卸载
                }

                else {
                    //timeMobile -= taskM[i].durationMobile; //更新移动设备任务队列结束时间【delete】
                    double timeMobileTempt = 0;//临时变量暂存调度后时间【change】
                    if(i != 0) //最后一个任务不要计算，调走后timeMobile=0
                        timeMobileTempt = timeMobile - taskM.get(i).getdurationMobile();

                    //taskS[taskNumServer++] = taskM[i]; //更新任务队列
                    //taskNumMobile--;
                    taskS.add(taskM.get(i));
                    taskNumServer++;
                    taskM.remove(i);
                    taskNumMobile--;

                    double timeServerTempt = timeServer;   //临时变量暂存调度前时间【change】
                    timeServer = getTimeServer(taskM.get(i));  //更新服务器任务队列结束时间

                    double timeTemp = Math.max(timeMobileTempt, timeServer); //当前任务完成时间
                    if(timeTemp <= timeDone) { //改为<=，调度后如果时间不变，只要时长未增加就应继续调度！！！！！【change】
                        timeMobile = timeMobileTempt; //更新移动设备任务队列结束时间
                        timeDone = timeTemp;   //更新任务完成时间
                        Esum = Etemp; //更新系统能耗

//                        System.out.println("-----move M to S-----");
//                        System.out.println("【tempt】移动设备任务数:  " + taskNumMobile);
//                        System.out.println("【tempt】服务器任务数:  " + taskNumServer);
//                        System.out.println("【tempt】timeMobile:  " + timeMobile);
//                        System.out.println("【tempt】timeServer:  " + timeServer);
                    }
                    else{
                        //taskM[taskNumMobile++] = taskS[i]; //恢复任务队列【change】
                        //taskNumServer--; //【change】
                        taskM.add(taskS.get(taskNumServer-1));
                        taskNumMobile++;
                        taskS.remove(taskNumServer-1);
                        taskNumServer--;

                        timeServer = timeServerTempt; //恢复服务器任务队列结束时间【change】
                        break; //调度后导致时长增加，停止卸载
                    }
                }
            }
        }
        else {
            timeDone = timeServer;
            for(int i = taskNumServer-1; i >= 0; i--) {
                //遍历服务器任务队列中的任务，将其逐个移动到移动设备队列中
                double Etemp = Esum - taskS.get(i).getEtrans() + taskS.get(i).getEmobile();  //当前系统整体能耗

                if(Etemp > Emax){
                    //System.out.println();
                    //System.out.println("能耗>Emax  " + Emax + "  !!!!!!!!!!");
                    //System.out.println();
                    breakNum = 1;
                    break; //能耗超出限制，停止卸载
                }
                else {
                    //timeMobile += taskS[i].durationMobile; //更新移动设备任务队列结束时间【delete】
                    double timeMobileTempt = timeMobile + taskS.get(i).getdurationMobile(); //临时变量暂存调度后时间【change】
                    //taskM[taskNumMobile++] = taskS[i]; //更新任务队列
                    //taskNumServer--;
                    taskM.add(taskS.get(i));
                    taskNumMobile++;
                    taskS.remove(i);
                    taskNumServer--;

//                    //直接减导致CPU与DPU时间减不净
//                    //timeServer =0; //【delete】
//                    double timeServerTempt = 0; //临时变量暂存调度后时间【change】
//                    DPU[taskS[i].serverID][taskS[i].dpuID] -= taskS[i].durationDPU;
//                    CPU[taskS[i].serverID][taskS[i].cpuID] -= taskS[i].durationCPU;
//                    for(int j=0;j<serverNum;j++) {  //遍历更新服务器任务队列结束时间
//                        for(int l=0;l<serverNum;l++)
//                            if(CPU[j][l]> timeServerTempt)
//                                timeServerTempt = CPU[j][l];
//                    }

                    double timeServerTempt = timeServer; //临时变量暂存调度前时间【change】
                    timeServer  = 0; //timeServer要置0！！！【change】
                    DPU = new double[serverNum][dpuNum]; //清空数组【change】
                    CPU = new double[serverNum][cpuNum]; //清空数组【change】

                    if(i != 0) //最后一个服务器任务不要计算，调走后timeServer=0！！！【change】
                        timeServer = getTimeServer(taskS.get(0));
                    for(int j = 1; j < taskNumServer; j++) {
                        //更新服务器任务队列结束时间，全重算，直接减导致CPU与DPU空闲时间减不干净
                        taskS.get(j).setTimeTrans(taskS.get(j-1).getTimeTrans() + taskS.get(j).getdurationTrans());
                        timeServer = getTimeServer(taskS.get(j));
                    }

                    double timeTemp = Math.max(timeMobileTempt, timeServer); //当前任务完成时间
                    if(timeTemp <= timeDone) { //改为<=，调度后如果时间不变，只要时长未增加就应继续调度！！！！！【change】
                        timeMobile = timeMobileTempt; //更新移动设备任务队列结束时间
                        //timeServer = timeServerTempt; //更新服务器任务队列结束时间
                        timeDone = timeTemp;   //更新任务完成时间
                        Esum = Etemp; //更新系统能耗

//                        System.out.println("-----move S to M-----");
//                        System.out.println("【tempt】移动设备任务数:  " + taskNumMobile);
//                        System.out.println("【tempt】服务器任务数:  " + taskNumServer);
//                        System.out.println("【tempt】timeMobile:  " + timeMobile);
//                        System.out.println("【tempt】timeServer:  " + timeServer);
                    }
                    else{
                        //taskS[taskNumServer++] = taskM[i]; //恢复任务队列【change】
                        //taskNumMobile--; //【change】
                        taskS.add(taskM.get(taskNumMobile-1));
                        taskNumServer++;
                        taskM.remove(taskNumMobile-1);
                        taskNumMobile--;

                        timeServer = timeServerTempt; //恢复服务器任务队列结束时间【change】
                        break; //调度后导致时长增加，停止卸载
                    }
                }
            }
        }
    }

    public double getTimeServer(Task t) {

        int dpuDone = 0; //记录DPU是否处理完任务
        int cpuDone = 0; //记录CPU是否处理完任务

        double minDpuTime = 99999; //当前最短DPU处理结束时间
        double minCpuTime = 99999; //当前最短CPU处理结束时间

        int minServerID = 0; //当前处理结束时间最短DPU所在服务器编号
        int minDpuID = 0; //当前处理结束时间最短DPU编号
        int minCpuID = 0; //当前处理结束时间最短CPU编号

        for(int s = 0; s < serverNum; s++) {         //遍历服务器
            minDpuTime = 99999;
            for(int i = 0; i < 2; i++) {     //遍历DPU

                if(DPU[s][i] <= t.getTimeTrans()) {  //如果DPUi空闲, 更新DPU数组及DPU处理状态
                    t.setTimeDPU(t.getTimeTrans() + t.getDurationDPU());
                    DPU[s][i] = t.getTimeDPU();
                    t.setServerID(s);
                    t.setDpuID(i);;
                    dpuDone = 1;
                }
                else if(DPU[s][i] < minDpuTime) { //如果DPUi繁忙，更新最短DPU处理结束时间及设备编号
                    minServerID = s;
                    minDpuID = i;
                    minDpuTime = DPU[s][i];
                }

                if(dpuDone == 1) {  //如果任务已完成DPU处理
                    minCpuTime = 99999;
                    for(int j = 0; j < cpuNum; j++) {  //遍历CPU

                        if(CPU[s][j] <= t.getTimeDPU()) {  //如果CPUj空闲, 更新CPU数组及CPU处理状态
                            t.setTimeCPU(t.getTimeDPU() + t.getDurationCPU());
                            CPU[s][j] = t.getTimeCPU();
                            t.setCpuID(j);
                            cpuDone = 1;

                            if(t.getTimeCPU() > timeServer)
                                timeServer = t.getTimeCPU();
                            break; //任务处理结束，停止遍历CPU
                        }
                        else if(CPU[s][j] < minCpuTime) { //如果DPUi繁忙，更新最短CPU处理结束时间及设备编号
                            minCpuID = j;
                            minCpuTime = CPU[s][j];
                        }
                    }
                    if(cpuDone == 0) { //如果服务器s上没有空闲CPU
                        t.setTimeCPU(CPU[s][minCpuID] + t.getDurationCPU());  //为任务选择最快结束上一个任务的CPU
                        CPU[s][minCpuID] = t.getTimeCPU();
                        t.setCpuID(minCpuID);
                        cpuDone = 1;

                        if(t.getTimeCPU() > timeServer)
                            timeServer = t.getTimeCPU();
                    }
                }
                if(cpuDone == 1)
                    break; //停止遍历DPU
            }
            if(cpuDone == 1)
                break; //停止遍历服务器
        }
        if(dpuDone == 0) {  //如果当前没有空闲DPU
            t.setTimeDPU(DPU[minServerID][minDpuID] + t.getDurationDPU());  //为任务选择最快结束上一个任务的DPU
            DPU[minServerID][minDpuID] = t.getTimeDPU();
            t.setServerID(minServerID);
            t.setDpuID(minDpuID);
            minCpuTime = 99999;
            for(int j = 0; j < cpuNum; j++) {  //遍历CPU

                if(CPU[minServerID][j] <= t.getTimeDPU()) {  //如果CPUj空闲, 更新CPU数组及CPU处理状态
                    t.setTimeCPU(t.getTimeDPU() + t.getDurationCPU());
                    CPU[minServerID][j] = t.getTimeCPU();
                    t.setCpuID(j);
                    cpuDone = 1;

                    if(t.getTimeCPU() > timeServer)
                        timeServer = t.getTimeCPU();
                    break; //任务处理结束，停止遍历CPU
                }
                else if(CPU[minServerID][j] < minCpuTime) { //如果DPUi繁忙，更新最短CPU处理结束时间及设备编号
                    minCpuID = j;
                    minCpuTime = CPU[minServerID][j];
                }
            }
            if(cpuDone == 0) { //如果服务器s上没有空闲CPU
                t.setTimeCPU(CPU[minServerID][minCpuID] + t.getDurationCPU()); ;  //为任务选择最快结束上一个任务的CPU
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
