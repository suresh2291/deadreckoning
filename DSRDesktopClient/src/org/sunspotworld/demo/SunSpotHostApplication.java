/*
 * Copyright (c) 2006, 2007 Sun Microsystems, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
