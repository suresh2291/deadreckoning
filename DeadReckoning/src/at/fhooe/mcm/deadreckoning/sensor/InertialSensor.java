package at.fhooe.mcm.deadreckoning.sensor;

import at.fhooe.mcm.deadreckoning.filter.IFilter;
import at.fhooe.mcm.deadreckoning.filter.KalmanFilter;
import at.fhooe.mcm.deadreckoning.filter.LowpassFilter;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import java.io.IOException;

/**
 * @class KalmanFilter
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @date 12.05.2010
 * @version 2.0
 *
 * @brief This class provides a deadreckoning algorithm based on inertial navigation techniques.
 *
 * This algorithm uses accelerometers to not calculate exact positions but to provide accurate
 * m_distance information. This algorithm only uses two axis of a accelerometer and does not depend
 * on gyroscopes. For accuracy multiple filtering strategies are used.
 */
public class InertialSensor {
    
    private static final float DISCRIMINATION_SIZE = 2.0f;
    private static final float SLOWDOWN_THRESHOLD = 12.0f;

    
    /** @brief The interface to access the Sun Spot's accelerometer. */
    private IAccelerometer3D m_accel;
    /** @brief Storage for the current and the previous x-acceleration. */
    float[] m_accelerationX;
    /** @brief Storage for the current and the previous y-acceleration. */
    float[] m_accelerationY;
    /** @brief Storage for the current and the previous x-velocity. */
    float[] m_velocityX;
    /** @brief Storage for the current and the previous y-velocity. */
    float[] m_velocityY;
    /** @brief Storage for the current and the previous x-position. */
    float[] m_positionX;
    /** @brief Storage for the current and the previous y-position. */
    float[] m_positionY;
    /** @brief Filter interface for a Kalman filter. */
    IFilter m_kalmanFilter;
    /** @brief Filter interface for a Kalman based lowpass filter. */
    IFilter m_lowpassFilter;
    /** @brief Last measured raw x-acceleration. */
    float m_xAccelSample;
    /** @brief Last measured raw y-acceleration. */
    float m_yAccelSample;
    /** @brief Last measured raw y-acceleration. */
    float m_zAccelSample;
    /** @brief Counter to detect movement stops on the x-axis. */
    float m_cntX;
    /** @brief Counter to detect movement stops on the y-axis. */
    float m_cntY;
    /** @brief The overall m_distance in meters. */
    float m_distance;
    /** @brief The sensor offset for the x-axis. */
    float m_offsetX;
    /** @brief The sensor offset for the y-axis. */
    float m_offsetY;
    /** @brief The sensor offset for the y-axis. */
    float m_offsetZ;
    /** @brief A flag to indicate if the the sensor is calibrated. */
    boolean m_isCalibrated;

     float[] m_posXdt;
     float[] m_posYdt;

