import java.lang.Math.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.sun.corba.se.spi.ior.Writeable;


public class FFT {
	private int width, height;
	private BufferedImage origImage, paddedImage,
						  fftImage, fftFiltImage,  filtImage;
	
	private Complex[][] fourierOut, fourierInv;
	
	public static final int FFT_FORWARD =  1;
	public static final int FFT_INVERSE = -1;
	
	public FFT(BufferedImage origImage) {
		super();
		int largerDimension = 0;
				
		this.origImage = origImage;
		this.width     = origImage.getWidth();
		this.height    = origImage.getHeight();
		
		if ( isPowerOfTwo(this.width)  != true || 
			 isPowerOfTwo(this.height) != true || 
			 this.height != this.width) {
									
			if (this.height < this.width)
				largerDimension = this.width;
			else
				largerDimension = this.height;
			
			// Create a larger image that is power of 2.
			// Check the larger dimension of the original image is a power of 2.
			// If so skip, if not find the next largest power of 2.
			if (isPowerOfTwo(largerDimension) != true) {				
				for (int i=0; i<32; i++) {									
					if ( (int) Math.pow(2, i) > largerDimension) {
						largerDimension = (int) Math.pow(2, i);
						break;
					}
				}
					
			}
			
			// Create and copy image to padded image
			this.height = this.width = largerDimension;
			this.paddedImage = new BufferedImage(largerDimension, 
												 largerDimension,
												 BufferedImage.TYPE_BYTE_GRAY);
			
			Graphics g = this.paddedImage.createGraphics();
			g.drawImage(this.origImage, 0, 0, null);
			g.dispose();
			
			
			
			System.out.print("Debug: Newly sized image");
		}
		else {
			this.paddedImage = this.origImage;
		}	
		
        this.fftImage    = new BufferedImage(this.width, 
											 this.width,
										     BufferedImage.TYPE_BYTE_GRAY);

		this.fftFiltImage = new BufferedImage(this.width, 
										      this.width,
											  BufferedImage.TYPE_BYTE_GRAY);
		
		performFFT();
	}

	// Setters and Getters	
	public BufferedImage getOrigImage() {
		return origImage;
	}

	public void setOrigImage(BufferedImage origImage) {
		this.origImage = origImage;
	}

	public BufferedImage getFftImage() {
		return fftImage;
	}

	public void setFftImage(BufferedImage fftImage) {
		this.fftImage = fftImage;
	}

	public BufferedImage getFiltImage() {
		return filtImage;
	}

	public void setFiltImage(BufferedImage filtImage) {
		this.filtImage = filtImage;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	
	// FFT related functions
	private void performFFT() {
		int i, j;
		Raster inputRaster =  this.paddedImage.getRaster();
		
		//Initializing Fourier Transform Array        
        Complex[][] FourierInput = new Complex[this.width][this.height];
        this.fourierOut          = new Complex[this.width][this.height];
        
        //Copy Image Data to the Complex Array
        for (i=0; i < this.width; i++) {
            for (j = 0; j < this.height; j++)
            {
            	FourierInput[i][j] = new Complex(inputRaster.getSample(i, j, 0), 0);            	
            }
        }
        
        inputShift(FourierInput, this.width);
        
        //Calling Forward Fourier Transform
        this.fourierOut = FFT2D(FourierInput, this.width, FFT.FFT_FORWARD);
                
        ConvertFFTtoImage(this.fourierOut, this.fftImage);                      
        
	}
	
	// Performs FFT with the direction set to Inverse and sets it to an image
	private void performInvFFT(Complex[][] input) {
		
		double[][] magnitude = new double[this.width][this.height];
		
		this.filtImage = new BufferedImage(this.origImage.getWidth(), 
										   this.origImage.getHeight(),
										   BufferedImage.TYPE_BYTE_GRAY);
		
//		this.filtImage = new BufferedImage(this.width, 
//				   						   this.height,
//				   						   BufferedImage.TYPE_BYTE_GRAY);
		
		WritableRaster raster = this.filtImage.getData().createCompatibleWritableRaster();
		
		this.fourierInv = FFT2D(input, this.width, FFT.FFT_INVERSE);
		ConvertFFTtoImage(this.fourierInv, this.filtImage);
		
		// When the inverse transform is taken the real signal is flipped.
		// Adjust accordingly. Also the whole padded image has to be inversed
		// So we cannot set the new image in this loop
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				
				magnitude[x][y] = this.fourierInv[x][y].abs();		
			}
		}
		
