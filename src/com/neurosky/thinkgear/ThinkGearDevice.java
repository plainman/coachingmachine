/**
 * This code was written by
 * David Cheatham
 * dcheath@projectportfolio.info
 * http://dcheath.projectportfolio.info/
 * 
 * Any person is free to use this code under the terms of Creative Commons
 * Attribution-ShareAlike (CC BY-SA):
 * http://creativecommons.org/licenses/by-sa/3.0/
 */
package com.neurosky.thinkgear;


/**
 * This class wraps the functions of the ThinkGear API encapsulating the
 * creation and freeing of the API resources. When the user calls connect, a
 * readPacket thread is automatically spawned which will check for packets once
 * every 1ms. A {@link #waitForPacket() } method is provided that will allow a
 * thread to wait until a valid packet has been read.
 * @author David Cheatham <dcheath@projectportfolio.info>
 * @version 0.1 
 * @todo Split into several classes that break up the tasks.
 *      This includes scanning for devices, tracking already created devices and
 *      device count, providing shared and exclusive access, disposing of
 *      connections, storing set configuration and notifying clients of updates.
 * @todo Replace finalize with weak references.
 * @todo Add checking (one valid packet) to the connect function.
 * @todo Add port sweeping for connect.
 * @todo Add baud sweeping.
 * @todo Add MindSet counting (for multiple headsets).
 * @todo Add notify on valid packet.
 */
public class ThinkGearDevice {
    ////////////////////Class Level Members////////////////////

    /**
     * The number of connections currently open.
     */
    private static int connections = 0;
    /**
     * The maximum number of connections supported by the API.
     */
    public static final int maxConnections = ThinkGear.MAX_CONNECTION_HANDLES;

    /**
     * This method returns the number of connections that currently exist.
     * @return the number of connections that currently exist
     */
    public static int connectionCount() {
        return connections;
    }

    /**
     * An enumeration containing the BUAD_ constants specified in the API.
     */
    private enum Baudrate {

        BAUD_1200(ThinkGear.BAUD_1200),
        BAUD_2400(ThinkGear.BAUD_2400),
        BAUD_4800(ThinkGear.BAUD_4800),
        BAUD_9600(ThinkGear.BAUD_9600),
        BAUD_57600(ThinkGear.BAUD_57600),
        BAUD_115200(ThinkGear.BAUD_115200);
        /**
         * The Baudrate represented by this member of the enumeration.
         */
        private int rate;

        /**
         * The sole constructor requires you to provide a int Baudrate when
         * specifying a new member of the enumeration.
         * @param rate the int Baudrate for this member of the enumeration
         */
        Baudrate(int rate) {
            this.rate = rate;
        }

        /**
         * Returns the int Baudrate for this member of the enumeration.
         * @return the int Baudrate for this member of the enumeration
         */
        private int getRate() {
            return rate;
        }
    }

    /**
     * An enumeration containing the Data_ constants specified in the ThinkGear
     * API.
     */
    public enum DataType {

        Battery(ThinkGear.DATA_BATTERY),
        PoorSignal(ThinkGear.DATA_POOR_SIGNAL),
        Attention(ThinkGear.DATA_ATTENTION),
        Meditation(ThinkGear.DATA_MEDITATION),
        Raw(ThinkGear.DATA_RAW),
        Delta(ThinkGear.DATA_DELTA),
        Theta(ThinkGear.DATA_THETA),
        lowAlpha(ThinkGear.DATA_ALPHA1),
        highAlpha(ThinkGear.DATA_ALPHA2),
        lowBeta(ThinkGear.DATA_BETA1),
        highBeta(ThinkGear.DATA_BETA2),
        lowGamma(ThinkGear.DATA_GAMMA1),
        highGamma(ThinkGear.DATA_GAMMA2),
        BlinkStrength(ThinkGear.TG_DATA_BLINK_STRENGTH);
        /**
         * The code used to access the DataType represented by this member of
         * the enumeration
         */
        private int code;

