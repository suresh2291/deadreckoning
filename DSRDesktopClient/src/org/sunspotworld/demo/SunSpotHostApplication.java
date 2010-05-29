package org.sunspotworld.demo;

import com.sun.spot.peripheral.Spot;

import com.sun.spot.util.IEEEAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sunspotworld.demo.dsr.DSRClient;
import org.sunspotworld.demo.gui.DeadReckoningInfoGUI;


/**
 * Sample Sun SPOT host application
 */
public class SunSpotHostApplication {
static DeadReckoningInfoGUI g = new DeadReckoningInfoGUI();
    private static void testGUI()
    {
        StringBuffer buff = new StringBuffer();
        for(double i =0d;;i=i+0.1d)
        {
          for(int j = 0;j<14;j++)
          {
              buff.append(i);
              buff.append("|");

          }
          g.setData(buff.toString());
          buff = new StringBuffer();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(SunSpotHostApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Print out our radio address.
     */
    public void run() {
        long ourAddr = Spot.getInstance().getRadioPolicyManager().getIEEEAddress();
        System.out.println("Our radio address = " + IEEEAddress.toDottedHex(ourAddr));
    }

    /**
     * Start up the host application.
     *
     * @param args any command line arguments
     */
    public static void main(String[] args) throws Exception {
        SunSpotHostApplication app = new SunSpotHostApplication();
        
        new DSRClient(g);
        testGUI();
        //System.exit(0);
    }

}
