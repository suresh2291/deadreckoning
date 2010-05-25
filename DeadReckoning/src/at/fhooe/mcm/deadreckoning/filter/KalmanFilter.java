/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.fhooe.mcm.deadreckoning.filter;

/**
 * @class KalmanFilter
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @date 20.05.2010
 * @version 2.0
 *
 * @brief This class provides a recursive prediction algorithm based on the Kalman filter.
 *
 * The application for which it is designed is cleaning up multi-dimensional sensor errors,
 * based on sensor noise, user input and measurement.
 */
public class KalmanFilter implements IFilter{

    /** @brief Noise variance estimation in percent. */
    private static float percentvar = 0.05f;

    /** @brief Filter gain. */
    private static float gain = 0.8f;

    /** @brief Noise variance. */
    float[] noisevar;

    /** @brief Corrected/filtered value. */
    float[] corrected;

    /** @brief Predicted variance. */
    float[] predictedvar;

    /** @brief Observed value due to measurement. */
    float[] observed;

    /** @brief The Kalman gain. */
    float[] kalman;

    /** @brief The corrected variance. */
    float[] correctedvar;

    /** @brief The predicted value. */
    float[] predicted;

    /**
     * @brief Initializes the filter with some initial values and defines the dimension used.
     *
     * @param initialValues The values used for initialization.
     * @param dimension The dimension of the filter.
     */
    public void init(float[] initialValues, int dimension) {

        noisevar     = new float[dimension];
        corrected    = new float[dimension];
        predictedvar = new float[dimension];
        observed     = new float[dimension];
        kalman       = new float[dimension];
        correctedvar = new float[dimension];
        predicted    = new float[dimension];

        for(int i = 0; i < dimension; i++)
            noisevar[i] = percentvar;

        predictedvar = noisevar;
        predicted = initialValues;
    }

    /**
     * @brief Updates the Kalman filter.
     *
     * @param observedValue The value gained by measuring.
     */
    public void update(float[] observedValue) {

        // if dimensions do not match throw an exception
        if(observedValue.length != observed.length)
            throw new RuntimeException("Array dimensions do not match");

        observed = observedValue;

        // compute the Kalman gain for each dimension
        for(int i = 0; i < kalman.length; i++)
            kalman[i] = predictedvar[i] / (predictedvar[i] + noisevar[i]);

        // update the sensor prediction with the measurement for each dimension
        for(int i = 0; i < corrected.length; i++)
            corrected[i] = gain * predicted[i] + (1.0f - gain) * observed[i] + kalman[i] * (observed[i] - predicted[i]);

        // update the variance estimation
        for(int i = 0; i < correctedvar.length; i++)
            correctedvar[i] = predictedvar[i] * (1.0f - kalman[i]);

        // predict next variances and values
        predictedvar = correctedvar;
        predicted = corrected;
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
