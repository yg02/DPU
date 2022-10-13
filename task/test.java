package task;

import org.apache.poi.util.SystemOutLogger;

import java.io.PrintStream;

/**
 * @author YG
 * @create 2022-04-25 16:32
 */
public class test {
    public static void main(String[] args) {

        double[] NEDA = {3318.52, 3701.58, 4139.84, 4608.8, 5064.28, 5568.94, 6122.16, 6706.34, 7396.64, 7919.94};
        double[] GA = {2438.06, 2897.92, 3366.48, 3873.92, 4381.62, 4929.76, 5520.94, 6138.7, 6635.02, 7300.18};
        double[] PSO = {2739.44, 3392.22, 4082.42, 4756.74, 5565.54, 6426.36, 7120.52, 8150.68, 9559.52, 10718.12};
        double[] FA = {2762.8, 3282.76, 4079.74, 4768.76, 5548.12, 6487.5, 7647.24, 8460.7, 10099.80, 10886.06};

        double sum = 0.0;
        for (int i = 0; i < 10; i++) {
            double NE = NEDA[i];
            System.out.println(NE);
            sum += NE;
        }
        System.out.println(sum/10);
        System.out.println((6.4 - 6.25) / 6.4);
    }
}
