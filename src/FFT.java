import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;


public class FFT {
	private int width, height;
	private BufferedImage origImage, fftImage, filtImage;
	
		
	public FFT(BufferedImage origImage) {
		super();
		this.origImage = origImage;
		this.width  = origImage.getWidth();
		this.height = origImage.getHeight();
		
		
	}

	// Setters and Getters
	public BufferedImage getOriginalImage()	{
		return this.origImage;
	}
	
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
	
	
	// Helper functions
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
	
	private void linearTransformImageToRange(long[][] matrix,
											 long min,
											 long max){
		
		double temp, 
			   scale = max-min;
				
		for (int x=0; x<this.width; x++) {
			for (int y = 0; y < this.getHeight(); y++) {
				
				temp = (matrix[x][y] - min)/scale;
				
 				matrix[x][y] = Math.round(temp * 255);
			}
		}
	}
	
	
	public static void main(String[] args) {
		
		// create a scanner so we can read the command-line input
	    Scanner scanner = new Scanner(System.in);
		
		System.out.print("Welcome to my FFT demo. Please enter the string you would like to perform on.");
		
		// get their input as a String
	    String fileName = scanner.next();
	    
	    FFT myFFT = new FFT(readImage(fileName));
	    
	}

}
