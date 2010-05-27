/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunspotworld.demo.dsr;

import java.util.Vector;

/**
 *
 * @author Peter
 */
public class RequestTable {

    private Vector m_requests = new Vector();

    public void addTupel(IITupel _tupel) {
        m_requests.addElement(_tupel);
    }

    public boolean contains(IITupel _tupel) {
        //System.out.println("checking tupel:"+_tupel.toString());
        for (int i = 0; i < m_requests.size(); i++) {
            //System.out.println("against:"+((IITupel)m_requests.elementAt(i)).toString());
            if (((IITupel) m_requests.elementAt(i)).equals(_tupel)) {
                return true;
            }
        }
        return false;
    }

    public void remove(IITupel _tupel) {
        m_requests.removeElement(_tupel);
    }
}
