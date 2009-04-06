package myctapp;

import java.util.Random;
import java.lang.Math;

/**Distribution.java
 * This class generates various random variables for distributions
 * not directly supported in Java
 */
public class Distribution extends Random {
    
    
    public static void main(String[] args) {
        
        int mean = 100;
        Distribution p_random = new Distribution();
        for (int i = 0; i < mean; i++) {

            System.out.println(p_random.nextPoisson(2));
        }
    
    } 
    
    
   public int nextPoisson(double lambda) {
        double elambda = Math.exp(-1*lambda);
        double product = 1;
        int count =  0;
        int result=0;
        while (product >= elambda) {
            product *= nextDouble();
            result = count;
            count++; // keep result one behind
        }
        return result;
    }
    
    public  double nextExponential(double b) {
        double randx;
        double result;
        randx = nextDouble();
        result = -1*b*Math.log(randx);
        return result;
    }
}
