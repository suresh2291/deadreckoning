package at.fhooe.mcm.deadreckoning.sensor;

import at.fhooe.mcm.deadreckoning.filter.IFilter;
import at.fhooe.mcm.deadreckoning.filter.KalmanFilter;
import at.fhooe.mcm.deadreckoning.filter.LowpassFilter;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import java.io.IOException;

/**
 * @class InertialSensor
 * @brief This class provides a deadreckoning algorithm based on inertial navigation techniques.
 *
 * This algorithm uses accelerometers to not calculate exact positions but to provide accurate
 * m_distance information. This algorithm only uses two axis of a accelerometer and does not depend
 * on gyroscopes. For accuracy multiple filtering strategies are used.
 *
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @date 12.05.2010
 * @version 2.0
 */
public class InertialSensor {

    /** @brief The earth gravity in m/s² is calculated by mass(earth)/radius(earth)². */
    private static final float EARTH_ACCELERATION = 9.812865328f;

    /** @brief The size of the discrimination window used to correct accelerations. */
    private static final float DISCRIMINATION_SIZE = 2.0f;

    /** @brief A threshold to indicate movement ends. */
    private static final float SLOWDOWN_THRESHOLD = 15.0f;

    /** @brief The number of samples used for zero threshold estimation. */
    private static final int NO_CALIBRATION_SAMPLES = 32;

    /** @brief The interface to access the Sun Spot's accelerometer. */
    private IAccelerometer3D m_accel;

    /** @brief A counter to indicate how many calibration samples have been taken. */
    private int m_calibrationCount;
    
    /** @brief Storage for the current and the previous x-acceleration. */
    private float[] m_accelerationX;
    
    /** @brief Storage for the current and the previous y-acceleration. */
    private float[] m_accelerationY;

    /** @brief Storage for the current and the previous x-velocity. */
    private float[] m_velocityX;

    /** @brief Storage for the current and the previous y-velocity. */
    private float[] m_velocityY;

    /** @brief Storage for the current and the previous x-position. */
    private float[] m_positionX;

    /** @brief Storage for the current and the previous y-position. */
    private float[] m_positionY;

    /** @brief Filter interface for a Kalman filter. */
    private IFilter m_kalmanFilter;

    /** @brief Filter interface for a Kalman based lowpass filter. */
    private IFilter m_lowpassFilter;

    /** @brief Last measured raw x-acceleration. */
    private float m_xAccelSample;

    /** @brief Last measured raw y-acceleration. */
    private float m_yAccelSample;

    /** @brief Last measured raw y-acceleration. */
    private float m_zAccelSample;

    /** @brief Counter to detect movement stops on the x-axis. */
    private float m_cntX;

    /** @brief Counter to detect movement stops on the y-axis. */
    private float m_cntY;

    /** @brief The overall m_distance in meters. */
    private float m_distance;

    /** @brief The sensor offset for the x-axis. */
    private float m_offsetX;

    /** @brief The sensor offset for the y-axis. */
    private float m_offsetY;

    /** @brief The sensor offset for the y-axis. */
    private float m_offsetZ;

    /** @brief The frame gap since the last update in ms. */
    private float m_frameLength;

    /** @brief A flag to indicate if the the sensor is calibrated. */
    private boolean m_isCalibrated;
    
    /**
     * @brief Creates a new instance of an <code>InertialSensor</code>.
     */
    public InertialSensor() {
        m_accelerationX = new float[2];
        m_accelerationY = new float[2];
        m_velocityX = new float[2];
        m_velocityY = new float[2];
        m_positionX = new float[2];
        m_positionY = new float[2];

        m_xAccelSample = 0;
        m_yAccelSample = 0;
        m_zAccelSample = 0;
        m_cntX = 0;
        m_cntY = 0;

        m_distance = 0;

        m_offsetX = 0;
        m_offsetY = 0;

        m_isCalibrated = false;

        m_accel = EDemoBoard.getInstance().getAccelerometer();
        m_kalmanFilter = new KalmanFilter();
        m_lowpassFilter = new LowpassFilter();
    }

