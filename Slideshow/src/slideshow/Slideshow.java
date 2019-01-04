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
 * @version Jan 4 2019
 * 
 **/
public class Slideshow extends JFrame {
	
	//Store current photos in slideshow
    private static ArrayList<Icon> icons = new ArrayList<Icon>();
    
    //Store any new photos detected in google drive path
    private static ArrayList<Icon> newIcons = new ArrayList<Icon>();
    
    //Path to google drive folder
    private static Path folder = Paths.get("W:/Discovery Centre/Discovery Centre IT Support Analyst/Pi Project/TestPhotos");
    
    //Stream from folder path
    private static DirectoryStream<Path> folderStream;
    
    //Index of current slide in slideshow arraylist
    private static int currSlide;
    
    //JLabel for display
    private static JLabel slidesLabel = new JLabel();
    
    //Multithreading to handle fetching and display concurrently 
    private static Thread thread1, thread2;
    
    //Slideshow delay in seconds
    private static int slideDelay = 10;
    
    //Fecth delay in seconds
    private static int refreshDelay = 120;
    
    /**
     * Constructs a new slideshow class object.
     * Object is a fullscreen undecorated JFrame.
     * This constructor also populates an arraylist of icons with images from a hardcoded folder path after scaling them to 1080 pixels wide
     */
    
    public Slideshow() {
      
      currSlide = 0;
      setUndecorated(true);
      
      //Create fullscreen undecorated JFrame
      setExtendedState(JFrame.MAXIMIZED_BOTH);
      
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
        	
          //Check to see that there is more than one photo in folder
          if (icons.size() > 0){
            slidesLabel.setIcon(icons.get(currSlide));
            currSlide = (currSlide + 1) % icons.size();
            
            //Display the photo
            pack();
            
            //Wait
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
    	
    	//Construct new slideshow and initialize
       Slideshow slideshow = new Slideshow();
       slideshow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       slideshow.setLocationRelativeTo(null);
       slideshow.setVisible(true);
       
       //Check that slideshow delay was provided as a command line argument
       if (args.length > 0){
           try {
        	
        	//Update slideshow delay to command line argument
           slideDelay = Integer.parseInt(args[0]);
           } 
           catch (NumberFormatException e) {
           }
           
           //Check that refresh delay was provided in slideshow argument
           if (args.length > 1){
               try {
               refreshDelay = Integer.parseInt(args[1]);
               } catch (NumberFormatException e) {
               }
           }
       }
       
       //Create thread1 to run slideshow loop
       thread1 = new Thread(){
         public void run(){
           slideshow.startSlideshow();
         }
       };
       
       //Create thread2 to run refresh loop
       thread2 = new Thread(){
         public void run(){
           slideshow.updateFolder();
         }
       };
       
       
       thread1.start();
       thread2.start(); 
    }
    
}
