package utils;

import java.util.List;

/**
 * Created by ngopalan on 11/8/16.
 */
public class SummaryStatistics {

    public static double mean(double[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }

    public static double mean(List<Double> m) {
        double sum = 0;
        for (int i = 0; i < m.size(); i++) {
            sum += m.get(i);
        }
        return sum / m.size();
    }

}
