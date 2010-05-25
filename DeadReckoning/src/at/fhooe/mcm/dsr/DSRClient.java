/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.fhooe.mcm.dsr;

import at.fhooe.mcm.dsr.packages.DataPkg;
import at.fhooe.mcm.dsr.packages.RREPPkg;
import at.fhooe.mcm.dsr.packages.RREQPkg;
import at.fhooe.mcm.dsr.util.IITupel;
import at.fhooe.mcm.dsr.util.RequestTable;
import at.fhooe.mcm.dsr.util.RouteRecord;
import at.fhooe.mcm.dsr.util.RouteTable;
import com.sun.spot.io.j2me.radiogram.Radiogram;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.NoRouteException;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.peripheral.radio.routing.RoutingPolicy;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.LEDColor;
import com.sun.spot.sensorboard.peripheral.TriColorLED;
import com.sun.spot.util.IEEEAddress;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;

/**
 *
 * @author Peter
 */
public class DSRClient
{
    private static final int DISCOVERY_TIMEOUT = 10000;
    private static final String ADDRESS_START = "0014.4F01.";
    private static final int ROUTE_DISCOVERY_TIMEOUT = 2000;
    private static final int BLINK_TIME = 500;
   
    private int m_lastRRQID = 0;
    private Vector m_clientsInRange = new Vector();

    private RequestTable m_reqTable = new RequestTable();
    private RouteTable m_routeTable = new RouteTable();

    private ITriColorLED m_leds[] = EDemoBoard.getInstance().getLEDs();


    private static final int SEND_DATA_LED = 0;
    private static final int CREATE_RREQ_LED = 1;
    private static final int FORWARD_RREQ_LED = 2;
    private static final int FORWARD_RREP_LED = 3;
    private static final int RECEIVE_RREP_LED = 4;
    private static final int FORWARD_DATA_LED = 5;
    
    private static final LEDColor SEND_DATA_COL = LEDColor.BLUE;
    private static final LEDColor CREATE_RREQ_COL = LEDColor.GREEN;
    private static final LEDColor FORWARD_RREQ_COL = LEDColor.YELLOW;
    private static final LEDColor FORWARD_RREP_COL = LEDColor.ORANGE;
    private static final LEDColor RECEIVE_RREP_COL = LEDColor.RED;
    private static final LEDColor FORWARD_DATA_COL = LEDColor.WHITE;

    private String m_baseAddr = "radiogram://";
    private int m_port = 66;
    private String m_recvAddr = m_baseAddr+":"+m_port;
    private String m_broadcastAddr = m_baseAddr+"broadcast:"+m_port;

    private boolean m_execRCVLoop = true;
    private boolean m_execNodeDiscovery = true;
    private boolean m_execBlink = true;

    public DSRClient()
    {
        new Thread() {
            public void run () {
                blinkTest();
            }
        }.start();

        new Thread(){
            public void run()
            {
                rcvLoop();
            }
        }.start();

        new Thread(){
            public void run()
            {
                //discoverNodesInRange();
            }
        }.start();
        //TestThread
        new Thread()
        {
            public void run()
            {
                //testSendData();
                /*while(true!=false)
                {
                    performRREQ(ADDRESS_START+"0000.6B75");
                }*/
            }
        }.start();
    }

