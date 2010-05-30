package at.fhooe.mcm.dsr.util;

import java.util.Vector;

/**
 * @class RouteRecord
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @version 1.0
 *
 * @brief This class represents a RouteRecord used in DSR.
 *
 * This class is a ontainer for storage of a list of node addresses and provides some useful
 * operations needed for DSR.
 *
 */
public class RouteRecord {

    /** @brief A vector containing the stored node addresses. */
    private Vector m_records = new Vector();

    /**
     * @brief Instantiates an empty RouteRecord.
     */
    public RouteRecord() {
    }

    /**
     * @brief Copy constructor for the passed RouteRecord.
     *
     * @param _rr The RouteRecord to copy.
     */
    public RouteRecord(RouteRecord _rr) {
        m_records = _rr.getRecord();
    }

    /**
     * @brief Instantiates a new RouteRecord from the passed string representation of a RouteRequest.
     *
     * @param _rec The string representation of the RouteRecord.
     */
    public RouteRecord(String _rec) {
        parseRouteRecordStr(_rec);
    }

    /**
     * @brief Adds the passed string as a node address in the list.
     *
     * @param _addr The address to store.
     */
    public void addNodeAddr(String _addr) {
        m_records.addElement(_addr);
    }

    /**
     * @brief Provides the vector of node addresses.
     *
     * @return The vector of node addresses.
     */
    public Vector getRecord() {
        return m_records;
    }

    /**
     * @brief Concatenates the passed RouteRecord with this RouteRecord and returns the result.
     *
     * @param _rec The RouteRecord to concatenate.
     * @return The concatenated RouteRecord.
     */
    public RouteRecord concat(RouteRecord _rec) {
        RouteRecord r = new RouteRecord(this);
        for (int i = 0; i < _rec.getRecord().size(); i++) {
            r.addNodeAddr((String) _rec.getRecord().elementAt(i));
        }
        return r;
    }

    /**
     * @brief Provides the address of the next node in the list of addresses after the passed address.
     *
     * @param _ownAddr The address to get the next hop for.
     * @return The address of the next node in the RouteRecord.
     */
    public String getNextHop(String _ownAddr) {
        for (int i = 0; i < m_records.size() - 1; i++) {
            String s = (String) m_records.elementAt(i);
            if (s.equals(_ownAddr)) {
                return (String) m_records.elementAt(i + 1);
            }
        }
        return "";
    }

    /**
     * @brief Provides the address of the first node in the RouteRecord.
     *
     * @return The address of the first node in the RouteRecord.
     */
    public String getInitiator() {
        return (String) m_records.firstElement();
    }

    /**
     * @brief Provides the address of the last node in the RouteRecord.
     * @return The address of the last node in the RouteRecord.
     */
    public String getTarget() {
        return (String) m_records.lastElement();
    }

    /**
     * @brief Provides the reversion of this RouteRecord.
     *
     * @return The reversed RouteRecord.
     */
    public RouteRecord reverse() {
        RouteRecord res = new RouteRecord();
        for (int i = m_records.size() - 1; i >= 0; i--) {
            res.addNodeAddr((String) m_records.elementAt(i));
        }
        return res;
    }

    /**
     * @brief Provides a string representation of the RouteRecord.
     *
     * @return The string representation of the RouteRecord.
     */
    public String toString() {
        if (m_records.isEmpty()) {
            return "";
        }
        StringBuffer b = new StringBuffer((String) m_records.elementAt(0));
        for (int i = 1; i < m_records.size(); i++) {
            b.append(",");
            b.append((String) m_records.elementAt(i));
        }
        return b.toString();
    }

    /**
     * @brief Parses a string representation of a passed string.
     * @param _rec The string representation of a RouteRecord.
     */
    private void parseRouteRecordStr(String _rec) {
        int divIdx = -2;
        String buff = _rec;
        while (buff.indexOf(",") != -1) {
            divIdx = buff.indexOf(",");
            addNodeAddr(buff.substring(0, divIdx));
            buff = buff.substring(divIdx + 1);
        }
        addNodeAddr(buff);
    }
}
