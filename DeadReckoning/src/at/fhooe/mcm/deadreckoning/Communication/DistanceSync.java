/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.fhooe.mcm.deadreckoning.Communication;

import at.fhooe.mcm.deadreckoning.sensor.InertialSensor;
import com.sun.spot.peripheral.NoRouteException;
import java.io.IOException;

/**
 *
 * @author bibo
 */
public class DistanceSync
{
    private DataInputOutputStreamConnection rConnection = null;
    private RadioOutputStreamConnection rosConnection = null;
    
    Thread m_receiveAndSend = null;
    boolean m_runThread = true;
    InertialSensor m_sensor = null;
    float m_avg=0;

    private DistanceSync()
    {

    }

    public DistanceSync(InertialSensor _sensor)
    {
        rConnection = new DataInputOutputStreamConnection();
        rConnection.connect("broadcast");
        m_sensor=_sensor;
        m_receiveAndSend = new Thread()
        {
            public void run()
            {
                String recv="";
                while(m_runThread)
                {
                    recv = rConnection.receive();
                    if(recv.substring(0, 4).equals("DIST:"))
                    {
                        try
                        {
                            float foreigndist = Float.parseFloat(recv.substring(5));
                            float owndist = m_sensor.getDistance();
                            m_avg = (foreigndist+owndist)/2;
                            rConnection.send("AVG:"+m_avg, 0);
                        }
                        catch (NumberFormatException _nfe)
                        {
                            _nfe.printStackTrace();
                        }
                        catch (IOException _ioe)
                        {
                            _ioe.printStackTrace();
                        }
                        catch (NullPointerException _nex)
                        {
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
    }

    public float getAverage(float _dist)
    {
        try {
            rConnection.send("DIST:"+String.valueOf(_dist), 0);
            String recv = rConnection.receive();
            if(recv.substring(0, 3).equals("AVG:"))
            {
                try
                {
                    m_avg = Float.parseFloat(recv.substring(4));
                }
            }
            //recv = rConnection.receive();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NoRouteException ex) {
            ex.printStackTrace();
        }
        catch (NumberFormatException _nfe)
        {
            _nfe.printStackTrace();
        }
        return avg;
    }
}
