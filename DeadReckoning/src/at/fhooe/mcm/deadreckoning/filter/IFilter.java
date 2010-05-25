package at.fhooe.mcm.deadreckoning.filter;

/**
 * @class IFilter
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @date 23.05.2010
 * @version 1.0
 *
 * @brief This interface can be used to implement different filters.
 *
 * Filters can be used to optimize sensor data and to improve reliability. Heuristic
 * filters require initial values and updates with measured values to predict future
 * values.
 */
public interface IFilter {
    /**
     * @brief Initializes a filter with some initial values.
     *
     * As each filter uses different parameters, they have to be handled
     * in the derived implementation of a filter.
     *
     * @param initialValues The initial values for each dimension used.
     * @param dimension The amount of dimensions used for filtering.
     */
    void init(float[] initialValues, int dimension);

    /**
     * @brief Updates a filter each cycle.
     *
     * This method uses measured value to improve the prediction of a filter and
     * generates new corrected values each time the method is called.
     *
     * @param observedValue The observed value needed to update the prediction.
     */
    void update(float[] observedValue);

    /**
     * @brief Provides the caller with the currently filtered values.
     *
     * @return A float array storing the corrected values since the last update.
     */
    float[] getCorrectedValues();
}