    /**
     * @brief Calibrates the sensor.
     *
     * This method takes 1024 samples of sensor values and provides a filtered
     * value by averaging the taken samples.
     *
     * @throws IOException
     */
    private void calibrate(int _noSamples) throws IOException {

        getSensorValues();
        m_offsetX += m_xAccelSample;
        m_offsetY += m_yAccelSample;

        if(m_calibrationCount == _noSamples) {
            m_offsetX /= (float)_noSamples;
            m_offsetY /= (float)_noSamples;
            m_isCalibrated = true;
        }

        m_calibrationCount++;
    }

    /**
     * @brief Initializes the sensor.
     *
     * This method initializes the used filters and sets the filters dimensions. For
     * accurate initial values the sensor has to be calibrated first. Calibration
     *
     * @throws IOException
     */
    public void init() throws IOException {
        m_kalmanFilter.init(new float[]{m_xAccelSample - m_offsetX, m_yAccelSample - m_offsetY}, 2);
        m_lowpassFilter.init(new float[]{m_positionX[1], m_positionY[1]}, 2);
    }

    /**
     * @brief Updates the deadreckoning system.
     *
     * This method updates current m_distance by filtering newly observed values,
     * looks for movement ends and re-calculates the m_distance.
     *
     * @param _dt The time between the previous and the current update cylce in seconds.
     * @throws IOException
     */
    public void update(float _dt) throws IOException {

        m_frameLength = _dt;

        if (!m_isCalibrated) {
            calibrate(NO_CALIBRATION_SAMPLES);
        } else {
            getSensorValues();
            filterAcceleration(_dt);
            integrate(_dt);
            detectMovementEnd();
            filterPosition(_dt);
            calculateDistance();
        }
    }

    /**
     * @brief Provides the caller with the current m_distance.
     *
     * @return The overall measured m_distance in meters.
     */
    public float getDistance() {
        return m_distance;
    }

    /**
     * @brief Serializes the current sensor state to a string.
     *
     * Each value has to be seperated using pipes in order
     * to be parsed back by the GUI.
     *
     * @return A string containing a representation of the current sensor state.
     */
    public String currentStateToString() {
        String temp = "";
        
        temp += m_accelerationX[1] + "|";
        temp += m_accelerationY[1] + "|";
        temp += m_velocityX[1] + "|";
        temp += m_velocityY[1] + "|";
        temp += (m_positionX[1] - m_positionX[0]) + "|";
        temp += (m_positionY[1] - m_positionY[0]) + "|";
        temp += m_distance + "|";
        temp += m_frameLength + "|";
        temp += m_kalmanFilter.currentStateToString();

        return temp;
    }

    /**
     * @brief Updates the m_distance using position data.
     *
     * This method computes the square root sum of position differences
     * between two update cycles to get the overall position change between
     * two frames.
     */
    private void calculateDistance() {
       
        float posXdt = m_positionX[1] - m_positionX[0];
        float posYdt = m_positionY[1] - m_positionY[0];

        m_distance += Math.sqrt(posXdt * posXdt + posYdt * posYdt);
        //System.out.println("Distance: " + m_distance);

        // store current positions as previous values for the next integral step
        m_positionX[0] = m_positionX[1];
        m_positionY[0] = m_positionY[1];
    }

    /**
     * @brief Integrates the filtered acceleration values to calculate a position.
     *
     * Explicit and implicit methods are approaches used in numerical analysis
     * for obtaining numerical solutions of timedependent ordinary and partial
     * differential equations, as is required in computer simulations of physical
     * processes. The explicit or forward Euler method calculates a state of
     * a system which occurs later than the current state (t > t0). For this purpose
     * time is seen as a descrete function that has values in fixed intervals of
     * the same size. This fixed step size is declared as tk = t0 + k · h
     * while h > 0. However, the Forward Euler Method does not work accurately
     * for stiff equations that have a larger h. In this case the Implicit Euler
     * Method could be used.
     *
     * @param _dt The difference between the current and the previous frame in seconds.
     */
    private void integrate(float _dt) {

        // calculate new velocity
        m_velocityX[1] = m_velocityX[0] + m_accelerationX[1] * _dt;
        m_velocityY[1] = m_velocityY[0] + m_accelerationY[1] * _dt;

        // calculate new position
        m_positionX[1] = m_positionX[0] + m_velocityX[1] * _dt;
        m_positionY[1] = m_positionY[0] + m_velocityY[1] * _dt;

        // store current values as previous values for next integral step
        m_accelerationX[0] = m_accelerationX[1];
        m_accelerationY[0] = m_accelerationY[1];
        m_velocityX[0] = m_velocityX[1];
        m_velocityY[0] = m_velocityY[1];
    }

