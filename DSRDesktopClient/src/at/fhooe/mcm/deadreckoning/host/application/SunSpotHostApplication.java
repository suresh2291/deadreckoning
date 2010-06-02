package at.fhooe.mcm.deadreckoning.host.application;

import at.fhooe.mcm.deadreckoning.host.dsr.DSRClient;
import at.fhooe.mcm.deadreckoning.host.gui.DeadReckoningInfoGUI;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.util.IEEEAddress;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @class SunSpotHostApplication
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @version 1.0
 *
 * @brief The starter for the Dead Reckoning GUI.
 *
 * Sstarts a new DSR client and the according GUI.
 */
public class SunSpotHostApplication {

    /**@brief instance of the GUI to show.*/
    static DeadReckoningInfoGUI g = new DeadReckoningInfoGUI();

    /**
     * @deprecated
     * @brief Tests the gui with a "|" separated list of data
     * notice the counting bug
     */
    private static void testGUI() {
        StringBuffer buff = new StringBuffer();
        for (double i = 0d;; i = i + 0.125f) {
            for (int j = 0; j < 13; j++) {
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
     * @brief Print out our radio address.
     */
    public void run() {
        long ourAddr = Spot.getInstance().getRadioPolicyManager().getIEEEAddress();
        System.out.println("Our radio address = " + IEEEAddress.toDottedHex(ourAddr));
    }

    /**
     * @brief Start up the host application.
     *
     * @param args any command line arguments
     */
    public static void main(String[] args) throws Exception {
        SunSpotHostApplication app = new SunSpotHostApplication();

        new DSRClient(g);
        //testGUI();
    }
}
