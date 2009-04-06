package myctapp;


/**@author     Damien Farrell
 *@created    February 2004 */
/**provides methods to create frequency filters in 1D arrays */

public class Filters {
    
    public static double[] filter1(String filtname, int scans, double cutoff){
        int i=0;
        int Width = scans/2;
        double tau = Width*cutoff;
        double exponent;
        double[] filter = new double[scans];
        double PI = Math.PI;
        
        filter[0] = 0;
        if (filtname == "ramp"){
            for (i = 1; i <= Width; i++) {
                filter[scans - i] = filter[i] = (double) PI*i ;
            }
        } else if (filtname == "shepplogan"){
            for (i = 1; i <= Width; i++) {
                filter[scans - i] = filter[i] =  PI*i * ((Math.sin(PI*i/Width/2))/(PI*i/Width/2));
                
            }
        } else if (filtname == "hamming"){
            
            for (i = 1; i <= Width; i++) {
                if (i <= tau){
                    filter[scans - i] = filter[i] =  PI*i * (.54 + (.46 * Math.cos(PI*i/tau)));
                } else filter[scans - i] = filter[i] = 0;
            }
        } else if (filtname == "hann"){
            for (i = 1; i <= Width; i++) {
                if (i <= tau){
                    filter[scans - i] = filter[i] =  PI*i * (1 + (Math.cos(PI*i/tau)));
                } else filter[scans - i] = filter[i] = 0;
            }
        } else if (filtname == "cosine"){
            for (i = 1; i <= Width; i++) {
                if (i <= tau){
                    filter[scans - i] = filter[i] =  PI*i * (Math.cos(PI*i/tau/2));
                } else filter[scans - i] = filter[i] = 0;
            }
        } else if (filtname == "blackman"){
            for (i = 1; i <= Width; i++) {
                if (i <= tau){
                    filter[scans - i] = filter[i] = PI*i * (0.42 + (0.5 * Math.cos(PI*i/tau-1))
                                                      + (0.08 * Math.cos(2*PI*i/tau-1)));
                } else filter[scans - i] = filter[i] = 0;
            }
        }
        //normalizeData(filter, 1);
        setRange1DArray(filter,0,1);
        return filter;
    }
    
    
    public static double[]  ramlak(int width){
        
        int n = -(width-1)/2;
        int tap;
        double[] filter;
        filter = new double[width];
        
        for (tap=1;tap<width;tap++){
            if(n==0){
                filter[tap]=Math.PI/4 *100;
            } else if (n%2 == 1 || n%2 == -1){
                filter[tap]=-1/(Math.PI*n*n) *100;
            } else{
                filter[tap]=0;
            }
            n++;
            //System.out.print(filter[tap]+", ");
        }
        System.out.println();
        normalizeData(filter, 1);
        double[] idata = new double[width];
        Filters.FFT(1, width, filter, idata);
        return filter;
        
    }
    
    public static double[] shepplogan(int width){
        int n =0;
        int tap;
        double[] filter;
        filter = new double[width];
        n =-(width-1)/2;
        for (tap=1;tap<width;tap++){
            filter[tap] = -2/(Math.PI*(4*n*n-1)) *100;
            //System.out.print(filter[tap]+" ");
            n++;
        }
        
        System.out.println();
        
        return filter;
    }
    
    // http://www.developer.com/java/other/article.php/10936_1554511_1
    
    public static double[] convolve( double[] data, int dataLen,
            double[] operator, int operatorLen){
        double[] output = new double[dataLen];
        
        //Apply the operator to the data, dealing with the index
        // reversal required by convolution.
        for (int i=0; i < dataLen-operatorLen;i++){
            output[i] = 0;
            for (int j=operatorLen-1;j>=0;j--){
                output[i] += data[i+j]*operator[j];
            }
        }
        return output;
    }
    
    // delay is a hack; 1 for analysis, 0 for synthesis
    public static double[] convolve(double[] x, double[] h, int delay) {
        int min = Math.min(x.length, h.length) - 1;
        double[] y = new double[x.length + h.length - 1];
        for (int i = 0; i < y.length; i++) {
            y[i] = 0.0;
            for (int k = 0; k <= min; k++) {
                if (i - k + delay < x.length && i - k + delay >= 0) {
                    y[i] += h[k] * x[i - k + delay];
                }
            }
        }
        return y;
    }
    
