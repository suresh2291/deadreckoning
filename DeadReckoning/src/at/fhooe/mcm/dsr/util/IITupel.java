/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.fhooe.mcm.dsr.util;

/**
 *
 * @author Peter
 */
public class IITupel {

    private String m_initiator = "";
    private Integer m_id = new Integer(-1);

    public IITupel(String _initiator, int _id) {
        m_initiator = _initiator;
        m_id = new Integer(_id);
    }

    public Integer getID() {
        return m_id;
    }

    public void setID(int _id) {
        this.m_id = new Integer(_id);
    }

    public String getInitiator() {
        return m_initiator;
    }

    public void setInitiator(String _initiator) {
        this.m_initiator = _initiator;
    }

    public boolean equals(Object _obj) {
        if (!(_obj instanceof IITupel)) {
            return false;
        }
        IITupel _t = (IITupel) _obj;
        return _t.getID().equals(getID()) && _t.getInitiator().equals(getInitiator());
    }

    public String toString() {
        return m_initiator + ":" + m_id;
    }
}
