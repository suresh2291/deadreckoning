package at.fhooe.mcm.deadreckoning.application;

/**
 * @class Timer
 * @author Florian Lettner, Lukas Bischof, Peter Riedl
 * @date 24.05.2010
 * @version 1.0
 *
 * @brief This class represents a modified high precision timer.
 *
 * High precision timers are used in the gaming domain for most accurate updates.
 * This is a modified version that works with milliseconds instead of nanoseconds.
 */
public class Timer {

    /** @brief Maximum delta time in nanoseconds. */
    private static final long MAX_DELTATIME = 250;

    /** @brief Represents the duration of one milliseconds in seconds. */
    private static final double ONE_MILLISECOND = 0.001;

    /** @brief Represents the duration of one second in milliseconds. */
    private static final long ONE_SECOND = 1000;

    /** @brief Number of ticks used to calculate average delta time. */
    private static final int MAX_CNT_TICKS = 30;

    /** @brief Current time. */
    private long time;

    /** @brief Current delta time. */
    private long dt;

    /** @brief Last time time stamp. */
    private long last;

    /** @brief Counter used to calculate average updates per second. */
    private int cntTicks;

    /** @brief Last time stamp used to calculate average updates per second. */
    private long lastAvg;

    /** @brief Average delta time. */
    private long dtAvg;

    /**
     * @brief Creates a new instance of <code>Timer</clock>.
     *
     * <code>reset</code> is called during initialization.
     */
    public Timer() {
        reset();
    }

    /**
     * @brief Resets all values to zero.
     */
    public void reset() {
        this.time = 0;
        this.dt = 0;
        this.last = System.currentTimeMillis();

        this.dtAvg = 0;
        this.lastAvg = this.last;
        this.cntTicks = MAX_CNT_TICKS;
    }

    /**
     * @brief Calculates new values for time and delta time.
     *
     * The new values are calculated by measuring the current
     * time using Java's internal timer timer.
     */
    public void tick() {
        // calculate new delta time and absolute time
        long now = System.currentTimeMillis();
        dt = (now - last);
        last = now;

        if (dt > MAX_DELTATIME) {
            dt = MAX_DELTATIME;
        }

        time += dt;

        // update average delta time used to calculate average updates per second
        this.cntTicks--;

        if (this.cntTicks <= 0) {
            this.dtAvg = (now - this.lastAvg) / MAX_CNT_TICKS;
            this.lastAvg = now;
            this.cntTicks = MAX_CNT_TICKS;
        }
    }

    /**
     * @brief Returns the current delta time in seconds.
     *
     * @return Current delta time in seconds.
     */
    public double getDelta() {
        return dt * ONE_MILLISECOND;
    }

    /**
     * @brief Returns the current absolute time in seconds.
     *
     * @return Current absolute time in seconds.
     */
    public double getTime() {
        return time * ONE_MILLISECOND;
    }

    /**
     * @brief Returns the average updates in seconds (UPS).
     *
     * @return Average updates in seconds.
     */
    public double getUps() {
        return ONE_SECOND / this.dtAvg;
    }

    /**
     * @brief Indicates if an new value for average updates in seconds is available.
     *
     * @return <code>true</code> if a new value for average updates in
     * seconds is available, <code>false</code> otherwise.
     */
    public boolean isNewUps() {
        return this.cntTicks == MAX_CNT_TICKS;
    }
}