    public static double[] dft(double[] data, double[] idata, int dataLen){
        //Set the frequency increment to the reciprocal of the data
        // length.  This is convenience only, and is not a requirement
        // of the DFT algorithm.
        
        double[] spectrum = new double[dataLen];
        double delF = 1.0/dataLen;
        //Outer loop iterates on frequency
        // values.
        for(int i=0; i < dataLen;i++){
            double freq = i*delF;
            double real = 0;
            double imag = 0;
            //Inner loop iterates on time-
            // series points.
            for(int j=0; j < dataLen; j++){
                real += data[j]*Math.cos( 2*Math.PI*freq*j);
                imag += data[j]*Math.sin( 2*Math.PI*freq*j);
            }
            spectrum[i] = Math.sqrt(real*real + imag*imag);
            idata[i] = -imag;
        }
        return  spectrum;
    }
    
    
    public static double[] idft(double[] data, double[] idata, int dataLen){
        
        double[] spectrum = new double[dataLen];
        
        for (int i=0;i<dataLen;i++) {
            idata[i] = - idata[i];
        }
        spectrum = dft(data, idata, dataLen);
        
        for (int i=0;i<dataLen;i++) {
            
            idata[i] = -idata[i];
            spectrum[i] /= dataLen;
        }
        return  spectrum;
    }
    
    
/*
   This computes an in-place complex-to-complex FFT
   x and y are the real and imaginary arrays of 2^m points.
   dir =  1 gives forward transform,  dir = -1 gives reverse transform
 */
    
    public static void FFT(int dir, int s, double[] x,double[] y) {
        int n, i, i1, j, k, i2, l, l1, l2;
        double c1, c2, tx, ty, t1, t2, u1, u2, z;
        int m = (int) (Math.log(s)/Math.log(2));
        double[] spectrum = new double[s];
        
        /* Calculate the number of points */
        n = 1;
        for (i=0;i<m;i++)
            n *= 2;
        
        /* Do the bit reversal */
        i2 = n >> 1;
        j = 0;
        for (i=0;i<n-1;i++) {
            if (i < j) {
                tx = x[i];
                ty = y[i];
                x[i] = x[j];
                y[i] = y[j];
                x[j] = tx;
                y[j] = ty;
            }
            k = i2;
            while (k <= j) {
                j -= k;
                k >>= 1;
            }
            j += k;
        }
        
        /* Compute the FFT */
        c1 = -1.0;
        c2 = 0.0;
        l2 = 1;
        for (l=0;l<m;l++) {
            l1 = l2;
            l2 <<= 1;
            u1 = 1.0;
            u2 = 0.0;
            for (j=0;j<l1;j++) {
                for (i=j;i<n;i+=l2) {
                    i1 = i + l1;
                    t1 = u1 * x[i1] - u2 * y[i1];
                    t2 = u1 * y[i1] + u2 * x[i1];
                    x[i1] = x[i] - t1;
                    y[i1] = y[i] - t2;
                    x[i] += t1;
                    y[i] += t2;
                }
                z =  u1 * c1 - u2 * c2;
                u2 = u1 * c2 + u2 * c1;
                u1 = z;
            }
            c2 = Math.sqrt((1.0 - c1) / 2.0);
            if (dir == 1)
                c2 = -c2;
            c1 = Math.sqrt((1.0 + c1) / 2.0);
        }
        
        /* Scaling for forward transform */
        if (dir == 1) {
            for (i=0;i<n;i++) {
                x[i] /= n;
                y[i] /= n;
                
            }
            
        }
        
    }
    
    public static void normalizeData(double data[], int y){
        double min = Utils.getMin(data);
        double max = Utils.getMax(data);
        //rescale values to fit on graph panel
        int s = data.length;
        for ( int i = 0; i < s; i++ ) {
            if (max>min){
                data[i] = ((data[i])*y)/(max-min);
            }
        }
    }
    
    public static double[] setRange1DArray(double data[], int min, int max) {
        
        double[] result = new double [data.length];
        for ( int i = 0; i < result.length; i++ ) {
            if (data[i] > max){
                result[i] = max ;
            } else if (data[i] < min){
                result[i] = min ;
            } else { result [i] = data[i];}
        }
        return result;
    }
    
    //test the filtering
    public static void main(String[] args) {
        int n = 128;
        int m = 128;
        double[] testdata = new double[n];
        double[] ftestdata = new double[n*2];
        double[] temp = new double[n*2];
        double[] filt1 = filter1("ramp", n, 1);
        double[] filt2 = shepplogan(n);
        
        
        int i=0;
        
        for(i=0;i<n;i++){
            testdata[i]=Math.cos(i*Math.PI/180);
            //System.out.print(testdata[i]+",");
        }System.out.println();
        
        
        //testdata = convolve( testdata, n, filt1, n);
        for(i=0;i<n;i++){
            System.out.print(filt1[i]+",");
        }System.out.println();
        
    }
}
