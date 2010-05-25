/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.fhooe.mcm.dsr.packages;

import at.fhooe.mcm.dsr.util.IITupel;
import at.fhooe.mcm.dsr.util.RouteRecord;


/**
 *
 * @author Peter
 */
public class RREQPkg
{
    private String m_initiator = "";
    private String m_target = "";
    private RouteRecord m_route = null;
    private int m_id = -1;

    public RREQPkg()
    {
        setID(0);
        setInitiator("");
        setRouteRecord(new RouteRecord());
        setTargetAddress("");
    }
    public RREQPkg(String _initiator, String _target, RouteRecord _rRec, int _id)
    {
        m_initiator = _initiator;
        m_target = _target;
        m_route = _rRec;
        m_id = _id;
    }
    public RREQPkg(String _pkg)
    {
       parseRREQPkg(_pkg);
       System.out.println(toString());
    }

    public RREQPkg(RREQPkg _pkg)
    {
        setInitiator(_pkg.getInitiator());
        setTargetAddress(_pkg.getTargetAddress());
        setRouteRecord(_pkg.getRouteRecord());
        setID(_pkg.getID());
    }

    public RREQPkg(RREQPkg _pkg, RouteRecord _rr)
    {
        setInitiator(_pkg.getInitiator());
        setTargetAddress(_pkg.getTargetAddress());
        setRouteRecord(_rr);
        setID(_pkg.getID());
    }

    public int getID() {
        return m_id;
    }

    public void setID(int _id) {
        this.m_id = _id;
    }

    public String getInitiator() {
        return m_initiator;
    }

    public void setInitiator(String _initiator) {
        this.m_initiator = _initiator;
    }

    public RouteRecord getRouteRecord() {
        return m_route;
    }

    public void setRouteRecord(RouteRecord _route) {
        this.m_route = _route;
    }

    public String getTargetAddress() {
        return m_target;
    }

    public void setTargetAddress(String _target) {
        this.m_target = _target;
    }

    public IITupel getIITupel()
    {
        return new IITupel(getInitiator(), getID());
    }

    public String toString()
    {
        StringBuffer b = new StringBuffer("[RREQ]");
        b.append(m_initiator);
        b.append(";");
        b.append(m_target);
        b.append(";");
        b.append(m_route.toString());
        b.append(";");
        b.append(m_id);
        return b.toString();
    }

    private void parseRREQPkg(String _pkg)
    {
        int nextDividerIdx = 0;
        String buff = _pkg;
        if(!buff.startsWith("[RREQ]"))
        {
            return;
        }

        int headerEndIdx = buff.indexOf("]");
        buff = buff.substring(headerEndIdx+1);
        nextDividerIdx = buff.indexOf(";");
        setInitiator(buff.substring(0, nextDividerIdx));

        buff = buff.substring(nextDividerIdx+1);
        nextDividerIdx = buff.indexOf(";");
        setTargetAddress(buff.substring(0,nextDividerIdx));

        buff = buff.substring(nextDividerIdx+1);
        nextDividerIdx = buff.indexOf(";");
        String route = buff.substring(0,nextDividerIdx);
        setRouteRecord(new RouteRecord(route));

        buff = buff.substring(nextDividerIdx+1);
        setID(Integer.valueOf(buff).intValue());
    }


}
