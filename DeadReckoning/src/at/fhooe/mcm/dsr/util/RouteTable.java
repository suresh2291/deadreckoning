/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.fhooe.mcm.dsr.util;

import java.util.Hashtable;

/**
 *
 * @author Peter
 */
public class RouteTable {

    private Hashtable m_table = new Hashtable();

    public void addRoute(String _target, RouteRecord _route) {
        if (contains(_target)) {
            return;
        }
        m_table.put(_target, _route);
    }

    public boolean contains(String _target) {
        return m_table.containsKey(_target);
    }

    public RouteRecord getRouteToTarget(String _target) {
        return (RouteRecord) m_table.get(_target);
    }

    public void clear() {
        m_table.clear();
    }
}