    /**
     * @brief Filters the position using a lowpass filter.
     *
     * This method updates the lowpass filter and corrects the calculated positions.
     *
     * @param _dtx The time passed in seconds since the last update.
     */
    private void filterPosition(float _dtx) {
        m_lowpassFilter.update(new float[]{m_positionX[1], m_positionY[1]}, _dtx);
 
        float[] tempPos = m_lowpassFilter.getCorrectedValues();

        m_positionX[1] = tempPos[0];
        m_positionY[1] = tempPos[1];
    }

    /**
     * @brief Filters the measured accelerations.
     *
     * This method uses a two dimensional Kalman filter to correct measured
     * sensor values. The sensor values are first corrected by applying the
     * zero acceleration offset. Additionally, a discrimination window is
     * applied which helps to prevent error accumulation due to the earth's
     * gravity.
     *
     * @param _dtx The time passed in seconds since the last update.
     */
    private void filterAcceleration(float _dtx) {

        m_kalmanFilter.update(new float[]{m_xAccelSample - m_offsetX, m_yAccelSample - m_offsetY}, _dtx);

        float[] temp = m_kalmanFilter.getCorrectedValues();

        m_accelerationX[1] = temp[0];
        m_accelerationY[1] = temp[1];

        /*
         * Create discrimination window for very small accelerations which would
         * be very slow movements. Additionally, assume that this sensor is used
         * by human people who are moving around on their own feet.
         */
        if ((m_accelerationX[1] <= DISCRIMINATION_SIZE) && (m_accelerationX[1] >= -DISCRIMINATION_SIZE)) {
            m_accelerationX[1] = 0;
        }

        if ((m_accelerationY[1] <= DISCRIMINATION_SIZE) && (m_accelerationY[1] >= -DISCRIMINATION_SIZE)) {
            m_accelerationY[1] = 0;
        }
    }

    /**
     * @brief Detects movement ends and sets the velocity to zero.
     */
    private void detectMovementEnd() {
        if (m_accelerationX[1] == 0) {
            m_cntX++;
        } else {
            m_cntX = 0;
        }

        // 25 is an estimated threshold which can be adopted
        if (m_cntX >= SLOWDOWN_THRESHOLD) {
            m_velocityX[0] = 0;
            m_velocityX[1] = 0;       
        }

        if (m_accelerationY[1] == 0) {
            m_cntY++;
        } else {
            m_cntY = 0;
        }

        // 25 is an estimated threshold which can be adopted
        if (m_cntY >= SLOWDOWN_THRESHOLD) {
            m_velocityY[0] = 0;
            m_velocityY[1] = 0;
        }
    }

    /**
     * @brief Gets the sensor values from the sensor.
     *
     * Sensor values are provided in G. 1 G ~ 9.81m/s². These sensor values
     * are converted in m/s². However, it would also be possible to describe
     * the earth's acceleration in newton per kilogram. The numeric value stays
     * the same. This alternative representation can be understood by noting that
     * the gravitational force acting on an object at the Earth's surface is
     * proportional to the mass of the object: for each kilogram of mass, the
     * Earth exerts a nominal force of ~ 9.81 newtons. Though, the precise value
     * varies depending on the location of measurement.
     *
     * @throws IOException
     */
    private void getSensorValues() throws IOException {
        m_xAccelSample = (float) m_accel.getAccelX() * EARTH_ACCELERATION;
        m_yAccelSample = (float) m_accel.getAccelY() * EARTH_ACCELERATION;
        m_zAccelSample = (float) m_accel.getAccelZ() * EARTH_ACCELERATION;
    }
}