    /**
     * @brief Creates a new instance of an <code>InertialSensor</code>
     */
    public InertialSensor() {
        m_accelerationX = new float[2];
        m_accelerationY = new float[2];
        m_velocityX = new float[2];
        m_velocityY = new float[2];
        m_positionX = new float[2];
        m_positionY = new float[2];
        m_posXdt = new float[2];
        m_posYdt = new float[2];

        m_xAccelSample = 0;
        m_yAccelSample = 0;
        m_zAccelSample = 0;
        m_cntX = 0;
        m_cntY = 0;

        m_distance = 0;

        m_offsetX = 0;
        m_offsetY = 0;
        m_offsetZ = 0;

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
    public void calibrate() throws IOException {

        for (int i = 0; i < 1024; i++) {
            getSensorValues();
            m_offsetX = m_offsetX + m_xAccelSample;
            m_offsetY = m_offsetY + m_yAccelSample;
        }

        m_offsetY /= 1024;
        m_offsetZ /= 1024;

        m_offsetZ = m_zAccelSample - 9.81f;

        m_isCalibrated = true;
    }

    /**
     * @brief Initializes the sensor.
     *
     * This method initializes the used filters and sets the filters dimensions. For
     * accurate initial values the sensor has to be calibrated first.
     *
     * @throws IOException
     */
    public void init() throws IOException {
        if (!m_isCalibrated) {
            calibrate();
        }

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
        getSensorValues();
        filterAcceleration();
        integrate(_dt);
        detectMovementEnd();
        filterPosition();
        calculateDistance();
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
     * @brief Updates the m_distance using position data.
     *
     * This method computes the square root sum of position differences
     * between two update cycles to get the overall position change between
     * two frames.
     */
    private void calculateDistance() {
        m_posXdt[1] = m_positionX[1] - m_positionX[0];
        m_posYdt[1] = m_positionY[1] - m_positionY[0];

        if(m_posXdt[1] > 1.5)
            m_posXdt[1] = (float)(m_posXdt[1] - ((int)m_posXdt[1]) + 1.0f);

        if(m_posYdt[1] > 1.5)
            m_posYdt[1] = (float)(m_posYdt[1] - ((int)m_posYdt[1]) + 1.0f);

        float posXdt = (m_posXdt[1] + m_posXdt[0]) / 2.0f;
        float posYdt = (m_posYdt[1] + m_posYdt[0]) / 2.0f;

        m_posXdt[0] = m_posXdt[1];
        m_posYdt[0] = m_posYdt[1];


        m_distance += Math.sqrt(posXdt * posXdt + posYdt * posYdt);
        System.out.println("Distance: " + m_distance);

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

        float gravityOffset = m_zAccelSample / 9.81f;

        // correct earth gravity
        if(gravityOffset < 1f) {
           if(m_accelerationX[1] > m_accelerationY[1])
               m_accelerationX[1] *= (1f - gravityOffset);
            else
                m_accelerationY[1] *= (1f - gravityOffset);
        }

        // calculate new velocity
        m_velocityX[1] = m_velocityX[0] + ((m_accelerationX[0] + m_accelerationX[1]) / 2f) * _dt;
        m_velocityY[1] = m_velocityY[0] + ((m_accelerationX[0] + m_accelerationY[1]) / 2f) * _dt;

        // calculate new position
        m_positionX[1] = m_positionX[0] + ((m_velocityX[0] + m_velocityX[1]) / 2f) * _dt;
        m_positionY[1] = m_positionY[0] + ((m_velocityY[0] + m_velocityY[1]) / 2f) * _dt;

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
     */
    private void filterPosition() {
        m_lowpassFilter.update(new float[]{m_positionX[1], m_positionY[1]});

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
     */
    private void filterAcceleration() {
        m_kalmanFilter.update(new float[]{m_xAccelSample - m_offsetX, m_yAccelSample - m_offsetY});

        float[] temp = m_kalmanFilter.getCorrectedValues();

        m_accelerationX[1] = temp[0];
        m_accelerationY[1] = temp[1];

        /*
         * Create discrimination window for very small accelerations which would
         * be very slow movements. Additionally, assume that this sensor is used
         * by human people who are moving around on their own feet.
         */
        if (((m_accelerationX[1] <= DISCRIMINATION_SIZE) && (m_accelerationX[1] >= -DISCRIMINATION_SIZE))
                || ((m_accelerationX[1] >= 6) || (m_accelerationX[1] <= -6))) {
            m_accelerationX[1] = 0;
        }

        if (((m_accelerationY[1] <= DISCRIMINATION_SIZE) && (m_accelerationY[1] >= -DISCRIMINATION_SIZE))
                || ((m_accelerationY[1] >= 6) || (m_accelerationY[1] <= -6))) {
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
     * Sensor values are provided in G. 1 G ~ 9.81m/s
     *
     * @throws IOException
     */
    private void getSensorValues() throws IOException {
        m_xAccelSample = (float) m_accel.getAccelX() * 9.81f;
        m_yAccelSample = (float) m_accel.getAccelY() * 9.81f;
        m_zAccelSample = (float) m_accel.getAccelZ() * 9.81f;
    }
}
