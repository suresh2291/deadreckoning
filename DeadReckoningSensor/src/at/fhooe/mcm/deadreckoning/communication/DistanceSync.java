/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.fhooe.mcm.deadreckoning.Communication;

import at.fhooe.mcm.deadreckoning.sensor.InertialSensor;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.NoRouteException;
import com.sun.spot.util.Utils;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

/**
 * @class DistanceSync
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @date 24.05.2010
 * @version 1.0
 *
 * @brief This class is responsible for broadcasting the distance and calculating the average between two SPOTs.
 *
 * The class uses two threads for receiving and sending. the receiving thread
 * blocks at the receiving function and calculates the average between the own distance
 * and the received distance.
 * The sending thread sends in a interval of 5 seconds the own distance as broadcast.
 * CAUTION: initialize the class ONLY after the init() of InertialSensor m_sensor!
 */
public class DistanceSync
{
    /** @brief Boolean, defines if the thread should run or not. */
    private boolean m_runThread = true;
    /** @brief reference to the inertial sensor for getting the distance. */
    private InertialSensor m_sensor = null;
    /** @brief own distance. */
    private float m_owndist = 0;
    /** @brief other distance. */
    private float m_foreigndist = 0;
    /** @brief average distance. */
    private float m_avg = 0;
    /** @brief Send thread timeout. */
    private int m_timeOut = 1000;

    private DistanceSync() {
    }

    public DistanceSync(InertialSensor _sensor) {
        m_sensor = _sensor;

        startSenderThread();
        startReceiverThread();
    }

    public void startReceiverThread() {
        new Thread() {

            public void run() {
                String tmp = null;
                RadiogramConnection dgConnection = null;
                Datagram dg = null;

                try {
                    dgConnection = (RadiogramConnection) Connector.open("radiogram://:99");
                    // Then, we ask for a datagram with the maximum size allowed
                    dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
                } catch (IOException e) {
                    System.out.println("Could not open radiogram receiver connection");
                    e.printStackTrace();
                    return;
                }

                while (true) {
                    try {
                        dg.reset();
                        dgConnection.receive(dg);
                        tmp = dg.readUTF();
                        if (tmp.substring(0, 5).equals("DIST:")) {
                            m_foreigndist = Float.parseFloat(tmp.substring(5));
                            m_owndist = m_sensor.getDistance();
                            m_avg = (m_foreigndist + m_owndist) / 2;
                        }
                        System.out.println("Received: " + tmp + " from " + dg.getAddress());
                    } catch (NumberFormatException _nfe) {
                        _nfe.printStackTrace();
                    } catch (IOException _ioe) {
                        _ioe.printStackTrace();
                    } catch (NullPointerException _nex) {
                        _nex.printStackTrace();
                    }
                }
            }
        }.start();
    }

    synchronized public void startSenderThread() {
        new Thread() {

            public void run() {
                // We create a DatagramConnection
                DatagramConnection dgConnection = null;
                Datagram dg = null;
                try {
                    // The Connection is a broadcast so we specify it in the creation string
                    dgConnection = (DatagramConnection) Connector.open("radiogram://broadcast:99");
                    // Then, we ask for a datagram with the maximum size allowed
                    dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
                } catch (IOException ex) {
                    System.out.println("Could not open radiogram broadcast connection");
                    ex.printStackTrace();
                    return;
                }

                while (true) {
                    try {
                        // We send the message (UTF encoded)
                        dg.reset();
                        dg.writeUTF("DIST:" + m_sensor.getDistance());
                        dgConnection.send(dg);
                        System.out.println("Broadcast is going through");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    Utils.sleep(m_timeOut);
                }
            }
        }.start();
    }

    public float getAverage() {
        return m_avg;
    }
}
