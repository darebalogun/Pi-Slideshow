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
 * Default slideshow frequency is 10 seconds
 * 
 * Default folder refresh frequency is 2 minutes
 * 
 * @author Oludare Balogun
 * @version Jan 23 2018
 * 
 **/
public class Slideshow extends JFrame {

    private static ArrayList<Icon> icons = new ArrayList<Icon>();
    private static ArrayList<Icon> newIcons = new ArrayList<Icon>();
    private static Path folder = Paths.get("W:/Discovery Centre/Discovery Centre IT Support Analyst/Pi Project/TestPhotos");
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
     * Folder must have at least one image
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
            System.out.println("There are no image files in the path");
            break;
          }
        } catch (InterruptedException e){
          e.printStackTrace();
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
          e.printStackTrace();
       }
      }
     }
    
    public static void main(String[] args) {
        Slideshow slideshow = new Slideshow();
       slideshow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       slideshow.setLocationRelativeTo(null);
       slideshow.setVisible(true);
       
       if (args.length > 0){
           try {
           slideDelay = Integer.parseInt(args[0]);
           } catch (NumberFormatException e) {
           }
           
           if (args.length > 1){
               try {
               refreshDelay = Integer.parseInt(args[1]);
               } catch (NumberFormatException e) {
               }
           }
       }
       
       thread1 = new Thread(){
         public void run(){
           slideshow.startSlideshow();
         }
       };
       thread2 = new Thread(){
         public void run(){
           slideshow.updateFolder();
         }
       };
       thread1.start();
       thread2.start(); 
    }
    
}
