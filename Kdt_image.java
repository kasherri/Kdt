import bridges.connect.Bridges;
import bridges.base.KdTreeElement;
import bridges.base.Color;
import bridges.base.ColorGrid;

import java.io.*;
import java.lang.String;
import java.nio.charset.StandardCharsets;
import java.awt.Desktop;
import java.io.FileNotFoundException;
import java.util.Random;


/**
  Use KDTree to process image.
 */

public class Kdt_image {
    // define some constants
    public static final int MaxLevel = 10;
    public static final float HomogeneityThresh = 1.0f;
    public static final Boolean ShowPartitioners = false; // for viewing/hiding partitioning lines
    public static final Color LINE_COLOR = new Color("white"); // for partition lines

    public Kdt_image() {
        super();
    }

    /**
     * Builds a KD tree representation of a 2D color image.
     * Recursively partitions the image into smaller and smaller regions
     * and tests its pixel colors until they are deemed to be homogeneous (or
     * pass a homogeneity criteria, or the tree reaches a maximum height.
     *
     * @param region {xmin,ymin,xmax,ymax}
     * @param level level of tree
     * @param cg  colorgrid to work on
     * @param dim_flag
     * @param draw_partitioners
     * @return  the kdtree
     */
    public static KdTreeElement<Integer, String> buildImageTree(int[] region, int level,  ColorGrid cg, Boolean dim_flag, Boolean draw_partitioners) {
        // create a kd tree element
        int orientation = (dim_flag) ? 1 : 0; //false if x, true if y
        KdTreeElement<Integer, String> root = new KdTreeElement<Integer, String>(0, orientation);

        // check the region's homogeneity
        Boolean homogeneous = Image.IsRegionHomogeneous(cg, region);
        if ((level < MaxLevel) && !homogeneous) {
            // partition the region on one of two dimensions
            // here the dimension alternates between X and Y, controlled by a boolean flag
           int partition;

           if (!dim_flag) {
               partition=genRandom(region[0], region[2]); // partition on X (cols)
                // X partition - locate between 1/3 and 2/3 of the partition interval
                root.setPartitioner(partition);
                root = new KdTreeElement<Integer, String>(partition, orientation); //changing root key
                root.setLabel("X");
                int[] lregion={region[0], region[1], partition, region[3]};
                int[] rregion={partition, region[1], region[2], region[3]};

               // set children of root to subtrees obtained via recursion
                root.setRight(buildImageTree(rregion, level+1, cg, true, draw_partitioners ));//partition bt x axis
                root.setLeft(buildImageTree(lregion, level+1, cg, true, draw_partitioners )); //partition by x axis


                if (draw_partitioners==true) {
                    for (int i = region[1]; i < region[3]; i++) {
                        cg.set(i, partition, LINE_COLOR);
                    }
                }
                // find the region of the partitioning line, different for
                // X or Y partitioned dimension

            }
           else {
                // partition on Y (rows)
                // Y partition - locate between 1/3 and 2/3
                // of the partition interval
               partition=genRandom(region[1], region[3]);
               root.setPartitioner(partition);
               root = new KdTreeElement<Integer, String>(partition, 0); //changing root key
               root.setLabel("y");

               int[] lregion={region[0], region[1], region[2], partition};
               int[] rregion={region[0], partition, region[2], region[3]};

               // set children of root to subtrees obtained via recursion
               root.setRight(buildImageTree(rregion, level+1, cg, false, draw_partitioners ));//partion by x axis
               root.setLeft(buildImageTree(lregion, level+1, cg, false, draw_partitioners ));//partition by x axis


               if (draw_partitioners==true) {
                   for (int i = region[0]; i < region[2]; i++) {
                       cg.set(partition, i, LINE_COLOR);//uncomment this line to make the line white

                   }

               }
            }
            return root;
        }

        // BASE: this is a homogeneous region, so color it with average color
        Image.ColorRegion(cg, region, Image.avgColor(cg, region));
        return null;
    }

