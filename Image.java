import java.io.*;
import bridges.base.ColorGrid;
import bridges.base.Color;
import java.util.Scanner;


/**
 * Read and store image.
 * Enables conversion to Bridges ColorGrid for display.
 * @author Kasherri
 */
public class Image {
    // If a default color is needed, use this one.
    private static final Color DEFAULT_COLOR = new Color("black");
    private int width, height, maxVal; // image dimensions
    public int[] image_array; // image array to store


    public Image() {
        width = height = maxVal = 0;
        image_array = null;
    }

    /**
     * creates an image object by reading the input image in
     * binary PPM
     *  All IO exceptions are thrown and must be
     *  dealt with by caller.
     * @param input_file name of PPM file (binary,not ascii)
     */

    public Image(String input_file) throws IOException, FileNotFoundException {


        // Create a File object and then a FileInputStream
        File input = new File(input_file);
        FileInputStream fins =new FileInputStream(input_file);


        readLine(fins);
        readLine(fins);
        Scanner sw= new Scanner(readLine(fins));
        this.width=sw.nextInt();
        this.height= sw.nextInt();
        Scanner sm=new Scanner(readLine(fins));
        this.maxVal=sm.nextInt();


       // s.nextLine();//first line
     //   s.nextLine(); //second line

	// Use Scanner to extract numbers for lines 3 and 4



        // Read in data a bytes.
        byte[] arr = new byte[3*this.width*this.height]; //intializing byte array
        fins.read(arr);//reading byte array
        fins.close();//closing to prevent memory leak
        this.image_array= new int[arr.length];
        for (int i=0; i < image_array.length; i++){
            this.image_array[i]= Byte.toUnsignedInt(arr[i]);

        }







    }



    /*
       Read a maximum of n bytes, until newline is found.
       @return string up to first newline.
   */
    private String readLine(FileInputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        char c;
        while (true) {
            c = (char) stream.read();
            if (c != '\n') sb.append(c);
            else break;
        }
        return sb.toString();
    }


    /**
     * @return imgarray as colorgrid
     */
    public ColorGrid toColorGrid(int[] image_array) {
        int k=0;

        // Create ColorGrid of correct dimensions.
        // value cant be int width or int height or out of bounds
        int row=0;//height
        int column=0;//width
        ColorGrid cg = new ColorGrid(this.height, this.width);

        for (int j=0; j < this.height; j++){
            for (int i=0; i < this.width; i++){
                Color c = new Color(this.image_array[k], this.image_array[k + 1], this.image_array[k + 2]);
                cg.set(j, i, c);
                k=k+3;

            }

        }

        return cg;
    }

    /**
     * Return avg RGB in ColorGrid for entire region
     * @param cg colorgrid to process
     * @param region rgb triplets
     * @return avg RGB
     */
    public static Color avgColor(ColorGrid cg, int[] region) {
        Color pixel;
        int[] rgb = {0,0,0}; // red, green, and blue
        //height is y axis, width is x axis
        //colurms is x axis, rows y
        int total=(region[2]-region[0])*(region[3]-region[1]);




        for(int y=region[1]; y < region[3]; y++){//j is rows
            for (int x=region[0]; x < region[2]; x++) { //int i is columns
                pixel=cg.get(y,x);
                //finding the amounts of each color
                rgb[0]+= pixel.getRed();
                rgb[1]+= pixel.getGreen();
                rgb[2]+= pixel.getBlue();
            }

        }
        //check if total is 0. means no pixel
        if(total==0) return null;
        //change back to int
        int r= rgb[0]/total;
        int g= rgb[1]/total;
        int b= rgb[2]/total;

        return new Color(r,g, b);
    }
    /**
     * Color the region with an average color
     * @param cg  grid to set
     * @param region used to set grid.
     */
    public static void ColorRegion(ColorGrid cg, int[] region) {
        ColorRegion(cg,region,avgColor(cg,region));
    }


    /**
     Color the provided region with the provided color.
     Used for partitioning lines; all pixels have the same color
     as the region is homogeneous.
     There are two versions of this function, depending on whether a
     constant or average color is used.  The average color is the average
     r,g,b of all pixels in the region.
     Use function overloading to implement the functions
     *
     * @param cg  the colorgrid
     * @param region the region to use
     * @param c the color
     */
    public static void ColorRegion(ColorGrid cg, int[] region, Color c) {
        //region {xmin,ymin,xmax,ymax} so region x is column y is row

        for(int j=region[1]; j< region[3]; j++){//j is rows
            for (int i=region[0]; i < region[2] ; i++){ //int i is columns
                    cg.set(j, i, c);

            }
        }
    }


    /**
     * Test a given region for homogeneity, i.e.,
     * if the region is within a threshold for approximation
     *
     * @param cg
     * @param region
     * @return true iff homogenous
     */
    public static Boolean IsRegionHomogeneous(ColorGrid cg, int[] region) {
        Color average=avgColor(cg, region);//average color
        long[] rgb={0,0,0}; //red green blue

        // Minimum is 4 pixels (return true if < 4)
        double total= (region[2]-region[0])*(region[3]-region[1]); //rows*columns to get total amount of pixels. previously had it as int but turned it to double in order to divide with more accuracy
        if (total<4)return true;

        // need to compute variance here for RGB (each color separately)
        for(int j=region[1]; j < region[3]; j++){
            for (int i=region[0]; i < region[2]; i++) { //int i is columns
                Color pixel=cg.get(j, i);
                rgb[0]+= (pixel.getRed()- average.getRed())*(pixel.getRed()- average.getRed());
                rgb[1]+= (pixel.getGreen()- average.getGreen())*(pixel.getGreen()- average.getGreen());
                rgb[2]+= (pixel.getBlue()- average.getBlue())*(pixel.getBlue()- average.getBlue());
            }
        }
        //finding variance as double
        int variance= (int) ((rgb[0]+rgb[1]+rgb[2])/total);
        return variance < 10;

        //return rgb[0]< 1 && rgb[1]<1 && rgb[2] < 1;
        //return (rgb[0] + rgb[1] + rgb[2]) < 1000;
    }

}

