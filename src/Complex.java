// A simple complex number class for my DiscreetFFT
public class Complex {

	public double real;   // the real part
    public double imag;   // the imaginary part
    
    
    public Complex() {
    	this.real = 0;
    	this.imag = 0;
    }
    // create a new object with the given real and imaginary parts
    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    // return a string representation of the invoking Complex object
    public String toString() {
        if (imag == 0) return real + "";
        if (real == 0) return imag + "i";
        if (imag <  0) return real + " - " + (-imag) + "i";
        return real + " + " + imag + "i";
    }

    // return abs/modulus/magnitude and angle/phase/argument
    public double abs()   { 
    	return Math.sqrt(real*real + imag*imag); 
    }
    
    // Returns between -+pi
    public double phase() { 
    	return Math.atan2(imag, real); 
    }  

    // return a new Complex object whose value is (this + b)
    public Complex plus(Complex b) {
        Complex a = this;             // invoking object
        double real = a.real + b.real;
        double imag = a.imag + b.imag;
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this - b)
    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.real - b.real;
        double imag = a.imag - b.imag;
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this * b)
    public Complex times(Complex b) {
        Complex a = this;
        double real = a.real * b.real - a.imag * b.imag;
        double imag = a.real * b.imag + a.imag * b.real;
        return new Complex(real, imag);
    }

    // scalar multiplication
    // return a new object whose value is (this * alpha)
    public Complex times(double alpha) {
        return new Complex(alpha * real, alpha * imag);
    }

    // return a new Complex object whose value is the conjugate of this
    public Complex conjugate() {  return new Complex(real, -imag); }

    // return a new Complex object whose value is the reciprocal of this
    public Complex reciprocal() {
        double scale = real*real + imag*imag;
        return new Complex(real / scale, -imag / scale);
    }

    // return a / b
    public Complex divides(Complex b) {
        Complex a = this;
        return a.times(b.reciprocal());
    }

    // a static version of plus
    public static Complex plus(Complex a, Complex b) {
        double real = a.real + b.real;
        double imag = a.imag + b.imag;
        Complex sum = new Complex(real, imag);
        return sum;
    }
    
}