

package at.fhooe.mcm.deadreckoning.application;

import at.fhooe.mcm.deadreckoning.sensor.InertialSensor;
import at.fhooe.mcm.dsr.DSRClient;
import com.sun.spot.sensorboard.EDemoBoard;

import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.ISwitch;
import com.sun.spot.sensorboard.peripheral.LEDColor;
import com.sun.spot.util.Utils;
import com.sun.spot.util.BootloaderListener;

import java.io.IOException;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
 
public class DeadReckoningAPI extends MIDlet {
     private DSRClient m_dsr = new DSRClient();

    InertialSensor sensor;
    Timer clock;

  
    /**
     * @brief Main application loop.
     * 
     * @throws IOException
     */
    private void run()throws IOException {  
        

        sensor.calibrate();
        sensor.init();
        int i = 0;
        for(;;i++)
        {
           sensor.update((float)clock.getDelta());
           if(i%20==0)
           {
            m_dsr.sendData(""+sensor.getDistance(), "0014.4F01.0000.6EF0");
           }
           Utils.sleep(50);
           clock.tick();
        }
    }

   
    /**
     * @brief The rest is boiler plate code, for Java ME compliance
     *
     * startApp() is the MIDlet call that starts the application.
     */
    protected void startApp() throws MIDletStateChangeException { 
        new BootloaderListener().start();       // Listen for downloads/commands over USB connection
        try {
            clock = new Timer();
            sensor = new InertialSensor();
            
            run();
        } catch (IOException ex) {              // A problem in reading the sensors. 
            ex.printStackTrace();
        }
    }

    /**
     * @brief This will never be called by the Squawk VM.
     */
    protected void pauseApp() { 
    }

    /**
     * @brief Called if the MIDlet is terminated by the system.
     *
     * If startApp throws any exception other than MIDletStateChangeException,
     * if the isolate running the MIDlet is killed with Isolate.exit(), or
     * if VM.stopVM() is called this method is called..
     */
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException { 
    }

}