    /**
     *     generate an integer between 1/3 and 2/3 of the min-max range
     */
    public static int genRandom(int min, int max) {
        double a= ((max-min)/3.0) + min; //min
        double b= ((max-min)* (2.0/3.0)) + min; //max
        int rand = (int) (Math.random() * (a-b) +b);
        return  rand;
    }

    public static String data_saving(String original_file, String output) throws IOException {
        FileInputStream fisOriginal =new FileInputStream(original_file);
        FileInputStream fisOutput= new FileInputStream(output);
        byte[] bytes1= fisOriginal.readAllBytes();
        byte[] bytes2=fisOutput.readAllBytes();
        fisOutput.close();
        fisOriginal.close();
        long size=bytes1.length;
        long size1=bytes2.length;
        return "Original size:"+size+ " " +" Output Size:"+size1;
    }


    public static void save(ColorGrid cg, String file_name) throws IOException {
        //saving file in binary form
        String header = "P3\n" + "#my image file\n" + cg.getWidth() + cg.getHeight() + "\n" + 255 + " \n"; //header
        File file = new File(file_name);
        byte[] head= header.getBytes();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        BufferedOutputStream bufferedOutputStream= new BufferedOutputStream(fileOutputStream);
        //write header
        try {
            bufferedOutputStream.write(head);
        } catch (Exception e) {
            System.out.println(e);
        }
        //write rbg vals
        try {
            for (int row=0; row < cg.getHeight(); row++){
                for (int col=0; col < cg.getWidth(); col++){

                    Color c = cg.get(row, col);
                    bufferedOutputStream.write(Integer.toString(c.getRed()).getBytes(StandardCharsets.US_ASCII)); //write red
                    bufferedOutputStream.write(Integer.toString(c.getGreen()).getBytes(StandardCharsets.US_ASCII));//write green
                    bufferedOutputStream.write(Integer.toString(c.getBlue()).getBytes(StandardCharsets.US_ASCII));//write blue

                }

            }

    } catch (Exception e) {
        e.printStackTrace();
    }



}


    public static void load(String file_name, ColorGrid cg) throws IOException{
        File file = new File(file_name);
        FileOutputStream fileOutputStream= new FileOutputStream(file_name);
        OutputStreamWriter outputStreamWriter= new OutputStreamWriter(fileOutputStream);
        BufferedWriter writer = new BufferedWriter(outputStreamWriter);

        //top of file
        writer.write("P3");
        writer.newLine();
        writer.write(cg.getWidth()+" "+ cg.getHeight());
        writer.newLine();
        writer.write("255"); //maxVal
        writer.newLine();
        for(int row=0;row<cg.getHeight();row++){
            for(int column=0;column<cg.getWidth();column++){
                Color c= cg.get(row, column);
                writer.write(c.getRed()+ " ");
                writer.write(c.getGreen()+" ");
                writer.write(c.getBlue()+" ");
                if(column < cg.getWidth() - 1) writer.write(" ");
            }
            writer.newLine();
        }
        writer.flush();
        writer.close();
        Desktop desktop = Desktop.getDesktop();
        desktop.open(file);
    }











    public static void main(String[] args) throws Exception {

        // Bridges credentials
        Bridges bridges = new Bridges(24, "kasherri", "873658290717");


        bridges.setTitle("Image Representation/Compression Using K-D Trees: Part 1");
        bridges.setDescription("Press the > button to see KD Representation");
        String OrigFileName="images/square.ppm";
        Image image = new Image(OrigFileName);


	// Read image
	// Convert to ColorGrid

	ColorGrid cg =image.toColorGrid(image.image_array);



    int w=cg.getWidth();
    System.out.println(w);
    int h=cg.getHeight();
    System.out.println(h);
    int[] region= {0,0, w, h};
    //bridges.setDataStructure(buildImageTree(region, 0, cg, false, false )); to visualize tree
        //  bridges.visualize();

    bridges.setDataStructure(cg);
    bridges.visualize();
    buildImageTree(region, 0, cg, false, false );
    save(cg, "output.ppm");
    data_saving(OrigFileName,"output.ppm" );
    System.out.println(data_saving(OrigFileName,"output.ppm" ));
    bridges.visualize();
    load("output.ppm", cg);



    }

}