        /**
         * The sole constructor requires an int code to be used to access the
         * DataType represented by this member of the enumeration.
         * @param code code to be used to access the DataType represented by
         *              this member of the enumeration
         */
        DataType(int code) {
            this.code = code;
        }

        /**
         * Returns the code used to access the DataType represented by this
         * member of the enumeration.
         * @return the code used to access the DataType represented by this
         *          member of the enumeration
         */
        private int getCode() {
            return code;
        }
    }
    ////////////////////Instance Level Members////////////////////
    /**
     * The connectionId to be used in communications with the ThinkGear API
     * about this instance.
     */
    private int connectionID = -1;
    /**
     * The {@link ReadLoop} task associated with thir readThread.
     */
    private ReadLoop readLoop = null;
    /**
     * The Thread executing this instances {@link ReadLoop}.
     */
    private Thread readThread = null;
    /**
     * A flag indicating whether this instance is currently connected to a
     * ThinkGear device.
     */
    private boolean connected = false;
    /**
     * A flag indicating whether software blink detection has been enabled in
     * this instance.
     */
    private boolean blinkDetection = false;
    /**
     * A flag indicating whether software low pass filtering has been enabled in
     * this instance.
     */
    private boolean lowPassFilter = false;

    /**
     * The sole constructor accepts no parameters. A connectionId is requested
     * from the ThinkGear API, but no connection to a ThinkGear device is made.
     */
    public ThinkGearDevice() {
        connectionID = ThinkGear.GetNewConnectionId();
        connections++;
    }

    /**
     * Attempts to connect to the COM port number provided. If it is successful
     * the method then enables software blink detection and starts the read
     * loop.
     * @param comPort the number of the COM port to connect to
     * @return true if the connection succeeds; otherwise false
     */
    public boolean connect(byte comPort) {
        if (comPort < 1) {
            throw new IndexOutOfBoundsException();
        }
        if (ThinkGear.Connect(connectionID, "\\\\.\\COM" + comPort,
                Baudrate.BAUD_9600.getRate(), ThinkGear.STREAM_PACKETS) == 0) {
            connected = true;
            setBlinkDetection(true);
            startReadLoop();
            return true;
        } else {
            return false;
        }
    }

    /**
     * If there is no {@link ReadLoop}, this method creates a ReadLoop task in a
     * new {@link Thread} and starts the Thread
     */
    private void startReadLoop() {
        if (readLoop == null) {
            readLoop = new ReadLoop();
            readThread = new Thread(readLoop);
            readThread.start();
        }
    }

    /**
     * If the {@link ReadLoop} is running, this method will wait until it reads a valid
     * packet and pass return true. If the ReadLoop does not exist, or is
     * terminating, it will return false.
     * @return true when a packet has been read; false if ReadLoop terminating
     *          or not running
     */
    public boolean waitForPacket() {
        if (readLoop == null) {
            return false;
        } else {
            return readLoop.waitForPacket();
        }
    }

    /**
     * If a {@link ReadLoop} exists, this method requests that it terminate
     * (notifying any waiting {@link Thread}s and removes all references to the
     * ReadLoop and it's Thread.
     */
    private void stopReadLoop() {
        if (readLoop != null) {
            readLoop.requestStop();
            readLoop = null;
            readThread = null;
        }
    }

    /**
     * Sets whether software blink detection should be enabled for this
     * instance.
     * @param enabled true if software blink detection should be enabled;
     *                  otherwise false
     */
    public void setBlinkDetection(boolean enabled) {
        int setting = 0;
        if (enabled) {
            setting = 1;
        }
        ThinkGear.EnableBlinkDetection(connectionID, setting);
        blinkDetection = enabled;
    }

    /**
     * Returns a boolean indicating whether software blink detection is enabled.
     * @return a boolean indicating whether software blink detection is enabled
     */
    public boolean getBlinkDetection() {
        return blinkDetection;
    }

