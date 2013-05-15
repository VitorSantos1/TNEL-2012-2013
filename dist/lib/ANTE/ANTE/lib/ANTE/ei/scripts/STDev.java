package ei.scripts;

import java.util.Vector;

/**
 * calculates the standard deviation
 * @author pbn
 * */

public class STDev {
    public static double computeSDDvt(Vector<Double> values, double mediaSum ) {
        double sum = 0;   
       
        for (Double value: values) {
            sum += Math.pow((value - mediaSum),2);
        }
        if(values.size() == 1)
        	return 0d;
        else
        	return Math.sqrt( sum / (values.size()-1) );
    }
}