		for (int x = 0; x < this.filtImage.getWidth(); x++) {
			for (int y = 0; y < this.filtImage.getHeight(); y++) {
				
//				raster.setSample(this.filtImage.getWidth()-x-this., 
//								 this.filtImage.getHeight()-y-1,
				raster.setSample(x, y, 0, 
								 Math.round(magnitude[this.width-x-1][this.height-y-1]));
			}
		}
		
		this.filtImage.setData(raster);
	}
	
	private Complex[][] FFT2D(Complex[][] input, int n, int direction) {
		
		 int i,j;

		 Complex[] input1D      = new Complex[n];
		 Complex[] output1D     = new Complex[n];
         Complex[][] tempOutput = new Complex[n][n];      
         
         // Initialize the complex array.
         for (i=0; i<n; i++) {
        	 input1D[i]  = new Complex();
         }
         
         // Fourier Transform Rows First     
         for (i=0; i<n; i++) 
         {
        	 for (j=0; j<n; j++) 
        	 {        	   
			   input1D[j] = input[i][j];
        	 }
        	 
        	 // Calling 1D FFT Function for Rows
        	 output1D = FFT1D(n, input1D);
        	 
        	 for (j=0; j<n; j++) {
        		 tempOutput[i][j] = output1D[j];
        	 }
        	 
        	 // Scaling if forward FFT
        	 if (direction == FFT.FFT_FORWARD) {
        		 for (j=0; j<n; j++)
            		 tempOutput[i][j] = tempOutput[i][j].times((1d/n));
        	 }
         }
         
         // Now we tranform the Collumns
         // Flip the indexes for the Columns
         for (j=0; j<n; j++) {
        	 for (i=0; i<n; i++) {        		     
        		 input1D[i] = tempOutput[i][j];
        	 }
        	 
        	 output1D = FFT1D(n, input1D);
        	 
        	 for (i=0; i<n; i++) {
        		 // Scaling if forward FFT
	        	 if (direction == FFT.FFT_FORWARD) {        		
	    			 tempOutput[i][j] = output1D[i].times((1d/n));
	        	 }
	        	 else {
	        		 tempOutput[i][j] = output1D[i];
	        	 }
        	 }
         }
         
         return tempOutput;
	}
	
	// Recursive Implementation of Cooley Tukey FFT. Function call overhead(?) Scaling is done
	// outside the function due to recursive nature.
	public Complex[] FFT1D(int n, Complex[] input ) {
		
		Complex[] evenInput, oddInput,
				  evenOut,   oddOut,
				  Output;
		
		Output = new Complex[n];
		
		if (n==1)
		{
			Output[0] = input[0];
			return Output;
		}
		
		evenInput = new Complex[n/2];
		oddInput  = new Complex[n/2];
		
		// Grab even or odd elements
		for (int i=0; i < n/2; i++) {		
			evenInput[i] = input[2*i];
			oddInput[i]  = input[2*i+1];
		}
		
		evenOut = FFT1D(n/2, evenInput);
		oddOut  = FFT1D(n/2, oddInput);
		
		// Combine output
		for (int k=0; k < n/2; k++)
		{
			double kth = -2 * k * Math.PI / n;
            Complex exp = new Complex(Math.cos(kth), Math.sin(kth));
            Output[k]       = evenOut[k].plus(exp.times(oddOut[k]));
            Output[k + n/2] = evenOut[k].minus(exp.times(oddOut[k]));
       	}
        
		return Output;
	}
		
	private static void inputShift(Complex[][] input, int n) {
		
		for (int i=0; i<n; i++) {
			for (int j=0; j<n; j++) {
				input[i][j] = input[i][j].times(Math.pow(-1, (i+j)));
			}
		}
	}
	
	private void ConvertFFTtoImage(Complex[][] input, BufferedImage outImage) {
		
		double max = -1, scale = 1;
		double[][] magnitude = new double[this.width][this.height];
		
		WritableRaster raster = outImage.getData().createCompatibleWritableRaster();
		
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				
				magnitude[x][y] = Math.log(input[x][y].abs() + 1);

				if (magnitude[x][y] > max) {
					max = magnitude[x][y];
				}
			}
		}
		
		// Scale the image to 0-255
		if (max !=0)
			scale = 255d/max;
		
		for (int x = 0; x < outImage.getWidth(); x++) {
			for (int y = 0; y < outImage.getHeight(); y++) {
				magnitude[x][y] = magnitude[x][y] * scale;
				raster.setSample(x, y, 0, Math.round(magnitude[x][y]));
			}
		}
		
		outImage.setData(raster);
	}
	
	// Filtering functions
	public void idealLowPassFilter(double r) {
		
		double dist;
		int n = this.width;
		
		for (int u=0; u < n; u++) {
			for (int v=0; v < n; v++) {			
				dist = Math.sqrt(Math.pow(u-n/2, 2) + 
								 Math.pow(v-n/2, 2));
				
				// Zero out outside the circle
				if (dist > Math.pow(r, 2)) {
					this.fourierOut[u][v].real = 0;
					this.fourierOut[u][v].imag = 0;
				}
			}
		}
		
		ConvertFFTtoImage(this.fourierOut, this.fftFiltImage);
		displayImage(this.fftFiltImage, "Ideal Low Filtered FFT");
		performInvFFT(this.fourierOut);
	}
	
	public void butterworthLowPass(double r, int order) {
		double dist;
		int n = this.width;
		
		for (int u=0; u < n; u++) {
			for (int v=0; v < n; v++) {			
				dist = Math.sqrt(Math.pow(u-n/2, 2) + 
								 Math.pow(v-n/2, 2));
				
				dist = 1/(1+Math.pow((dist/r), 2*order));
				
				this.fourierOut[u][v] = this.fourierOut[u][v].times(dist);
			}
		}
		
		
		ConvertFFTtoImage(this.fourierOut, this.fftFiltImage);
		displayImage(this.fftFiltImage, "Butter Worth Low Filtered FFT");
		performInvFFT(this.fourierOut);
	}
	
	public void idealHighPassFilter(double r) {
		
		double dist;
		int n = this.width;
		
		for (int u=0; u < n; u++) {
			for (int v=0; v < n; v++) {			
				dist = Math.sqrt(Math.pow(u-n/2, 2) + 
								 Math.pow(v-n/2, 2));
				
				// Zero out outside the circle
				if (dist < Math.pow(r, 2)) {
					this.fourierOut[u][v].real = 0;
					this.fourierOut[u][v].imag = 0;
				}
			}
		}
		
		ConvertFFTtoImage(this.fourierOut, this.fftFiltImage);
		displayImage(this.fftFiltImage, "Ideal High Filtered FFT");
		performInvFFT(this.fourierOut);
	}
	
	public void butterworthHighPass(double r, int order) {
		double dist;
		int n = this.width;
		
		for (int u=0; u < n; u++) {
			for (int v=0; v < n; v++) {			
				dist = Math.sqrt(Math.pow(u-n/2, 2) + 
								 Math.pow(v-n/2, 2));
				
				dist = 1/(1+Math.pow((r/dist),2*order));
				
				this.fourierOut[u][v] = this.fourierOut[u][v].times(dist);
			}
		}
		
		
		ConvertFFTtoImage(this.fourierOut, this.fftFiltImage);
		displayImage(this.fftFiltImage, "Butter Worth Low Filtered FFT");
		performInvFFT(this.fourierOut);
	}
	
	// Helper functions	
	private static boolean isPowerOfTwo(int number) {
        if(number <=0){
            throw new IllegalArgumentException("Number is less than 0: " + number);
        }
        if ((number & -number) == number) {
            return true;
        }
        return false;
    }

	
	public static BufferedImage toGray(BufferedImage image) {
		
		BufferedImage grayImage = new BufferedImage(image.getWidth(), 
													image.getHeight(),  
													BufferedImage.TYPE_BYTE_GRAY);  
		Graphics g = grayImage.getGraphics();  
		g.drawImage(image, 0, 0, null);  
		g.dispose();  
	    
	    return grayImage;
	  }
	
	private static BufferedImage readImage(String path) {
        
		BufferedImage readImage = null;
		try {
        	File file = new File(path);
        	readImage = ImageIO.read(file);
        	
        	if (readImage.getType() != BufferedImage.TYPE_BYTE_GRAY)
        	{
        		readImage = toGray(readImage);
        	}
        }
        catch (IOException e) {
        	System.console().printf("Error occured opening file.", null);
			e.printStackTrace();
			System.exit(-1);
        }
        
        return readImage;
	}
		
	public static void displayImage(BufferedImage image, String text) {
        
		JLabel label  = new JLabel(new ImageIcon(image));
		label.setText(text);
		label.setVerticalTextPosition(JLabel.BOTTOM);
		label.setHorizontalTextPosition(JLabel.CENTER);
        
    	JPanel panel = new JPanel(); 
    	panel.add(label);
    	 	
    	
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
        frame.add(panel);           
        
        
        frame.pack();
        frame.setVisible(true);
	}
	
	public void saveImages(){
		try {
		    // retrieve image
		    File outputfile = new File("fftImg.jpg");
		    ImageIO.write(this.fftImage, "jpg", outputfile);
		    
		    outputfile = new File("fftFiltImg.jpg");
		    ImageIO.write(this.fftFiltImage, "jpg", outputfile);
		    
		    outputfile = new File("filtImg.jpg");
		    ImageIO.write(this.filtImage, "jpg", outputfile);
		    
		} catch (IOException e) {
		    System.out.print("Error saving pictures.");
		}
	}
	
	public static void main(String[] args) {
		
		int order;
		double r;
		
		// create a scanner so we can read the command-line input
	    Scanner scanner = new Scanner(System.in);		
		System.out.print("Welcome to my FFT demo. Please enter the string you would like to perform on.\n");
		
		// get their input as a String
	    //String fileName = scanner.next();
	    String fileName = "Blur.jpg";
		FFT myFFT = new FFT(readImage(fileName));
	    
	    displayImage(myFFT.getOrigImage(), "Grayscale Original");
	    displayImage(myFFT.getFftImage(), "FFT Spectra");
	    	    
	    System.out.print("Please pick a type of filtering you would like to do by entering a number.\n");
	    System.out.print("1. Low Pass ideal filter. \n");
	    System.out.print("2. Low Pass Butterworth filter.\n");
		System.out.print("3. High Pass ideal filter. \n");
	    System.out.print("4. High Pass Butterworth filter.\n");
	    
	    myFFT.butterworthHighPass(1d, 2);
	    displayImage(myFFT.getFiltImage(), "Inverse");
	    
	    int choice = scanner.nextInt();
	    switch(choice){
	    	case 1:
	    		System.out.println("Please enter cut-off radius:");
	    		r = scanner.nextDouble();
	    		myFFT.idealLowPassFilter(r);
	    		break; 
	    	case 2:
	    		System.out.println("Please enter cut-off radius:");
	    		r = scanner.nextDouble();
	    		System.out.println("Please enter order:");
	    		order = scanner.nextInt();
	    		myFFT.butterworthLowPass(r, order);
	    		break;
	    	case 3:
	    		System.out.println("Please enter cut-off radius:");
	    		r = scanner.nextDouble();
	    		myFFT.idealHighPassFilter(r);
	    		break;
	    	case 4:
	    		System.out.println("Please enter cut-off radius:");
	    		r = scanner.nextDouble();
	    		System.out.println("Please enter order:");
	    		order = scanner.nextInt();
	    		myFFT.butterworthHighPass(r, order);
	    		break;
	    	default:
	    		
	    
	    }
	    
	    displayImage(myFFT.getFiltImage(), "Inverse");
	    myFFT.saveImages();
	}

}