    /**
     * Sets whether the software low pass filter should be enabled for this
     * instance.
     * @param enabled true if the software low pass filter should be enabled;
     *                  otherwise false
     */
    public void setLowPassFilter(boolean enabled) {
        int rate = 0;
        if (enabled) {
            rate = 512;
        }
        ThinkGear.EnableLowPassFilter(connectionID, rate);
        lowPassFilter = enabled;
    }

    /**
     * Returns a boolean indicating whether the software low pass filter is
     * enabled.
     * @return a boolean indicating whether the software software low pass
     *          filter is enabled
     */
    public boolean getLowPassFilter() {
        return lowPassFilter;
    }

    /**
     * Returns the last value of {@code type} received on this connection.
     * @param type the DataType to return
     * @return the last value received of {@code type}
     */
    public double getValue(DataType type) {
        return ThinkGear.GetValue(connectionID, type.getCode());
    }

    /**
     * Returns true if the last packet read updated the value for {@code type}.
     * @param type the DataType whose status should be returned
     * @return true if the last packet updated the value of this {@code type};
     *          otherwise false
     */
    public boolean getValueStatus(DataType type) {
        return ThinkGear.GetValueStatus(connectionID, type.getCode())
                != 0;
    }

    /**
     * Stops the {@link ReadLoop} and disconnects from any ThinkGear device
     * connected to this instance.
     */
    public void disconnect() {
        connected = false;
        stopReadLoop();
        ThinkGear.Disconnect(connectionID);
    }

    /**
     * Calls {@link #disconnect() } and frees the resources associated with this
     * isntance.
     */
    public void dispose() {
        if (connectionID > 0) {
            disconnect();
            ThinkGear.FreeConnection(connectionID);
            connectionID = -1;
            connections--;
        }
    }

    /**
     * Ensures that dispose has been called on this instance.
     * @throws Throwable if generated by the finalize method of the super class
     * @todo Replace finalize with weak references
     */
    @Override
    public void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    /**
     * When run, this class attempts to read a packet once a millisecond. If any
     * valid packets are read, it notifies any {@link Thread}s who have called
     * {@link #waitForPacket() }, otherwise it sleeps for a millisecond.
     */
    private class ReadLoop implements Runnable {

        /**
         * An object used to block any {@link Thread}s waiting for a valid
         * packet to be read.
         */
        private PacketFlag pacFlag = new PacketFlag();
        /**
         * Indicates whether or not the loop should continue running.
         */
        private boolean run = true;

        /**
         * Stops the loop on it's next pass.
         */
        public void requestStop() {
            run = false;
        }

        /**
         * This method will block until a valid packet is read, or the ReadLoop
         * terminates. If the ReadLoop is terminating it will return false,
         * otherwise it returns true.
         * @return false if the ReadLoop is terminating; otherwise true
         */
        public boolean waitForPacket() {
            pacFlag.waitForPacket();
            return run;
        }

        /**
         * While a stop has not been requested, this loop attempts to read a 
         * ThinkGear packet then sleeps for 1ms. If a valid packet is read, or
         * the loop terminates, all {@link Thread}s waiting for packets are
         * notified.
         */
        public void run() {
            while (run) {
                if (ThinkGear.ReadPackets(connectionID, 1) > 0) {
                    pacFlag.packetUpdate();
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                } //No-op
            }

            pacFlag.packetUpdate();
        }

        /**
         * This class is used to create a flag which allows {@link Thread}s to
         * wait for packet updates from the {@link ReadLoop}.
         */
        private class PacketFlag {

            /**
             * This method blocks until this object is notified of a packet
             * update.
             */
            public synchronized void waitForPacket() {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            /**
             * Notifies all {@link Thread}s waiting on this object.
             */
            private synchronized void packetUpdate() {
                notifyAll();
            }
        }
    }
}
