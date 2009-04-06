package myctapp;

import java.util.Random;

/**
 * Poisson.java
 *
 *
 * Created: Wed Feb 05 10:03:56 2003
 *
 * @author Todd W. Neller
 * @version
 */

public class Poisson1{
    public static void main(String[] args){

	System.out.print("Expected number: ");
	int lambda = 1;
	System.out.print("Trials: ");
	int trials = 300;

	Random rng = new Random();
	int total = 0;
	for (int i=0; i<trials; i++) {
	    double randNum = rng.nextDouble();
	    double probTerm = Math.exp(-lambda); // probability of 0
	    double probSum = probTerm;
	    int sampleNum = 0;
	    while (randNum >= probSum) {
		sampleNum++;
		// compute probability of n from prob. of n-1
		probTerm *= (double)lambda / sampleNum;
		// add to sum
		probSum += probTerm;
	    }
	    System.out.println(sampleNum);
	    total += sampleNum;
	}
	System.out.println("Sample average: " 
			   + ((double)total / trials));
    }
} // Poisson