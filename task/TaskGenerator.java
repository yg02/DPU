package task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class TaskGenerator {
    private static String path= "src/testingInstances/Tasks/";
    private int taskNum;
    private int instanceNum;

    public TaskGenerator(int taskNum, int instanceNum) throws IOException {
        this.taskNum = taskNum;
        this.instanceNum = instanceNum;
        generateTask();
    }

    public void generateTask() throws IOException {

        DecimalFormat format = new DecimalFormat("0.0000");

        String taskNumString = String.valueOf(this.taskNum);

        for(int i = 0; i < this.instanceNum; i++){
            String fileName = path + taskNumString + "/" + taskNumString + "_" + i + ".txt";
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
            double[] dataSize = new double[this.taskNum];
            double[] computationAmountCPU = new double[this.taskNum];
            double[] computationAmountDPU = new double[this.taskNum];
            double[] computationAmountMobile = new double[this.taskNum];

            double q = 0.45-0.2*Math.random(); //能耗限制参数 [0.25 , 0.45]
            writer.write(q +"\r\n");
            writer.flush();

            for (int j = 0; j < this.taskNum; j++){
                //数据量(0,2000]bit, 计算量(0,1595]cycles/bit
                //数据量，DPU计算量，CPU计算量，移动设备计算量
                dataSize[j] = Double.parseDouble(format.format(209715200 - Math.random()*157286400)); //50~200 Mb   209715200 - Math.random()*157286400
                computationAmountDPU[j] = Double.parseDouble(format.format(500 - Math.random()*300));//200~500 cycle/bit
                double p = 3-1*Math.random(); //2~3
                computationAmountCPU[j] = Double.parseDouble(format.format(computationAmountDPU[j]*p)); //DPU*p  500~1500
                computationAmountMobile[j] = Double.parseDouble(format.format(
                        (computationAmountDPU[j] + computationAmountCPU[j])/1.33)); //(CPU + DPU)/fMobile

                writer.write(j + "\t"+ dataSize[j] + "\t"+ computationAmountCPU[j]
                        + "\t"+ computationAmountDPU[j]+ "\t"+ computationAmountMobile[j]+"\r\n");
                writer.flush();
            }
            writer.close();
        }
    }

    public static void main(String[] args) throws IOException {
        int[] taskNums = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        //int[] taskNums = {10};
        int instanceNum = 10;
        for (int i = 0; i < taskNums.length; i++){
            TaskGenerator taskGenerator = new TaskGenerator(taskNums[i],instanceNum);
        }
    }
}
