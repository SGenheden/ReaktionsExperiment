/*
  Experimentet.java
  Created by Samuel Genheden 2008 
  (C) 2008 Samuel Genheden	
  Version 1.0
    
  Experiment is the main window of the program, handling all task of the program
  except of keeping track of time which is handled by Clock.java.
*/
package experiment;

import experiment.Clock;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class Experimentet extends JFrame {
  // GUI components
  static JTextField clockField = new JTextField();
  static JLabel wordField = new JLabel();
  static JTextField messageField = new JTextField();
  static JTextField dummy1 = new JTextField();
  static JTextField dummy2 = new JTextField();
  
  // Static strings
  private static char RESET_KEY = 'r';
  private static char LOG_KEY = 'l';
  private static String SETTINGS_FILE = "settings.dat";
  private static String ORD_FILE = "ord.txt";
  private static String LOG_FILE = "log.txt";
  private static String ICON_FILE = "icon.gif";
  private static String CROSS_FILE = "cross.GIF";
  private static String MEDDELANDE_PROP = "Meddelande";
  private static String SANT_TANG_PROP = "Sant_tang";
  private static String FALSKT_TANG_PROP = "Falskt_tang";
  private static String KLOCKAN_PROP = "Visa_klockan";
  private static String ORD_PROP = "Max_ord";
  private static String SANT_PROP = "Sant_med";
  private static String FALSKT_PROP = "Falskt_med";
  private static String MEDDELANDE_DEF = "<HTML><FONT COLOR=\"BLACK\"><U>Tack så mycket för hjäpen!</U></FONT></HTML>";
  private static String SANT_TANG_DEF = "a";
  private static String FALSKT_TANG_DEF = "ä";
  private static String ORD_DEF = "5";
  private static String SANT_DEF = "Sant";
  private static String FALSKT_DEF = "Falskt";
  private static String NEJ_STR = "Nej";
  private static String JA_STR = "Ja";
  private static String ERROR_CAPTION = "Experimentet Error!";
  private static String PROPS_ERROR = "Unable to read from settings.dat \n Using default properties.";
  private static String ORD_ERROR = "Unable to read from ord.txt";
  private static String CLOCK_START = "00:00.000";
  private static String LOG_DATE = "EEE, d MMM yyyy HH:mm:ss";
  private static String SETTINGS_HEAD =  "---Experimentet v1.0---";
  private static String CAPTION = "Experimentet";
  private static String FONT_NAME = "Arial";
    
  // State variables
  private int shownWords = 0;
  private boolean stopped = false;
  private boolean running = false;
  private Clock timer;
  
  // List containing the word library
  private Vector<String> allWords = new Vector<String>();
  private Vector<String> currentWords = new Vector<String>();
  private String currentWord = "";
  private Random generator = new Random();
  
  // Program log and properties
  private Vector<String> log = new Vector<String>();  
  private Properties props = new Properties();
  private char trueKey;
  private char falseKey;
  
  public static void main (String[] args) {
    new Experimentet();
  }
  
  /*
    Constructor which read application properties, the word library and finally sets up th GUI
  */
  public Experimentet() {             
    readProperies();   
    readWords();
    updateWords();    
    setupGUI();          
  }
  
  /*
    Called when the window is closed, so save properties, the log and exit the program
  */
  private void exitProgram() {
  	saveProperties();
    saveLog();
    System.exit(0);
  }
  
  /*
    Called when a key is pressed, do different things depending on the key, currently the
    program reacts on R, L, A and Ä keystrokes
  */
  private void keyDown(KeyEvent e) {
  	char key_pressed = Character.toLowerCase(e.getKeyChar());  
  	 
  	// If the program is stopped (reached max words) the user
    // have to press R to reset the program, other keystrokes are ignored
  	if (stopped) { 
      if (key_pressed == RESET_KEY) {
        reset();
      }
  	  return;
  	}
  	
  	if (key_pressed == LOG_KEY) {
  	  saveLog();
  	}
  	
  	// Check if the false key or the true key has been stroked
  	boolean false_key = (key_pressed == falseKey);
  	boolean true_key = (key_pressed == trueKey);
    if (false_key || true_key) {
      if (running) {
        stopClock(false_key);
      } else {
        startClock();
      } 
    } 
  }
  
  /*
    Read the application properties from settings.dat
  */
  private void readProperies() {
  	try {
      FileInputStream in = new FileInputStream(SETTINGS_FILE);
      props.load(in);
      in.close();    
      trueKey=props.getProperty(SANT_TANG_PROP).toLowerCase().charAt(0);  
      falseKey=props.getProperty(FALSKT_TANG_PROP).toLowerCase().charAt(0);      
    } 
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, PROPS_ERROR, ERROR_CAPTION, JOptionPane.ERROR_MESSAGE); 
      props.setProperty(KLOCKAN_PROP, JA_STR);
      props.setProperty(ORD_PROP, ORD_DEF);   
      props.setProperty(MEDDELANDE_PROP,MEDDELANDE_DEF); 
      props.setProperty(SANT_TANG_PROP,SANT_TANG_DEF);
      props.setProperty(FALSKT_TANG_PROP,FALSKT_TANG_DEF);
      props.setProperty(SANT_PROP,SANT_DEF);
      props.setProperty(FALSKT_PROP,FALSKT_DEF);      
      trueKey=props.getProperty(SANT_TANG_PROP).toLowerCase().charAt(0);
      falseKey=props.getProperty(FALSKT_TANG_PROP).toLowerCase().charAt(0);
      return; 	
    }
  }
  
  /*
    Read the word library from ord.txt
  */  	 
  private void readWords() {
  	try {
      BufferedReader in = new BufferedReader(new FileReader(ORD_FILE));
      String str;
      while ((str = in.readLine()) != null) {
        allWords.add(str);
      }
      in.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, ORD_ERROR, ERROR_CAPTION, JOptionPane.ERROR_MESSAGE);  
      System.exit(1);  	
    }
  }
  
  /*
    Reset the program if max number of words has been shown
  */
  private void reset() {
  	shownWords = 0;
    stopped = false;
    saveLog();
    clockField.setText(CLOCK_START);
    messageField.setText("");
    wordField.setText("");
  }
  
  /*
    Append the current log to log.txt and then clear the log
  */
  private void saveLog () {	      
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(LOG_FILE,true));
      // Write the current date and time
      Date dateNow = new Date ();
      SimpleDateFormat dateFormat = new SimpleDateFormat(LOG_DATE);
      out.write(dateFormat.format(dateNow)+"\n");  
      // Write each log line
      int i;      	  
      for (i=0;i<log.size();i++) {
        String str = (String)log.get(i);
        out.write(str);
        out.newLine();
      }
      out.close();
      log.clear();
    } 
    catch (IOException e) {
    }    
  }
  
  /*
    Save application properties to settings.dat
  */
  private void saveProperties () {    
    try {
      FileOutputStream out = new FileOutputStream(SETTINGS_FILE);
      props.store(out,SETTINGS_HEAD);
      out.close();
    } 
    catch (IOException e) {
    }
  }
  
  /*
    Setup the grahpical user interface
  */
  private void setupGUI () {
  	// Create and add event handlers
  	addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          exitProgram();	
        }
      });
    KeyAdapter keyAdapter = new KeyAdapter() {
      public void keyPressed(KeyEvent keyEvent) {
        keyDown(keyEvent);       
      }
    };
    addKeyListener(keyAdapter);
  	
  	// Set caption of the window and a nice icon
  	setTitle(CAPTION);     
  	URL url = getClass().getResource(ICON_FILE);
    setIconImage(Toolkit.getDefaultToolkit().getImage(url));
  	getContentPane().setLayout(new BorderLayout());
  	
  	
  	// Initialize the text field of the clock
  	clockField.setText(CLOCK_START);
    clockField.setEditable(false);
    clockField.setBackground(Color.WHITE);
    clockField.setForeground(Color.BLACK);
    clockField.setFont(new Font(FONT_NAME, Font.BOLD,24));
    clockField.setHorizontalAlignment(SwingConstants.CENTER);
    clockField.addKeyListener(keyAdapter);
    getContentPane().add(clockField,BorderLayout.NORTH);
    
  	// Initialize the text field of the word    
    wordField.setText("");
    wordField.setForeground(new Color(0,0,128));
    wordField.setBackground(new Color(125,190,255));    
    wordField.setFont(new Font(FONT_NAME, Font.BOLD,24));
    url = getClass().getResource(CROSS_FILE);
    wordField.setIcon(new ImageIcon(url));
    wordField.setVerticalTextPosition(JLabel.BOTTOM);
    wordField.setHorizontalTextPosition(JLabel.CENTER);    
    wordField.setHorizontalAlignment(SwingConstants.CENTER);
    wordField.addKeyListener(keyAdapter);
    getContentPane().add(wordField,BorderLayout.CENTER);
    
  	// Initialize the text field of the message    
    messageField.setText("");
    messageField.setEditable(false);
    messageField.setForeground(Color.GREEN);
    messageField.setBackground(Color.WHITE);
    messageField.setFont(new Font(FONT_NAME, Font.BOLD,24));
    messageField.setHorizontalAlignment(SwingConstants.CENTER);
    messageField.addKeyListener(keyAdapter);
    getContentPane().add(messageField,BorderLayout.SOUTH);
    
    // Create dummy components
    dummy1.setText("AA");
    dummy1.setEditable(false);
    dummy1.setBackground(new Color(125,190,255));
    dummy1.setForeground(new Color(125,190,255));
    dummy1.setBorder(BorderFactory.createLineBorder(new Color(125,190,255)));   
    dummy1.addKeyListener(keyAdapter);
    getContentPane().add(dummy1,BorderLayout.WEST);
    dummy2.setText("AA");
    dummy2.setEditable(false);
    dummy2.setBackground(new Color(125,190,255));
    dummy2.setForeground(new Color(125,190,255));  
    dummy2.setBorder(BorderFactory.createLineBorder(new Color(125,190,255)));
    dummy2.addKeyListener(keyAdapter);
    getContentPane().add(dummy2,BorderLayout.EAST);
    
    // Fix the window size and finally show it
    getContentPane().setBackground(new Color(125,190,255));
    pack();
    setSize(400, 400);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setBounds((int) (0.5 * (screenSize.width - getWidth())), (int) (0.5 * (screenSize.height - getHeight())), getWidth(), getHeight());    
    setVisible(true);
  }
  
  /*
    Called when start key was stroked, display a new word and create a new clock
  */
  private void startClock() {
    running = true;
    shownWords = shownWords + 1;        
    wordField.setText(currentWord);    
    messageField.setText("");
    clockField.setText(CLOCK_START);  
    if (props.getProperty(KLOCKAN_PROP).equals(NEJ_STR)) {
      clockField.setForeground(Color.WHITE);
    }            
    timer = new Clock(clockField);  	  
  }
  
  /*
    Called when stop key was stroked, stop clock and do post-taks
  */
  private void stopClock(boolean falseKey) {
     timer.pleaseStop();
     running = false;
     updateWords(); // Update words here so we do not delay start
     
     if (props.getProperty(KLOCKAN_PROP).equals(NEJ_STR)) {
       clockField.setForeground(Color.BLACK);
     }
          
     // Display different message depending on which key was stroked
     if (falseKey) {
       messageField.setText(props.getProperty(FALSKT_PROP));
       messageField.setForeground(new Color(128,0,0));
     } else {
       messageField.setText(props.getProperty(SANT_PROP));
       messageField.setForeground(new Color(0,128,0));	
     }
     
     // Add the current word index, mesage and time to the log   
     String log_str = Integer.toString(allWords.indexOf(wordField.getText())+1);
     log_str = log_str + "\t" + messageField.getText();
     log_str = log_str + "\t" + clockField.getText(); 
     log.add(log_str);
     
     // Display a message and stop the program if max word has been reached
     if (shownWords == Integer.parseInt(props.getProperty(ORD_PROP))) {
       wordField.setText(props.getProperty(MEDDELANDE_PROP));
       stopped = true;
     }  
  }
  
  /*
    Generates a new current word by taking one from the list randomly
    and then removing it to avoid repeating words
  */
  private void updateWords() {
    if (currentWords.isEmpty()) {
      currentWords.addAll(allWords);
    }
    int currentIndex = generator.nextInt(currentWords.size());
    currentWord = (String)currentWords.get(currentIndex);
    currentWords.remove(currentIndex); 
  }
}