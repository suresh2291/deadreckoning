/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.fhooe.mcm.deadreckoning.Communication;

/**
 *
 * @author bibo
 */
public class DistanceSync
{
    private final String REMOTE_SPOT_ADDRESS = "0014.4F01.0000.0221";
    private DataInputOutputStreamConnection rConnection = null;
    //private RadioOutputStreamConnection rosConnection = null;
        
    public DistanceSync()
    {
        rConnection = new DataInputOutputStreamConnection();
        //rosConnection = new RadioOutputStreamConnection();
    }

    public DistanceSync(String _address)
    {
        rConnection = new DataInputOutputStreamConnection();
        //rosConnection = new RadioOutputStreamConnection();
    }

    public float getAverage(float _dist)
    {
        rConnection.send(String.valueOf(_dist), 0)
        recv = rConnection.receive();
        
    }
}
