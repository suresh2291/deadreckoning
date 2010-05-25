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
public class LowpassFilter implements IFilter{

    /** @brief Process noise covariance. */
    private float[] correctedVar;

    /** @brief Measurement noise covariance. */
    private float[] noiseVar;

    /** @brief The filtered value. */
    private float[] corrected;

    /** @brief Estimation error covariance. */
    private float[] predictedvar;

    /** @brief Kalman gain. */
    private float[] kalman;

    /**
     * @brief Initializes the filter with some initial values and defines the dimension used.
     *
     * @param initialValues The values used for initialization.
     * @param dimension The dimension of the filter.
     */
    public void init(float[] initialValues, int dimension) {
        correctedVar = new float[dimension];
        noiseVar = new float[dimension];
        corrected = new float[dimension];
        predictedvar = new float[dimension];
        kalman = new float[dimension];

        for(int i = 0; i < dimension; i++) {
            correctedVar[i] = 4f;
            noiseVar[i] = 1f;
            predictedvar[1] = 1f;
        }

        corrected = initialValues;
    }

      /**
     * @brief Updates the Kalman filter.
     *
     * @param observedValue The value gained by measuring.
     */
    public void update(float[] observedValues) {

        // if dimensions do not match throw an exception
        if(corrected.length != observedValues.length)
            throw new RuntimeException("Array dimensions do not match");

        for(int i = 0; i < predictedvar.length; i++) {

            predictedvar[i] = predictedvar[i] + correctedVar[i];
                       
            // calculate the Kalman gain
            kalman[i] = predictedvar[i] / (predictedvar[i] + noiseVar[i]);

            // update the measurement
            corrected[i] = corrected[i] + kalman[i] * (observedValues[i] - corrected[i]);

            // update the prediction
            predictedvar[i] = (1f - kalman[i]) * predictedvar[i];
        }
    }

    /**
     * @brief Provides the caller with the filtered values since the last update.
     *
     * @return A float array storing the filtered values.
     */
    public float[] getCorrectedValues() {
        return corrected;
    }
}


