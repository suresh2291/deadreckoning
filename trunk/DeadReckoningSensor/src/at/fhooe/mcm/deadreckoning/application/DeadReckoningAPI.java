package at.fhooe.mcm.deadreckoning.application;

import at.fhooe.mcm.deadreckoning.Communication.DistanceSync;
import at.fhooe.mcm.deadreckoning.sensor.InertialSensor;
import at.fhooe.mcm.dsr.DSRClient;
import com.sun.spot.util.Utils;
import com.sun.spot.util.BootloaderListener;

import java.io.IOException;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * @class DeadReckoningAPI
 * @brief This class represents a dead reckoning application structure.
 *
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @date 15.05.2010
 * @version 2.0
 */
public class DeadReckoningAPI extends MIDlet {

    /** @brief A timer to count delta times in order to send data periodically. */
    private float m_timer;

    /** @brief The DSR client that enables ad hoc networking. */
    private DSRClient m_dsr ;

    /** @brief The inertial sensor for distance calculation. */
    private InertialSensor m_sensor;

    /** @brief A distance sync in order to correct sensor measurements by using reference sensors. */
    private DistanceSync m_distSync;

    /** @brief A clock in order to measure time. */
    private Timer m_clock;

    /**
     * @brief Main application loop.
     *
     * This method represents the main update loop for the sensor calculations.
     * The spot sends its collected data once a second to any arbitrary address.
     * If a spot is used as reference sensor to average measurement data, the
     * m_dsr.sendData() method call has to be commented on deploy.
     * 
     * @throws IOException
     */
    private void run() throws IOException {

        m_sensor.init();
        m_distSync = new DistanceSync(m_sensor);

        while (true) {
            m_timer += (float)m_clock.getDelta();
            m_sensor.update((float) m_clock.getDelta());
            
            if (m_timer > 1f) {

                m_dsr.sendData(m_sensor.currentStateToString() + m_distSync.getAverage() + "|", "0014.4F01.0000.6F4B");
                m_timer = 0f;
            }

            /*
             * The sensor operates at 160 Hz which means 160 updates per second.
             * This means that a new value is achieved 0.00625 seconds which are
             * 6.25 ms. To not get the same value multiple times the sleep must
             * be longer than 7ms.
             */
            Utils.sleep(10);
            m_clock.tick();
        }
    }

    /**
     * @brief The rest is boiler plate code, for Java ME compliance.
     *
     * startApp() is the MIDlet call that starts the application.
     */
    protected void startApp() throws MIDletStateChangeException {
        new BootloaderListener().start();
        try {
            m_clock  = new Timer();
            m_sensor = new InertialSensor();
            m_dsr    = new DSRClient();
            run();
        } catch (IOException ex) {          
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
     * if VM.stopVM() is called this method is called.
     */
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        m_dsr.stop();
    }
}