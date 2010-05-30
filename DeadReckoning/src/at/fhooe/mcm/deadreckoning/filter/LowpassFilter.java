package at.fhooe.mcm.deadreckoning.filter;

/**
 * @class KalmanFilter
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @date 20.05.2010
 * @version 2.0
 *
 * @brief This class provides a lowpass algorithm derived from the Kalman filter.
 *
 * The application for which it is designed is cleaning up multi-dimensional sensor errors,
 * based on sensor noise, user input and measurement.
 */
public class LowpassFilter implements IFilter {

    /** @brief The time constant in seconds in order to determine the cutoff frequency. */
    private static final float RC_TIME_CONSTANT = 0.25f;

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
        if (m_corrected.length != _observedValues.length) {
            throw new RuntimeException("Array dimensions do not match");
        }

        // update smoothing factor according to the time passed
        float alpha = _dtx / (RC_TIME_CONSTANT + _dtx);

        for (int i = 0; i < _observedValues.length; i++) {
            m_corrected[i] = m_corrected[i] * (1.0f - alpha) + alpha* _observedValues[i];
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
}


