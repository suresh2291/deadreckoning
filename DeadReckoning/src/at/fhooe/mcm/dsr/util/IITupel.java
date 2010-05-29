package at.fhooe.mcm.dsr.util;

/**
 * @class at.fhooe.mcm.dsr.util.IITupel
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @version 1.0
 *
 * @brief this class represents an IITupel package used by DSR
 *
 * An IITupel consists of the initiator and the ID of a RREQ package. That
 * information is used to determine whether a RREQ package is processed or
 * discarded
 *
 */
public class IITupel
{

    /**@brief the initiator of the IITupel*/
    private String m_initiator = "";
    /**@brief the ID of the IITupel*/
    private Integer m_id = new Integer(-1);

    /**
     * instantiates a new IITupel with passed initiator and ID
     * @param _initiator the initiator of the IITupel
     * @param _id the ID of the IITupel
     */
    public IITupel(String _initiator, int _id)
    {
        m_initiator = _initiator;
        m_id = new Integer(_id);
    }

    /**
     * provides the ID of the IITupel
     * @return the ID of the IITupel
     */
    public Integer getID()
    {
        return m_id;
    }

    /**
     * sets the passed integer as ID of the IITupel
     * @param _id the ID of the IITupel
     */
    public void setID(int _id)
    {
        this.m_id = new Integer(_id);
    }

    /**
     * provides the initiator of the IITupel
     * @return the initiator of the IITupel
     */
    public String getInitiator()
    {
        return m_initiator;
    }

    /**
     * sets the passed string as initiator of the IITupel
     * @param _initiator the initiator of the IITupel
     */
    public void setInitiator(String _initiator)
    {
        this.m_initiator = _initiator;
    }

    /**
     * overrides java.lang.Object.equals(Object obj) for comparison of two
     * IITupels
     * @param _obj the object to compare with the IITupel
     * @return <code>true</code> if initiator and ID of a passed IITupel are
     * equal to this IITupel, <code>false</code> otherwise
     */
    public boolean equals(Object _obj)
    {
        if (!(_obj instanceof IITupel)) {
            return false;
        }
        IITupel _t = (IITupel) _obj;
        return _t.getID().equals(getID()) && _t.getInitiator().equals(getInitiator());
    }

    public int hashCode()
    {
        int hash = 5;
        hash = 67 * hash + (this.m_initiator != null ? this.m_initiator.hashCode() : 0);
        hash = 67 * hash + (this.m_id != null ? this.m_id.hashCode() : 0);
        return hash;
    }

    /**
     * provides the string representation of an IITupel
     * @return the string representation of an IITupel
     */
    public String toString()
    {
        return m_initiator + ":" + m_id;
    }
}
