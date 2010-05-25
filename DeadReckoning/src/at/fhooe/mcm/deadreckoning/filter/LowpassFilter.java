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

    /** @brief Process noise covariance. */
    private float[] m_correctedVar;
    /** @brief Measurement noise covariance. */
    private float[] m_noiseVar;
    /** @brief The filtered value. */
    private float[] m_corrected;
    /** @brief Estimation error covariance. */
    private float[] m_predictedvar;
    /** @brief Kalman gain. */
    private float[] m_kalman;

    /**
     * @brief Initializes the filter with some initial values and defines the dimension used.
     *
     * @param _initialValues The values used for initialization.
     * @param _dimension The dimension of the filter.
     */
    public void init(float[] _initialValues, int _dimension) {
        m_correctedVar = new float[_dimension];
        m_noiseVar = new float[_dimension];
        m_corrected = new float[_dimension];
        m_predictedvar = new float[_dimension];
        m_kalman = new float[_dimension];

        for (int i = 0; i < _dimension; i++) {
            m_correctedVar[i] = 4f;
            m_noiseVar[i] = 1f;
            m_predictedvar[1] = 1f;
        }

        m_corrected = _initialValues;
    }

    /**
     * @brief Updates the Kalman filter.
     *
     * @param observedValue The value gained by measuring.
     */
    public void update(float[] _observedValues) {

        // if dimensions do not match throw an exception
        if (m_corrected.length != _observedValues.length) {
            throw new RuntimeException("Array dimensions do not match");
        }

        for (int i = 0; i < m_predictedvar.length; i++) {

            m_predictedvar[i] = m_predictedvar[i] + m_correctedVar[i];

            // calculate the Kalman gain
            m_kalman[i] = m_predictedvar[i] / (m_predictedvar[i] + m_noiseVar[i]);

            // update the measurement
            m_corrected[i] = m_corrected[i] + m_kalman[i] * (_observedValues[i] - m_corrected[i]);

            // update the prediction
            m_predictedvar[i] = (1f - m_kalman[i]) * m_predictedvar[i];
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


