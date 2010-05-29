package at.fhooe.mcm.dsr.packages;

import at.fhooe.mcm.dsr.util.IITupel;
import at.fhooe.mcm.dsr.util.RouteRecord;

/**
 * @class RREQPkg
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @version 1.0
 *
 * @brief this class represents a RREQ package used by DSR
 *
 * Container for a RREQ package in DSR. It contains the initiator, the target, a
 * RouteRecord and an id
 *
 */
public class RREQPkg
{
    /**@brief the initiator of the RREQ*/
    private String m_initiator = "";
    /**@brief the target of the RREQ*/
    private String m_target = "";
    /**@brief the route record of the RREQ*/
    private RouteRecord m_route = null;
    /**@brief the ID of the RREQ*/
    private int m_id = -1;

    /**
     * instantiates an empty RREQ package
     */
    public RREQPkg()
    {
        setID(0);
        setInitiator("");
        setRouteRecord(new RouteRecord());
        setTargetAddress("");
    }

    /**
     * instantiates a new RREQ package with passed initiator, target, route record
     * and ID
     * @param _initiator the initiator of the RREQ
     * @param _target the target of the RREQ
     * @param _rr the route record of the RREQ
     * @param _id the ID of the RREQ
     */
    public RREQPkg(String _initiator, String _target, RouteRecord _rr, int _id)
    {
        m_initiator = _initiator;
        m_target = _target;
        m_route = _rr;
        m_id = _id;
    }

    /**
     * instantiates a new RREQ package from the passed string representation of
     * a RREQ package
     * @param _pkg
     */
    public RREQPkg(String _pkg)
    {
        parseRREQPkg(_pkg);
    }

    /**
     * copy constructor of a RREQ package
     * @param _pkg the RREQ package to copy
     */
    public RREQPkg(RREQPkg _pkg)
    {
        setInitiator(_pkg.getInitiator());
        setTargetAddress(_pkg.getTargetAddress());
        setRouteRecord(_pkg.getRouteRecord());
        setID(_pkg.getID());
    }

    /**
     * copy constructor of a RREQ package with altered RouteRecord
     * @param _pkg the RREQ package to copy
     * @param _rr the new RouteRecord
     */
    public RREQPkg(RREQPkg _pkg, RouteRecord _rr)
    {
        setInitiator(_pkg.getInitiator());
        setTargetAddress(_pkg.getTargetAddress());
        setRouteRecord(_rr);
        setID(_pkg.getID());
    }

    /**
     * provides the ID of the package
     * @return the ID of the package
     */
    public int getID()
    {
        return m_id;
    }

    /**
     * sets the passed integer as ID for the package
     * @param _id the integer representing the ID
     */
    public void setID(int _id)
    {
        this.m_id = _id;
    }

    /**
     * provides the initiator of the package
     * @return the address of the initiator of the package
     */
    public String getInitiator()
    {
        return m_initiator;
    }

    /**
     * sets the passed string as initiator of the package
     * @param _initiator the address of the initiator
     */
    public void setInitiator(String _initiator)
    {
        this.m_initiator = _initiator;
    }

    /**
     * provides the RouteRecord of the RREQ package
     * @return the RouteRecord of the RREQ package
     */
    public RouteRecord getRouteRecord()
    {
        return m_route;
    }

    /**
     * sets the passed RouteRecord as the RouteRecord of the RREQ
     * @param _route the RouteRecord of the RREQ
     */
    public void setRouteRecord(RouteRecord _route)
    {
        this.m_route = _route;
    }

    /**
     * provides the target address of the RREQ package
     * @return the target address of the RREQ package
     */
    public String getTargetAddress()
    {
        return m_target;
    }

    /**
     * sets the target address of a RREQ package
     * @param _target the target address of a RREQ package
     */
    public void setTargetAddress(String _target)
    {
        this.m_target = _target;
    }

    /**
     * provides the IITupel of a RREQ package
     * @return the IITupel of the RREQ package
     */
    public IITupel getIITupel()
    {
        return new IITupel(getInitiator(), getID());
    }

    /**
     * provides a string representation of the RREQ package
     * @return the string representation of the RREQ package
     */
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

    /**
     * @brief parses the string representation of a RREQ package
     * assures the passed string is a representation of a RREQ package and
     * extracts the initiator, the target, the RouteRecord and the ID from the package
     * @param _pkg the string representation of the RREQ package
     */
    private void parseRREQPkg(String _pkg)
    {
        int nextDividerIdx = 0;
        String buff = _pkg;
        if (!buff.startsWith("[RREQ]"))
        {
            return;
        }

        int headerEndIdx = buff.indexOf("]");
        buff = buff.substring(headerEndIdx + 1);
        nextDividerIdx = buff.indexOf(";");
        setInitiator(buff.substring(0, nextDividerIdx));

        buff = buff.substring(nextDividerIdx + 1);
        nextDividerIdx = buff.indexOf(";");
        setTargetAddress(buff.substring(0, nextDividerIdx));

        buff = buff.substring(nextDividerIdx + 1);
        nextDividerIdx = buff.indexOf(";");
        String route = buff.substring(0, nextDividerIdx);
        setRouteRecord(new RouteRecord(route));

        buff = buff.substring(nextDividerIdx + 1);
        setID(Integer.valueOf(buff).intValue());
    }
}
