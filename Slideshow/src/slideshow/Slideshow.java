package slideshow;

import java.awt.BorderLayout;
import java.awt.Image;
import java.nio.file.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;


/**
 * Creates a JFrame and plays a slideshow of photos from a preset folder
 * 
 * Default slideshow frequency is 10 seconds. Can be modified via the first command-line argument
 * 
 * Default folder refresh frequency is 2 minutes. Can be modified via the second command-line argument
 * 
 * @author Oludare Balogun
 * @version Jan 23 2018
 * 
 **/
public class Slideshow extends JFrame {

    private static ArrayList<Icon> icons = new ArrayList<Icon>();
    private static ArrayList<Icon> newIcons = new ArrayList<Icon>();
    private static Path folder = Paths.get("/home/pi/gdrive/TVSlides");
    private static DirectoryStream<Path> folderStream;
    private static int currSlide;
    private static JLabel slidesLabel = new JLabel();
    private static Thread thread1, thread2;
    private static int slideDelay = 10;
    private static int refreshDelay = 120;
    
    /**
     * Constructs a new slideshow class object.
     * Object is a fullscreen undecorated JFrame.
     * This constructor also populates an arraylist of icons with images from a hardcoded folder path after scaling them to 1080 pixels wide
     */
    
    public Slideshow() {
      currSlide = 0;
      setUndecorated(true);
      setExtendedState(JFrame.MAXIMIZED_BOTH); //Create fullscreen undecorated JFrame
      // Add all files from folder into arraylist of icons
      try (Stream<Path> folderStream = Files.list(folder)) {
      for (Path file : (Iterable<Path>)folderStream::iterator){
        if (ImageIO.read(file.toFile()) != null) {
        icons.add(new ImageIcon(new ImageIcon(file.toFile().getAbsolutePath()).getImage().getScaledInstance(-1, 1080, Image.SCALE_DEFAULT)));
        }
      }
      } catch (IOException x) {
      System.err.println(x);
      }
      //Centre the JLabel within the frame
      slidesLabel.setVerticalAlignment(JLabel.CENTER);
      slidesLabel.setHorizontalAlignment(JLabel.CENTER);
      setLayout(new BorderLayout());
      add(slidesLabel, BorderLayout.CENTER);
      }
    
    /**
     * Starts slideshow of images from preset folder. Each image is shown for 10 seconds and images are shown in the order that they are put into the arraylist
     * 
     * Folder must have at least one image or the program terminates with error printed to console
     */
    public void startSlideshow(){
      while(true) {
        try {
          if (icons.size() > 0){
            slidesLabel.setIcon(icons.get(currSlide));
            currSlide = (currSlide + 1) % icons.size();
            pack();
            TimeUnit.SECONDS.sleep(slideDelay);
          } else {
            System.err.println("There are no image files in the path");
            System.exit(1);
          }
        } catch (InterruptedException e){
          e.printStackTrace(System.out);
      }
      }
       }
    /**
     * Refreshes the preset folder every 2 minutes. Runs concurrently with the slideshow
     */
    public void updateFolder(){
      while (true){
        try {
          newIcons.clear();
          try (Stream<Path> folderStream = Files.list(folder)) {
      for (Path file : (Iterable<Path>)folderStream::iterator){
        if (ImageIO.read(file.toFile()) != null) {
        newIcons.add(new ImageIcon(new ImageIcon(file.toFile().getAbsolutePath()).getImage().getScaledInstance(-1, 1080, Image.SCALE_DEFAULT)));
      }
      }
      } catch (IOException x) {
      System.err.println(x);
      }
          icons = newIcons;
          currSlide = 0;
          TimeUnit.SECONDS.sleep(refreshDelay);
        } catch (InterruptedException e){
          e.printStackTrace(System.out);
       }
      }
     }
    
    public static void main(String[] args) {
       if (args.length > 0){
           try {
           slideDelay = Integer.parseInt(args[0]);
           } catch (NumberFormatException e) {
               e.printStackTrace(System.out);
           }
           
           if (args.length > 1){
               try {
               refreshDelay = Integer.parseInt(args[1]);
               } catch (NumberFormatException e) {
                   e.printStackTrace(System.out);
               }
           }
           
           if (args.length > 2){
               folder = Paths.get(args[2]);
           }  
       } 
        
       Slideshow slideshow = new Slideshow();
       slideshow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       slideshow.setLocationRelativeTo(null);
       slideshow.setVisible(true);
       
       thread1 = new Thread(){
         @Override
         public void run(){
           slideshow.startSlideshow();
         }
       };
       thread2 = new Thread(){
         @Override
         public void run(){
           slideshow.updateFolder();
         }
       };
       
       // Start the slideshow and refresh the folder concurrently 
       thread1.start();
       thread2.start(); 
    }
    
}
