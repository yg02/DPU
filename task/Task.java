package task;

public class Task implements Cloneable{

    private double dataSize; //要处理的数据量
    private double computationDPU;  //解析该任务的每位数据所需要的服务器DPU周期数
    private double computationCPU;  //计算该任务的每位数据所需要的服务器CPU周期数
    private double computationMobile;  //执行该任务的每位数据所需要的移动设备CPU周期数

    private double durationTrans; //传输到服务器所需时间
    private double durationDPU;  //在DPU上处理所需时间
    private double durationCPU;  //在CPU上处理所需时间
    private double durationMobile;  //在移动设备上处理所需时间

    private double timeTrans; //上传结束时间点
    private double timeDPU;   //在DPU上处理结束时间点
    private double timeCPU;   //在CPU上处理结束时间点

    private double Etrans;  //传输能耗
    private double Emobile; //计算能耗

    private  int taskID;  //任务编号
    private int serverID; //所在服务器编号
    private int dpuID;    //所在DPU编号
    private int cpuID;    //所在CPU编号

    public Task(int taskID, double dataSize, double computationDPU, double computationCPU, double computationMobile, DpuSystem sys) {

        this.taskID = taskID;
        this.dataSize = dataSize;
        this.computationDPU = computationDPU;
        this.computationCPU = computationCPU;
        this.computationMobile = computationMobile;

        this.durationMobile = (dataSize*computationMobile)/sys.getfMobile();
        this.durationTrans = dataSize/sys.getR();
        this.durationDPU = (dataSize*computationDPU)/sys.getfDPU();
        this.durationCPU = (dataSize*computationCPU)/sys.getfCPU();

        this.Emobile = this.durationMobile*sys.getpMobile();
        this.Etrans = this.durationTrans*sys.getpTrans();

    }

    public Object clone(){

        Task task = null;
        try {
            task = (Task) super.clone();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return task;
    }

    public double getDataSize() {
        return dataSize;
    }

    public void setDataSize(double dataSize) {
        this.dataSize = dataSize;
    }

    public double getComputationDPU() {
        return computationDPU;
    }

    public void setComputationDPU(double computationDPU) {
        this.computationDPU = computationDPU;
    }

    public double getComputationCPU() {
        return computationCPU;
    }

    public void setComputationCPU(double computationCPU) {
        this.computationCPU = computationCPU;
    }

    public double getComputationMobile() {
        return computationMobile;
    }

    public void setComputationMobile(double computationMobile) {
        this.computationMobile = computationMobile;
    }

    public double getDurationDPU() {
        return durationDPU;
    }

    public void setDurationDPU(double durationDPU) {
        this.durationDPU = durationDPU;
    }

    public double getDurationCPU() {
        return durationCPU;
    }

    public void setDurationCPU(double durationCPU) {
        this.durationCPU = durationCPU;
    }

    public double getdurationTrans() {
        return durationTrans;
    }

    public void setdurationTrans(double durationTrans) {
        this.durationTrans = durationTrans;
    }

    public double getdurationMobile() {
        return durationMobile;
    }

    public void setdurationMobile(double durationMobile) {
        this.durationMobile = durationMobile;
    }

    public double getTimeTrans() {
        return timeTrans;
    }

    public void setTimeTrans(double timeTrans) {
        this.timeTrans = timeTrans;
    }

    public double getTimeCPU() {
        return timeCPU;
    }

    public void setTimeCPU(double timeCPU) {
        this.timeCPU = timeCPU;
    }

    public double getTimeDPU() {
        return timeDPU;
    }

    public void setTimeDPU(double timeDPU) {
        this.timeDPU = timeDPU;
    }

    public double getEtrans() {
        return Etrans;
    }

    public void setEtrans(double etrans) {
        Etrans = etrans;
    }

    public double getEmobile() {
        return Emobile;
    }

    public void setEmobile(double emobile) {
        Emobile = emobile;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public int getDpuID() {
        return dpuID;
    }

    public void setDpuID(int dpuID) {
        this.dpuID = dpuID;
    }

    public int getCpuID() {
        return cpuID;
    }

    public void setCpuID(int cpuID) {
        this.cpuID = cpuID;
    }
}

