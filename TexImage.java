/*

  TexImage.java

  This class is a wrapper around java's BufferedImage class
  
*/
import javax.imageio.*;
import java.io.*;
import java.awt.image.*;

class TexImage {

    BufferedImage image;

    TexImage(File file) throws IOException {
	
	image = ImageIO.read(file);
	
    }

    public BufferedImage getImage(){
	
	return image;
	
    }

    public int getMinX(){
	
	return image.getMinX();
	
    }

    public int getMinY(){
	
	return image.getMinY();
	
    }

    public int getWidth(){
	
	return image.getWidth();
	
    }

    public int getHeight(){
	
	return image.getHeight();
	
    }

    /*
      This method gets an array of ints (containing 1 RGB value per pixel)
      from the BufferedImage, and converts it into a byte array
    */
    public byte[] getPixels(){
	
	int minx = image.getMinX();
	int miny = image.getMinY();
	int width = image.getWidth();
	int height = image.getHeight();
	int len = width*height;
	byte[] outpixels = new byte[4*len];

	int[] inpixels = image.getRGB(image.getMinX(),image.getMinY(),
				      image.getWidth(),image.getHeight(),
				      null,0,image.getWidth());

	for(int i=0; i<len; i++){
	    int x = inpixels[i];
	    byte a = (byte)(x>>24 & 0xff);
	    byte r = (byte)(x>>16 & 0xff);
	    byte g = (byte)(x>>8 & 0xff);
	    byte b = (byte)(x & 0xff);
	    outpixels[4*i+0] = r;
	    outpixels[4*i+1] = g;
	    outpixels[4*i+2] = b;
	    outpixels[4*i+3] = a;
	}
	return outpixels;
	
    }

    static public void main(String[] args){
	
	if(args.length<1){
	    System.out.println("Usage: java TexImage <image file>");
	    System.exit(0);
	}

	String fname = args[0];

	TexImage img = null;

	try {
	    img = new TexImage(new File(fname));
	} catch(IOException e){
	    System.out.println(e.getMessage());
	    System.exit(0);
	}

	System.out.println("width:"+img.getWidth());
	System.out.println("height:"+img.getHeight());
	System.out.println("minX:"+img.getMinX());
	System.out.println("miny:"+img.getMinY());

	byte[] pix = img.getPixels();
	for(int i=0; i<120; i+=4){
	    System.out.println(pix[i]+" "+pix[i+1]+" "+pix[i+2]+" "+pix[i+3]);
	}

    }

}