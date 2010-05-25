/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.fhooe.mcm.dsr.packages;

import at.fhooe.mcm.dsr.util.RouteRecord;

/**
 *
 * @author Peter
 */
public class DataPkg {

    private RouteRecord m_rec = new RouteRecord();
    private String m_data = "";

    public DataPkg(RouteRecord _rr, String _data) {
        m_rec = _rr;
        m_data = _data;
    }

    public DataPkg(String _pkg) {
        parseDataPkg(_pkg);
    }

    private void parseDataPkg(String _pkg) {
        int nextDividerIdx = 0;
        String buff = _pkg;
        if (!buff.startsWith("[DATA]")) {
            return;
        }

        int headerEndIdx = buff.indexOf("]");
        buff = buff.substring(headerEndIdx + 1);
        //System.out.println(buff);
        nextDividerIdx = buff.indexOf(";");
        String route = buff.substring(0, nextDividerIdx);
        //System.out.println(route);
        setRouteRecord(new RouteRecord(route));

        buff = buff.substring(nextDividerIdx + 1);
        setData(buff);

    }

    public String toString() {
        StringBuffer b = new StringBuffer("[DATA]");
        b.append(getRouteRecord().toString());
        b.append(";");
        b.append(getData());
        return b.toString();
    }

    public String getData() {
        return m_data;
    }

    public void setData(String _data) {
        this.m_data = _data;
    }

    public RouteRecord getRouteRecord() {
        return m_rec;
    }

    public void setRouteRecord(RouteRecord _rec) {
        this.m_rec = _rec;
    }
}