    public synchronized void sendData(final String _data, final String _addr)
    {
        indicateSendData();
        new Thread()
        {
            public void run()
            {
                System.out.println("sending data");
                if(!m_routeTable.contains(_addr))
                {
                    System.out.println("route was not in table");
                    performRREQ(_addr);
                    try {
                        Thread.sleep(ROUTE_DISCOVERY_TIMEOUT);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                if(m_routeTable.contains(_addr))
                {
                    System.out.println("route is in table");
                    RouteRecord r = m_routeTable.getRouteToTarget(_addr);
                    sendDataOverRoute(new DataPkg(r, _data).toString(), r);
                }
                else
                {
                    System.out.println("route still not in table");
                }
            }
        }.start();

       
    }

    private void discoverNodesInRange()
    {
        while(m_execNodeDiscovery)
        {
            System.out.println("discovering spots in range...");
            m_clientsInRange.removeAllElements();
            broadcastPing();
            try
            {
                Thread.sleep(DISCOVERY_TIMEOUT);
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    private void rcvLoop()
    {
        RadiogramConnection rxConn = null;
        try
        {
            rxConn = (RadiogramConnection) Connector.open(m_recvAddr);
            rxConn.setMaxBroadcastHops(1);
            Radiogram rrg = (Radiogram) rxConn.newDatagram(rxConn.getMaximumLength());
            while(m_execRCVLoop)
            {
                rrg.reset();
                rxConn.receive(rrg);
                processReceivedMessage(rrg);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if(null!=rxConn)
            {
                try
                {
                    rxConn.close();
                } 
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void processReceivedMessage(Radiogram _rrg)
    {
        String msg = "";
        try {
            msg = _rrg.readUTF();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if(msg.equals("ping"))
        {
            sendPingACK(m_baseAddr+_rrg.getAddress()+":"+m_port);
        }
        else if(msg.equals("ack"))
        {
            addClientInRange(_rrg.getAddress());
        }
        else if(msg.startsWith("[RREQ]"))
        {
            System.out.println("[RREQ] discovered");
            RREQPkg pkg = new RREQPkg(msg);
            IITupel tupel = pkg.getIITupel();
            if(m_reqTable.contains(tupel)||pkg.getRouteRecord().getInitiator().equals(getOwnAddress()))
            {
                System.out.println("[RREQ] ignored");
                return;
            }
            else if(pkg.getTargetAddress().equals(getOwnAddress()))
            {
                sendRREPTarget(pkg.getRouteRecord());
                m_reqTable.addTupel(tupel);
            }
            else if(m_routeTable.contains(pkg.getTargetAddress()))
            {
                sendRREPRouteToTarget(pkg.getRouteRecord(), pkg.getTargetAddress());
                m_reqTable.addTupel(tupel);
            }
            else
            {
                System.out.println("forwarding RREQ");
                m_reqTable.addTupel(tupel);
                /*RouteRecord r = pkg.getRouteRecord();
                r.addNodeAddr(getOwnAddress());
                sendBroadcast(new RREQPkg(pkg, r).toString());*/
                forwardRREQ(pkg);
            }
            //m_reqTable.addTupel(tupel);
            System.out.println("added tupel to list");
            //System.out.println(msg);
        }
        else if(msg.startsWith("[RREP]"))
        {
            //System.out.println("RREP discovered");
            RREPPkg pkg = new RREPPkg(msg);
            if(pkg.getRouteRecord().getInitiator().equals(getOwnAddress()))
            {
                indicateReceiveRREP();
                m_routeTable.addRoute(pkg.getRouteRecord().getTarget(), pkg.getRouteRecord());
                System.out.println("added route to table");
            }
            else
            {
                forwardRREP(pkg);
                System.out.println("[RREP] forwarded");
            }
            System.out.println(msg);
        }
        else if(msg.startsWith("[DATA]"))
        {
            DataPkg pkg = new DataPkg(msg);
            if(pkg.getRouteRecord().getTarget().equals(getOwnAddress()))
            {
                System.out.println("###[DATA] received:"+msg.substring(msg.indexOf("]")+1));
            }
            else
            {
                forwardData(pkg);
                System.out.println("[DATA] forwarded");
            }
        }
    }

    private void sendRREPTarget(RouteRecord _rr)
    {
        System.out.println("sending RREP target");
        _rr.addNodeAddr(getOwnAddress());
        RouteRecord r = new RouteRecord(_rr.toString());

        //r = r.reverse();
        sendDataOverRoute(new RREPPkg(_rr).toString(), r.reverse());

    }

    private void sendRREPRouteToTarget(RouteRecord _rr, String _target)
    {
        System.out.println("sending RREP route to target");
        RouteRecord r = _rr.concat(m_routeTable.getRouteToTarget(_target));
        _rr.addNodeAddr(getOwnAddress());
        sendDataOverRoute(new RREPPkg(r).toString(), _rr.reverse());

    }

    private void testSendData()
    {
        while(true)
        {
            sendData("geilo", ADDRESS_START+"0000.6EF0");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }


    private void sendDataOverRoute(String _data, RouteRecord _rr)
    {
        String addr = _rr.getNextHop(getOwnAddress());
        sendDataThreaded(_data, m_baseAddr+addr+":"+m_port);
    }

    private void sendPingACK(String _addr)
    {
        sendDataThreaded("ack", _addr);
    }

    private void broadcastPing()
    {
        sendBroadcast("ping");
    }

    private void performRREQ(String _target)
    {
        indicateCreateRREQ();
        RouteRecord r = new RouteRecord();
        r.addNodeAddr(getOwnAddress());
        m_lastRRQID++;
        sendBroadcast(new RREQPkg(getOwnAddress(), _target, r, m_lastRRQID).toString());
    }
    private void forwardRREQ(RREQPkg _pkg)
    {
        indicateForwardRREQ();
        _pkg.getRouteRecord().addNodeAddr(getOwnAddress());
        sendBroadcast(_pkg.toString());
    }

    private void forwardData(DataPkg _pkg)
    {
        indicateForwardData();
        sendDataOverRoute(_pkg.toString(), _pkg.getRouteRecord());
       // String addr = _pkg.getRouteRecord().getNextHop(getOwnAddress());
        //sendDataThreaded(_pkg.toString(), addr);
    }

    private void forwardRREP(RREPPkg _pkg)
    {
        indicateForwardRREP();
        sendDataOverRoute(_pkg.toString(), _pkg.getRouteRecord().reverse());
    }

    public void sendBroadcast(String _msg)
    {
        sendDataThreaded(_msg, m_broadcastAddr);
    }

    private void sendDataThreaded(final String _msg, final String _addr)
    {

       // new Thread()
         {
         //   public void run ()
            {
                RadiogramConnection txConn = null;
                try
                {
                    txConn = (RadiogramConnection) Connector.open(_addr);
                    txConn.setMaxBroadcastHops(1);
                    

                    Radiogram tdg = (Radiogram) txConn.newDatagram(txConn.getMaximumLength());
                    tdg.reset();
                    tdg.writeUTF(_msg);
                    txConn.send(tdg);
                    //m_sendBuffer = "";

                    //m_blinkCols[ACK_LED] = LEDColor.GREEN;
                }
                catch(NoRouteException _nae)
                {
                    m_routeTable.clear();
                    System.out.println("routing table was cleared after no ack");
                }
                catch (IOException _ioe)
                {
                    System.out.println("send exception (IO):"+_ioe.getMessage());

                    _ioe.printStackTrace();
                    //m_blinkCols[ACK_LED] = LEDColor.RED;
                }
                catch(Exception _e)
                {
                    System.out.println("send exception:"+_e.getMessage());
                }
                finally
                {
                    //m_sendInProgress = false;
                    //m_blinkLEDIdx = ACK_LED;
                    //blink();
                    try
                    {
                        if(txConn!=null)
                        {
                            txConn.close();
                        }
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }

            }
        }//.start();
    }

   /* public void blink()
    {
        new Thread()
        {
            public void run()
            {
                m_leds[m_blinkLEDIdx].setColor(m_blinkCols[m_blinkLEDIdx]);
                m_leds[m_blinkLEDIdx].setOn();
                try {
                    Thread.sleep(m_blinkSleep);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                m_leds[m_blinkLEDIdx].setOff();
            }
        }.start();
    }*/
    public void blink(final int _ledIdx, final LEDColor _col)
    {
        new Thread()
        {
            public void run()
            {
                m_leds[_ledIdx].setColor(_col);
                m_leds[_ledIdx].setOn();
                try {
                    Thread.sleep(BLINK_TIME);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                m_leds[_ledIdx].setOff();
            }
        }.start();
    }

    private void indicateSendData()
    {
        blink(SEND_DATA_LED, SEND_DATA_COL);
    }
    private void indicateCreateRREQ()
    {
        blink(CREATE_RREQ_LED, CREATE_RREQ_COL);
    }

    private void indicateForwardRREQ()
    {
        blink(FORWARD_RREQ_LED, FORWARD_RREQ_COL);
    }

    private void indicateForwardRREP()
    {
        blink(FORWARD_RREP_LED, FORWARD_RREP_COL);
    }

    private void indicateReceiveRREP()
    {
        blink(RECEIVE_RREP_LED, RECEIVE_RREP_COL);
    }

    private void indicateForwardData()
    {
        blink(FORWARD_DATA_LED, FORWARD_DATA_COL);
    }
    /*private void indicate(final LEDColor _col)
    {
        new Thread(){
            public void run()
            {
                for(int i = 0;i<7;i++)
                {
                    m_leds[i].setColor(_col);
                    m_leds[i].setOn();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                for(int i = 0;i<7;i++)
                {
                    m_leds[i].setOff();
                }
            }
        }.start();

    }*/

    public void blinkTest()
    {
        EDemoBoard demoBoard = EDemoBoard.getInstance();
        TriColorLED blinkLed = (TriColorLED) demoBoard.getLEDs()[7];
        blinkLed.setColor(LEDColor.ORANGE);
        while(m_execBlink)
        {
            blinkLed.setOn();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            blinkLed.setOff();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addClientInRange(String _addr)
    {
        if(m_clientsInRange.contains(_addr))
        {
            System.out.println(_addr+" already in list");
        }
        else
        {
            m_clientsInRange.addElement(_addr);
            System.out.println("added "+_addr+" to list of in range nodes");
        }
    }

    private String getOwnAddress()
    {
        String addr = IEEEAddress.toDottedHex(    RadioFactory.
                                            getRadioPolicyManager().
                                            getIEEEAddress());
         return addr;
    }

    public void stop()
    {
        m_execRCVLoop = false;
        m_execNodeDiscovery = false;
        m_execBlink = false;
    }

}
