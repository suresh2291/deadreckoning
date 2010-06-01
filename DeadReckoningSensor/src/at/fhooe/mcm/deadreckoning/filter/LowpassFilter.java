package at.fhooe.mcm.deadreckoning.filter;

/**
 * @class LowpassFilter
 * @brief This class provides a lowpass algorithm derived from the Kalman filter.
 *
 * The application for which it is designed is cleaning up multi-dimensional sensor errors,
 * based on sensor noise, user input and measurement.
 * 
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @date 20.05.2010
 * @version 2.0
 */
public class LowpassFilter implements IFilter {

    /** @brief The time constant in seconds in order to determine the cutoff frequency (160Hz ~ 0.00625s) */
    private static final float RC_TIME_CONSTANT = 0.00625f;

    /** @brief The previously observed value. */
    private float[] m_observed;

    /** @brief The filtered value. */
    private float[] m_corrected;


    /**
     * @brief Initializes the filter with some initial values and defines the dimension used.
     *
     * @param _initialValues The values used for initialization.
     * @param _dimension The dimension of the filter.
     */
    public void init(float[] _initialValues, int _dimension) {
       
        m_corrected = new float[_dimension];
        m_observed  = new float[_dimension];
        m_corrected = _initialValues;
    }

    /**
     * @brief Updates the Kalman filter.
     *
     * @param observedValue The value gained by measuring.
     * @param _dtx The time in seconds since the last update.
     */
    public void update(float[] _observedValues, float _dtx) {

        // if dimensions do not match throw an exception
        if (m_observed.length != _observedValues.length) {
            throw new RuntimeException("Array dimensions do not match");
        }

        m_observed = _observedValues;

        // update smoothing factor according to the time passed
        float alpha = _dtx / (RC_TIME_CONSTANT + _dtx);

        for (int i = 0; i < m_observed.length; i++) {
            m_corrected[i] = m_corrected[i] * (1.0f - alpha) + alpha* m_observed[i];
        }
    }

    /**
     * @brief Provides the caller with the filtered values since the last update.
     *
     * @return A float array storing the filtered values.
     */
    public float[] getCorrectedValues() {
        return m_corrected;
    }

    /**
     * @brief Serializes the filter data of the current state to a string.
     *
     * Each value is seperated by a pipe, so it can be parsed by any GUI later.
     *
     * @return A concatenated string carrying the current filter data.
     */
    public String currentStateToString() {

        String temp = "";

        for(int i = 0; i < m_observed.length; i++){
            temp += m_observed[i] + "|";
        }

        for(int i = 0; i < m_corrected.length; i++){
            temp += m_corrected[i] + "|";
        }

        return temp;
    }
}


