package at.fhooe.mcm.dsr.util;

import java.util.Vector;

/**
 * @class RouteRecord
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @version 1.0
 *
 * @brief this class represents a RouteRecord used in DSR
 *
 * This class is a ontainer for storage of a list of node addresses and provides some useful
 * operations needed for DSR
 *
 */
public class RouteRecord
{
    /**@brief a vector containing the stored node addresses*/
    private Vector m_records = new Vector();

    /**
     * instantiates an empty RouteRecord
     */
    public RouteRecord() 
    {
    }

    /**
     * copy constructor for the passed RouteRecord
     * @param _rr the RouteRecord to copy
     */
    public RouteRecord(RouteRecord _rr)
    {
        m_records = _rr.getRecord();
    }

    /**
     * instantiates a new RouteRecord from the passed string representation of
     * a RouteRequest
     * @param _rec the string representation of the RouteRecord
     */
    public RouteRecord(String _rec)
    {
        parseRouteRecordStr(_rec);
    }

    /**
     * adds the passed string as a node address in the list
     * @param _addr the address to store
     */
    public void addNodeAddr(String _addr)
    {
        m_records.addElement(_addr);
    }

    /**
     * provides the vector of node addresses
     * @return the vector of node addresses
     */
    public Vector getRecord()
    {
        return m_records;
    }

    /**
     * concatenates the passed RouteRecord with this RouteRecord and returns the
     * result
     * @param _rec the RouteRecord to concatenate
     * @return the concatenated RouteRecord
     */
    public RouteRecord concat(RouteRecord _rec)
    {
        RouteRecord r = new RouteRecord(this);
        for (int i = 0; i < _rec.getRecord().size(); i++)
        {
            r.addNodeAddr((String) _rec.getRecord().elementAt(i));
        }
        return r;
    }

    /**
     * provides the address of the next node in the list of addresses after
     * the passed address
     * @param _ownAddr the address to get the next hop for
     * @return the address of the next node in the RouteRecord
     */
    public String getNextHop(String _ownAddr)
    {
        for (int i = 0; i < m_records.size() - 1; i++)
        {
            String s = (String) m_records.elementAt(i);
            if (s.equals(_ownAddr))
            {
                return (String) m_records.elementAt(i + 1);
            }
        }
        return "";
    }

    /**
     * provides the address of the first node in the RouteRecord
     * @return the address of the first node in the RouteRecord
     */
    public String getInitiator()
    {
        return (String) m_records.firstElement();
    }

    /**
     * provides the address of the last node in the RouteRecord
     * @return the address of the last node in the RouteRecord
     */
    public String getTarget()
    {
        return (String) m_records.lastElement();
    }

    /**
     * provides the reversion of this RouteRecord
     * @return the reversed RouteRecord
     */
    public RouteRecord reverse()
    {
        RouteRecord res = new RouteRecord();
        for (int i = m_records.size() - 1; i >= 0; i--)
        {
            res.addNodeAddr((String) m_records.elementAt(i));
        }
        return res;
    }

    /**
     * provides a string representation of the RouteRecord
     * @return the string representation of the RouteRecord
     */
    public String toString()
    {
        if (m_records.isEmpty())
        {
            return "";
        }
        StringBuffer b = new StringBuffer((String) m_records.elementAt(0));
        for (int i = 1; i < m_records.size(); i++)
        {
            b.append(",");
            b.append((String) m_records.elementAt(i));
        }
        return b.toString();
    }

    /**
     * parses a string representation of a passed string
     * @param _rec the string representation of a RouteRecord
     */
    private void parseRouteRecordStr(String _rec)
    {
        int divIdx = -2;
        String buff = _rec;
        while (buff.indexOf(",") != -1)
        {
            divIdx = buff.indexOf(",");
            addNodeAddr(buff.substring(0, divIdx));
            buff = buff.substring(divIdx + 1);
        }
        addNodeAddr(buff);
    }
}
