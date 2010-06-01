/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.fhooe.mcm.deadreckoning.communication;

import at.fhooe.mcm.deadreckoning.sensor.InertialSensor;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.NoRouteException;
import com.sun.spot.util.Utils;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

/**
 *
 * @author bibo
 */
public class DistanceSync {

    private boolean m_runThread = true;
    private InertialSensor m_sensor = null;
    private float m_owndist = 0;
    private float m_foreigndist = 0;
    private float m_avg = 0;

    private DistanceSync() {
    }

    public DistanceSync(InertialSensor _sensor) {
        m_sensor = _sensor;

        startSenderThread();
        startReceiverThread();
        /*
        rConnection = new DataInputOutputStreamConnection();
        rConnection.connect("broadcast");
        
        m_receiveAndSend = new Thread() {

        public void run() {
        String recv = "";
        while (m_runThread) {
        recv = rConnection.receive();
        if (recv.substring(0, 4).equals("DIST:")) {
        try {
        float foreigndist = Float.parseFloat(recv.substring(5));
        float owndist = m_sensor.getDistance();
        m_avg = (foreigndist + owndist) / 2;
        rConnection.send("AVG:" + m_avg, 0);
        } catch (NumberFormatException _nfe) {
        _nfe.printStackTrace();
        } catch (IOException _ioe) {
        _ioe.printStackTrace();
        } catch (NullPointerException _nex) {
        _nex.printStackTrace();
        }
        }
        }
        try {
        Thread.sleep(500);
        } catch (InterruptedException ex) {
        ex.printStackTrace();
        }
        }
        };
        m_receiveAndSend.start();
         */
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
                    Utils.sleep(5000);
                }
            }
        }.start();
    }

    public float getAverage() {
        return m_avg;
    }
}
