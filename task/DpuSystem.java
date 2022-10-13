package task;

import java.io.IOException;

public class DpuSystem {

    private double fMobile; //移动设备的CPU周期频率
    private double fDPU; //服务器DPU周期频率
    private double fCPU; //服务器CPU周期频率

    private double pTrans;  //传输功率
    private double pMobile; //计算功率
    private double R; //传输速率

    private double Emax; //系统最大能耗约束

    private int serverNum; //服务器数
    private int cpuNum; //CPU数
    private int dpuNum; //DPU数

    public DpuSystem(){
        this.fMobile = 1330000000.0;  //Hz
        this.fDPU = 2000000000.0;  //Hz
        this.fCPU = 3000000000.0;  //Hz

        this.pTrans = 0.1;  //100 mW
        this.pMobile = 0.1323;  //静态70mW + 动态62.3mW

        //this.Emax = 90000;

        this.serverNum = 2;
        this.dpuNum = 2;
        this.cpuNum = 16;

        double g0 = -40;    //单位：dB
        double d0 = 1;    //m
        double d = 100;   //m
        double theta = 4;
        double w = 20*Math.pow(10,6);    //Hz   1->20
        double N0 = -174;   //dBm/Hz
        double deviceTransmissionPower = 100; // mW  pTrans
        double tempG0 = Math.pow(10,g0/10);
        double tempN0 = Math.pow(10,N0/10);
        double temp1 = tempG0*deviceTransmissionPower*Math.pow(d0/d,theta);
        double temp2 = tempN0*w;
        double transmissionRate = w*(Math.log(1+temp1/temp2)/Math.log(2));
       // System.out.println(transmissionRate);
        this.R = transmissionRate; //23474615  bit/s

    }

    public static void main(String[] args) throws IOException {

        DpuSystem sys = new DpuSystem();

        //int[] taskNums = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        int[] taskNums = {10}; //个体任务数
        int instanceNum = 10; //测试样例数

        //int totalBreakNum = 0;

        for (int i = 0; i < taskNums.length; i++){

            //sys.Emax = taskNums[i] * 2.56*0.7; //Eworst*70%

            for (int j = 0; j <instanceNum; j++) {

                System.out.println("----------实例_" + j + "】--------------");

                TaskQueue queue = new TaskQueue(taskNums[i], j, sys);
                queue.getTimeDone(taskNums[i]);

                System.out.println("【更新后】：");
                System.out.println("移动设备任务数:  " + queue.getTaskNumMobile());
                System.out.println("服务器任务数:  " + queue.getTaskNumServer());
                System.out.println("timeMobile:  " + queue.getTimeMobile());
                System.out.println("timeServer:  " + queue.getTimeServer());
                System.out.println("Esum:        " + queue.getEsum());
//                //System.out.println("*序列完工时间*:  " + queue.timeDone);
//                System.out.println("【完工时间】:  " + queue.getTimeDone());
//
//                if(queue.getBreakNum() ==1)
//                    totalBreakNum++;

            }

//            System.out.println("超能队列数：  " + totalBreakNum);

        }
    }

    public double getfMobile() {
        return fMobile;
    }

    public void setfMobile(double fMobile) {
        this.fMobile = fMobile;
    }

    public double getfDPU() {
        return fDPU;
    }

    public void setfDPU(double fDPU) {
        this.fDPU = fDPU;
    }

    public double getfCPU() {
        return fCPU;
    }

    public void setfCPU(double fCPU) {
        this.fCPU = fCPU;
    }

    public double getpTrans() {
        return pTrans;
    }

    public void setpTrans(double pTrans) {
        this.pTrans = pTrans;
    }

    public double getpMobile() {
        return pMobile;
    }

    public void setpMobile(double pMobile) {
        this.pMobile = pMobile;
    }

    public double getR() {
        return R;
    }

    public void setR(double r) {
        R = r;
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

    public int getCpuNum() {
        return cpuNum;
    }

    public void setCpuNum(int cpuNum) {
        this.cpuNum = cpuNum;
    }

    public int getDpuNum() {
        return dpuNum;
    }

    public void setDpuNum(int dpuNum) {
        this.dpuNum = dpuNum;
    }
}

