package at.fhooe.mcm.dsr.util;

import java.util.Vector;

/**
 * @class RequestTable
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @version 1.0
 *
 * @brief this class represents an RequestTable for storage of IITupels
 *
 * Container for IItupels of several RREQs
 *
 */
public class RequestTable
{

    /**@brief a vector storing the IITupels of several RREQs*/
    private Vector m_requests = new Vector();

    /**
     * adds the passed IITupel to the vector of IITupels
     * @param _tupel the IITupel to add
     */
    public void addTupel(IITupel _tupel)
    {
        m_requests.addElement(_tupel);
    }

    /**
     * checks whether one of the stored IITupels is equal to the passed IITupel
     * @param _tupel the IITupel to check for
     * @return <code>true</code> if one of the stored IITupels is equal to the
     * passed IITupel, <code>false</code> otherwise
     */
    public boolean contains(IITupel _tupel)
    {
        for (int i = 0; i < m_requests.size(); i++)
        {
            if (((IITupel) m_requests.elementAt(i)).equals(_tupel)) {
                return true;
            }
        }
        return false;
    }

    /**
     * removes the passed IITupel from the stored IITupels
     * @param _tupel the IITupel to be removed
     */
    public void remove(IITupel _tupel)
    {
        m_requests.removeElement(_tupel);
    }
}
