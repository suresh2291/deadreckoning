/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.fhooe.mcm.dsr.util;

import java.util.Vector;

/**
 *
 * @author Peter
 */
public class RouteRecord
{
    private Vector m_records = new Vector();

    public RouteRecord()
    {
        
    }
    public RouteRecord(RouteRecord _rr)
    {
        m_records = _rr.getRecord();
    }
    public RouteRecord(String _rec)
    {
        parseRouteRecordStr(_rec);
    }


    public void addNodeAddr(String _addr)
    {
        m_records.addElement(_addr);
    }

    public Vector getRecord()
    {
        return m_records;
    }

    public RouteRecord concat(RouteRecord _rec)
    {
        RouteRecord r = new RouteRecord(this);
        for(int i =0; i<_rec.getRecord().size();i++)
        {
            r.addNodeAddr((String)_rec.getRecord().elementAt(i));
        }
        return r;
    }

    public String getNextHop(String _ownAddr)
    {
        for(int i =0;i<m_records.size()-1;i++)
        {
            String s = (String)m_records.elementAt(i);
            if(s.equals(_ownAddr))
            {
                return (String)m_records.elementAt(i+1);
            }
        }
        return "";
    }
    public String getInitiator()
    {
        return (String)m_records.firstElement();
    }
    public String getTarget()
    {
        return (String)m_records.lastElement();
    }

    public RouteRecord reverse()
    {
        RouteRecord res = new RouteRecord();
        for(int i = m_records.size()-1;i>=0;i--)
        {
            res.addNodeAddr((String)m_records.elementAt(i));
        }
        return res;
    }

    public String toString()
    {
        if(m_records.isEmpty())
        {
            return "";
        }
        StringBuffer b = new StringBuffer((String)m_records.elementAt(0));
        for(int i = 1;i<m_records.size();i++)
        {
            b.append(",");
            b.append((String)m_records.elementAt(i));
        }
        return b.toString();
    }

    private void parseRouteRecordStr(String _rec)
    {
        int divIdx = -2;
        String buff = _rec;
        while(buff.indexOf(",")!=-1)
        {
            divIdx = buff.indexOf(",");
            addNodeAddr(buff.substring(0, divIdx));
            buff = buff.substring(divIdx+1);
        }
        addNodeAddr(buff);
    }
}
