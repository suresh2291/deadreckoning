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
 * distance information. This algorithm only uses two axis of a accelerometer and does not depend
 * on gyroscopes. For accuracy multiple filtering strategies are used.
 */
public class InertialSensor {
    /** @brief The interface to access the Sun Spot's accelerometer. */
    private IAccelerometer3D accel;

    /** @brief Storage for the current and the previous x-acceleration. */
    float[] accelerationX;

    /** @brief Storage for the current and the previous y-acceleration. */
    float[] accelerationY;
    
     /** @brief Storage for the current and the previous x-velocity. */
    float[] velocityX;

     /** @brief Storage for the current and the previous y-velocity. */
    float[] velocityY;

     /** @brief Storage for the current and the previous x-position. */
    float[] positionX;

     /** @brief Storage for the current and the previous y-position. */
    float[] positionY;

    /** @brief Filter interface for a Kalman filter. */
    IFilter kalmanFilter;

    /** @brief Filter interface for a Kalman based lowpass filter. */
    IFilter lowpassFilter;

    /** @brief Last measured raw x-acceleration. */
    float xAccelSample;

    /** @brief Last measured raw y-acceleration. */
    float yAccelSample;

    /** @brief Counter to detect movement stops on the x-axis. */
    float cntX;

    /** @brief Counter to detect movement stops on the y-axis. */
    float cntY;

    /** @brief The overall distance in meters. */
    float distance;

    /** @brief The sensor offset for the x-axis. */
    float offsetX;

    /** @brief The sensor offset for the y-axis. */
    float offsetY;

    /** @brief A flag to indicate if the the sensor is calibrated. */
    boolean isCalibrated;

    /**
     * @brief Creates a new instance of an <code>InertialSensor</code>
     */
    public InertialSensor() {
        accelerationX = new float[2];
        accelerationY = new float[2];
        velocityX = new float[2];
        velocityY = new float[2];
        positionX = new float[2];
        positionY = new float[2];

        xAccelSample = 0;
        yAccelSample = 0;

        cntX  = 0;
        cntY  = 0;

        distance = 0;

        offsetX = 0;
        offsetY = 0;

        isCalibrated = false;

        accel  = EDemoBoard.getInstance().getAccelerometer();
        kalmanFilter = new KalmanFilter();
        lowpassFilter = new LowpassFilter();
    }

    /**
     * @brief Calibrates the sensor.
     *
     * This method takes 1024 samples of sensor values and provides a filtered
     * value by averaging the taken samples.
     *
     * @throws IOException
     */
    public void calibrate() throws IOException{

        for(int i = 0; i < 1024; i++) {
            getSensorValues();
              offsetX = offsetX + xAccelSample;
              offsetY = offsetY + yAccelSample;
        }

        offsetX /= 1024;
        offsetY /= 1024;

        isCalibrated = true;
    }

    /**
     * @brief Initializes the sensor.
     *
     * This method initializes the used filters and sets the filters dimensions. For
     * accurate initial values the sensor has to be calibrated first.
     *
     * @throws IOException
     */
    public void init() throws IOException{
        if(!isCalibrated)
            calibrate();
        
        kalmanFilter.init(new float[] {xAccelSample - offsetX, yAccelSample - offsetY}, 2);
        lowpassFilter.init(new float[] {positionX[1], positionY[1]}, 2);
    }

    /**
     * @brief Updates the deadreckoning system.
     *
     * This method updates current distance by filtering newly observed values,
     * looks for movement ends and re-calculates the distance.
     *
     * @param dt The time between the previous and the current update cylce in seconds.
     * @throws IOException
     */
    public void update(float dt) throws IOException{
        getSensorValues();
        filterAcceleration();
        integrate(dt);
        detectMovementEnd();
        filterPosition();
        calculateDistance();
    }

    /**
     * @brief Provides the caller with the current distance.
     *
     * @return The overall measured distance in meters.
     */
    public float getDistance() {
        return distance;
    }

    /**
     * @brief Updates the distance using position data.
     *
     * This method computes the square root sum of position differences
     * between two update cycles to get the overall position change between
     * two frames.
     */
    private void calculateDistance() {
        float posXdt = (positionX[1] - positionX[0]);
        float posYdt = (positionY[1] - positionY[0]);

        distance += Math.sqrt(posXdt * posXdt + posYdt * posYdt);
        System.out.println("Distance: " + distance);

        // store current positions as previous values for the next integral step
        positionX[0] = positionX[1];
        positionY[0] = positionY[1];
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
     * @param dtx The difference between the current and the previous frame in seconds.
     */
    private void integrate(float dtx) {
        // calculate new velocity
        velocityX[1] = velocityX[0] + (accelerationX[1]) * dtx;
        velocityY[1] = velocityY[0] + (accelerationY[1]) * dtx;

        // calculate new position
        positionX[1] = positionX[0] + (velocityX[1]) * dtx;
        positionY[1] = positionY[0] + (velocityY[1]) * dtx;

        // store current values as previous values for next integral step
        accelerationX[0] = accelerationX[1];
        accelerationY[0] = accelerationY[1];
        velocityX[0] = velocityX[1];
        velocityY[0] = velocityY[1];
    }

    /**
     * @brief Filters the position using a lowpass filter.
     *
     * This method updates the lowpass filter and corrects the calculated positions.
     */
    private void filterPosition() {
        lowpassFilter.update(new float[] { positionX[1],  positionY[1] });

        float[] tempPos = lowpassFilter.getCorrectedValues();

        positionX[1] = tempPos[0];
        positionY[1] = tempPos[1];
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
         kalmanFilter.update(new float[] { xAccelSample-offsetX, yAccelSample-offsetY});

        float[] temp = kalmanFilter.getCorrectedValues();

        accelerationX[1] = temp[0];
        accelerationY[1] = temp[1];

        /*
         * Create discrimination window for very small accelerations which would
         * be very slow movements. Additionally, assume that this sensor is used
         * by human people who are moving around on their own feet. The averaged
         * maximum speed of a human beeing while running over a longer period of
         * time is about 20 km/h ~ 5 m/s.
         */
        if (((accelerationX[1] <=3) && (accelerationX[1] >= -3)) ||
                ((accelerationX[1] >=6) || (accelerationX[1] <= -6)))
            accelerationX[1] = 0;

        if (((accelerationY[1] <=3) && (accelerationY[1] >= -3)) ||
                ((accelerationX[1] >=6) || (accelerationX[1] <= -6)))
            accelerationY[1] = 0;
    }

    /**
     * @brief Detects movement ends and sets the velocity to zero.
     */
    private void detectMovementEnd() {
        if(accelerationX[1] == 0)
            cntX++;
        else
            cntX = 0;

        // 25 is an estimated threshold which can be adopted
        if(cntX >= 5) {
            velocityX[0] = 0;
            velocityX[1] = 0;
        }

        if(accelerationY[1] == 0)
            cntY++;
        else
            cntY = 0;

        // 25 is an estimated threshold which can be adopted
        if(cntY >= 5) {
            velocityY[0] = 0;
            velocityY[1] = 0;
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
        xAccelSample = (float)accel.getAccelX() * 9.81f;
        yAccelSample = (float)accel.getAccelY() * 9.81f;
    }

}
