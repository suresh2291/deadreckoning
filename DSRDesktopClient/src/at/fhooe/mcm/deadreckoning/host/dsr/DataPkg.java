package at.fhooe.mcm.deadreckoning.host.dsr;

/**
 * @class DataPkg
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @version 1.0
 *
 * @brief This class represents a data package used by DSR.
 *
 * Container for a data package in DSR. It contains the route from initiator
 * to target and the actual data.
 */
public class DataPkg {

    /**@brief The route from the initiator to the target of the data. */
    private RouteRecord m_rec = new RouteRecord();

    /**@brief The actual data. */
    private String m_data = "";

    /**
     * @brief Instantiates a new data package with passed route and data.
     *
     * @param _rr The route for the data.
     * @param _data The actual data.
     */
    public DataPkg(RouteRecord _rr, String _data) {
        m_rec = _rr;
        m_data = _data;
    }

    /**
     * @brief Istantiates a new data package from the passed string representation of the data package.
     *
     * @param _pkg The string representation of the data package.
     */
    public DataPkg(String _pkg) {
        parseDataPkg(_pkg);
    }

    /**
     * @brief Parses the string representation of a data package.
     *
     * Assures that the passed string is a representation of a data package and
     * extracts RouteRecord and data from the package.
     *
     * @param _pkg The string representation of the data package.
     */
    private void parseDataPkg(String _pkg) {
        int nextDividerIdx = 0;
        String buff = _pkg;
        if (!buff.startsWith("[DATA]")) {
            return;
        }

        int headerEndIdx = buff.indexOf("]");
        buff = buff.substring(headerEndIdx + 1);

        nextDividerIdx = buff.indexOf(";");
        String route = buff.substring(0, nextDividerIdx);

        setRouteRecord(new RouteRecord(route));

        buff = buff.substring(nextDividerIdx + 1);
        setData(buff);

    }

    /**
     * @brief Creates the string representation of a data package.
     *
     * Creates the string representation of a data package by adding the string
     * "[DATA]", the RouteRecord of the data and the actual data to the result.
     *
     * @return The string representation of the data package.
     */
    public String toString() {
        StringBuffer b = new StringBuffer("[DATA]");
        b.append(getRouteRecord().toString());
        b.append(";");
        b.append(getData());
        return b.toString();
    }

    /**
     * @brief Provides the data from the package.
     *
     * @return A string representation of the package data.
     */
    public String getData() {
        return m_data;
    }

    /**
     * @brief Sets the passed string as data for the package.
     *
     * @param _data The string representing the data.
     */
    public void setData(String _data) {
        this.m_data = _data;
    }

    /**
     * @brief Provides the route to the data.
     *
     * @return The route to the data.
     */
    public RouteRecord getRouteRecord() {
        return m_rec;
    }

    /**
     * @brief Sets the passed RouteRecord as RouteRecord for the package.
     *
     * @param _rec The RouteRecord of the data.
     */
    public void setRouteRecord(RouteRecord _rec) {
        this.m_rec = _rec;
    }
}
