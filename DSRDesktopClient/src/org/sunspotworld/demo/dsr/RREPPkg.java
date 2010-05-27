/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunspotworld.demo.dsr;


/**
 *
 * @author Peter
 */
public class RREPPkg {

    private RouteRecord m_rec = new RouteRecord("A,B,C,D");

    public RREPPkg(RouteRecord _rr) {
        m_rec = _rr;
    }

    public RREPPkg(String _pkg) {
        parseRREP(_pkg);
    }

    public String toString() {
        StringBuffer b = new StringBuffer("[RREP]");
        b.append(m_rec.toString());
        return b.toString();
    }

    private void parseRREP(String _pkg) {
        String buff = _pkg;
        if (!buff.startsWith("[RREP]")) {
            return;
        }
        int headerEndIdx = buff.indexOf("]");
        buff = buff.substring(headerEndIdx + 1);
        setRouteRecord(new RouteRecord(buff));
    }

    public void setRouteRecord(RouteRecord _rec) {
        this.m_rec = _rec;
    }

    public RouteRecord getRouteRecord() {
        return m_rec;
    }
}
