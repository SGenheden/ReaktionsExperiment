/*
  Clock.java
  Created by Samuel Genheden 2008 
  (C) 2008 Samuel Genheden	
  Version 1.0
    
  Clock is class that implements a threaded clock
  and which display time on a text field every
  millisecond. At the current time there is now
  way of customizing the display.
*/
package experiment;

import java.text.*;
import javax.swing.*;

public class Clock extends Thread {
  
  boolean keepRunning = true;
  long startTime;
  JTextField textField;
  
  /*
    Constructor which starts the clock
    immediately when it is called.
  */
  public Clock(JTextField aTextField) {
    textField = aTextField;  	  
  	startTime = System.currentTimeMillis();
    setDaemon(true);
    start(); 
  }

  /*
    Display the elapsed time every millicsecond
    unless it has been told to stop
  */
  public void run() {
    while(keepRunning) {
      long currentTime = System.currentTimeMillis(); 
      textField.setText(millisecondsToString(currentTime - startTime));
      try { 
        Thread.sleep(10); 
      }
      catch (InterruptedException e) {
      } 
    }
  }

  /*
    Enabling to stop the clock
  */
  public void pleaseStop() { 
    keepRunning = false; 
  }
  
  /*
    Convert the elapsed time to a 
    convenient string for display
  */
  private String millisecondsToString(long tms) {
    // Break time down into hours, minutes and seconds
    double t = tms / 1000.0;    	
    int h = (int) (t / 3600);
    int m = (int) ((t - h * 3600) / 60);
    double s = t - h * 3600 - m * 60;
    // Format time as string, only showing minutes and seconds
    return(new DecimalFormat("00").format(m) + ":" + new DecimalFormat("00.000").format(s));
  }
}