package at.fhooe.mcm.dsr.util;

import java.util.Hashtable;

/**
 * @class RouteTable
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @version 1.0
 *
 * @brief this class represents a RouteTable used in DSR
 *
 * This class is a ontainer for storage of a list of <target address, RouteRecord>
 * tupels in a hashtable
 *
 */
public class RouteTable
{
    /**@brief the hashtable for storing the <target address, RouteRecord> tupels*/
    private Hashtable m_table = new Hashtable();

    /**
     * adds a <target address, RouteRecord> tupel to the RoutingTable
     * @param _target the target address to add
     * @param _route the RouteRecord to add
     */
    public void addRoute(String _target, RouteRecord _route)
    {
        if (contains(_target))
        {
            return;
        }
        m_table.put(_target, _route);
    }

    /**
     * checks whether the passed target address is contained in the RouteTable
     * @param _target the target address to check for
     * @return <code>true</code> if the RouteRecord to the target address is
     * stored in the RouteTable, <code>false</code> otherwise
     */
    public boolean contains(String _target)
    {
        return m_table.containsKey(_target);
    }

    /**
     * provides the RouteRecord to a passed target address
     * @param _target the target to get the RouteRecord for
     * @return the RouteRecord to the specified target
     */
    public RouteRecord getRouteToTarget(String _target)
    {
        return (RouteRecord) m_table.get(_target);
    }

    /**
     * clears the whole RouteTable
     */
    public void clear()
    {
        m_table.clear();
    }
}
