/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Cloudlet represents a application/job/task to be executed by a {@link Vm}
 * on behalf of a given user. It stores, despite all the
 * information encapsulated in the Cloudlet, the ID of the VM running it.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 * @see DatacenterBroker
 */
public class Cloudlet {    
  /**
   * Value to indicate that a given cloudlet resource was not assigned yet,
   * such as a user or a VM to run the cloudlet.
   * @see #reservationId
   */  
  public static final int NOT_ASSIGNED = -1;

  public enum Status {
        /**
         * The Cloudlet has been created and added to the CloudletList object.
         */
        CREATED, 
        /**
         * The Cloudlet has been assigned to a CloudResource object to be executed
         * as planned.
         */
        READY, 
        /**
         * The Cloudlet has moved to a Cloud node.
         */
        QUEUED, 
        /**
         * The Cloudlet is in execution in a Cloud node.
         */
        INEXEC, 
        /**
         * The Cloudlet has been executed successfully.
         */
        SUCCESS, 
        /**
         * The Cloudlet has failed.
         */
        FAILED, 
        /**
         * The Cloudlet has been canceled.
         */
        CANCELED, 
        /**
         * The Cloudlet has been paused. It can be resumed by changing the status
         * into <tt>RESUMED</tt>.
         */
        PAUSED, 
        /**
         * The Cloudlet has been resumed from <tt>PAUSED</tt> state.
         */
        RESUMED,
        /**
         * The cloudlet has failed due to a resource failure.
         */
        FAILED_RESOURCE_UNAVAILABLE
    }    

    /**
     * The cloudlet ID.
     */
    private final int cloudletId;

    /**
     * The User or Broker ID that is the owner of the Cloudlet. 
     * It is advisable that broker set this ID with its
     * own ID, so that CloudResource returns to it after the execution.
	 *
     */
    private int userId;

    /**
     * The execution length of this Cloudlet (Unit: in Million Instructions
     * (MI)). According to this length and the power of the processor (in
     * Million Instruction Per Second - MIPS) where the cloudlet will be run,
     * the cloudlet will take a given time to finish processing. For instance,
     * for a cloudlet of 10000 MI running on a processor of 2000 MIPS, the
     * cloudlet will spend 5 seconds using the processor in order to be
     * completed (that may be uninterrupted or not, depending on the scheduling
     * policy).
     *
     * @see #setNumberOfPes(int)
     *
     */
    private long cloudletLength;

    /**
     * The input file size of this Cloudlet before execution (unit: in byte).
     * This size has to be considered the program + input data sizes.
     */
    private final long cloudletFileSize;

    /**
     * The output file size of this Cloudlet after execution (unit: in byte).
     *
     * @todo See
     * <a href="https://groups.google.com/forum/#!topic/cloudsim/MyZ7OnrXuuI">this
     * discussion</a>
     */
    private final long cloudletOutputSize;

    /**
     * The number of Processing Elements (PEs) required to execute this cloudlet
     * (job).
     *
     * @see #setNumberOfPes(int)
     */
    private int numberOfPes;

    /**
     * The execution status of this Cloudlet.
     */
    private Status status;

    /**
     * The execution start time of this Cloudlet. With new functionalities, such
     * as CANCEL, PAUSED and RESUMED, this attribute only stores the latest
     * execution time. Previous execution time are ignored.
     */
    private double execStartTime;

    /**
     * The time where this Cloudlet completes.
     */
    private double finishTime;

    /**
     * The ID of a reservation made for this cloudlet.
     *
     * @todo This attribute doesn't appear to be used
     */
    private int reservationId = NOT_ASSIGNED;

    /**
     * Indicates if transaction history records for this Cloudlet is to be
     * outputted.
     */
    private final boolean record;

    /**
     * Stores the operating system line separator.
     */
    private String newline;

    /**
     * The cloudlet transaction history.
     */
    private StringBuffer history;

    /**
     * The list of every {@link Datacenter} where the cloudlet has been executed. In case
     * it starts and finishes executing in a single datacenter, without
     * being migrated, this list will have only one item.
     */
    private final List<Resource> resList;

    /**
     * The index of the last resource where the cloudlet was executed. If the
     * cloudlet is migrated during its execution, this index is updated. The
     * value {@link #NOT_ASSIGNED} indicates the cloudlet has not been executed yet.
     */
    private int index;

    /**
     * The classType or priority of this Cloudlet for scheduling on a resource.
     */
    private int classType;

    /**
     * The Type of Service (ToS) of IPv4 for sending Cloudlet over the network.
     */
    private int netToS;

    /**
     * The format of decimal numbers.
     */
    private DecimalFormat num;

    /**
     * The id of the vm that is planned to execute the cloudlet.
     */
    protected int vmId;

    /**
     * The cost of each byte of bandwidth (bw) consumed.
     */
    protected double costPerBw;

    /**
     * The total bandwidth (bw) cost for transferring the cloudlet by the
     * network, according to the {@link #cloudletFileSize}.
     */
    protected double accumulatedBwCost;

    // Utilization
    /**
     * The utilization model that defines how the cloudlet will use the VM's
     * CPU.
     */
    private UtilizationModel utilizationModelCpu;

    /**
     * The utilization model that defines how the cloudlet will use the VM's
     * RAM.
     */
    private UtilizationModel utilizationModelRam;

    /**
     * The utilization model that defines how the cloudlet will use the VM's
     * bandwidth (bw).
     */
    private UtilizationModel utilizationModelBw;

    // Data cloudlet
    /**
     * The required files to be used by the cloudlet (if any). The time to
     * transfer these files by the network is considered when placing the
     * cloudlet inside a given VM
     */
    private List<String> requiredFiles = null;

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1. By default this
     * constructor sets the history of this object.
     *
     * @param cloudletId the unique ID of this Cloudlet
     * @param cloudletLength the length or size (in MI) of this cloudlet to be
     * executed in a PowerDatacenter
     * @param cloudletFileSize the file size (in byte) of this cloudlet
     * <tt>BEFORE</tt> submitting to a Datacenter
     * @param cloudletOutputSize the file size (in byte) of this cloudlet
     * <tt>AFTER</tt> finish executing by a Datacenter
     * @param pesNumber the pes number
     * @param utilizationModelCpu the utilization model of cpu
     * @param utilizationModelRam the utilization model of ram
     * @param utilizationModelBw the utilization model of bw
     *
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
    public Cloudlet(
            final int cloudletId,
            final long cloudletLength,
            final int pesNumber,
            final long cloudletFileSize,
            final long cloudletOutputSize,
            final UtilizationModel utilizationModelCpu,
            final UtilizationModel utilizationModelRam,
            final UtilizationModel utilizationModelBw) {
        this(
                cloudletId,
                cloudletLength,
                pesNumber,
                cloudletFileSize,
                cloudletOutputSize,
                utilizationModelCpu,
                utilizationModelRam,
                utilizationModelBw,
                false);
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     *
     * @param cloudletId the unique ID of this cloudlet
     * @param cloudletLength the length or size (in MI) of this cloudlet to be
     * executed in a PowerDatacenter
     * @param cloudletFileSize the file size (in byte) of this cloudlet
     * <tt>BEFORE</tt> submitting to a PowerDatacenter
     * @param cloudletOutputSize the file size (in byte) of this cloudlet
     * <tt>AFTER</tt> finish executing by a PowerDatacenter
     * @param record record the history of this object or not
     * @param fileList list of files required by this cloudlet
     * @param pesNumber the pes number
     * @param utilizationModelCpu the utilization model of cpu
     * @param utilizationModelRam the utilization model of ram
     * @param utilizationModelBw the utilization model of bw
     *
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
    public Cloudlet(
            final int cloudletId,
            final long cloudletLength,
            final int pesNumber,
            final long cloudletFileSize,
            final long cloudletOutputSize,
            final UtilizationModel utilizationModelCpu,
            final UtilizationModel utilizationModelRam,
            final UtilizationModel utilizationModelBw,
            final boolean record,
            final List<String> fileList) {
        this(
                cloudletId,
                cloudletLength,
                pesNumber,
                cloudletFileSize,
                cloudletOutputSize,
                utilizationModelCpu,
                utilizationModelRam,
                utilizationModelBw,
                record);

        setRequiredFiles(fileList);
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1. By default this
     * constructor sets the history of this object.
     *
     * @param cloudletId the unique ID of this Cloudlet
     * @param cloudletLength the length or size (in MI) of this cloudlet to be
     * executed in a PowerDatacenter
     * @param cloudletFileSize the file size (in byte) of this cloudlet
     * <tt>BEFORE</tt> submitting to a PowerDatacenter
     * @param cloudletOutputSize the file size (in byte) of this cloudlet
     * <tt>AFTER</tt> finish executing by a PowerDatacenter
     * @param fileList list of files required by this cloudlet
     * @param pesNumber the pes number
     * @param utilizationModelCpu the utilization model of cpu
     * @param utilizationModelRam the utilization model of ram
     * @param utilizationModelBw the utilization model of bw
     *
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
    public Cloudlet(
            final int cloudletId,
            final long cloudletLength,
            final int pesNumber,
            final long cloudletFileSize,
            final long cloudletOutputSize,
            final UtilizationModel utilizationModelCpu,
            final UtilizationModel utilizationModelRam,
            final UtilizationModel utilizationModelBw,
            final List<String> fileList) {
        this(
                cloudletId,
                cloudletLength,
                pesNumber,
                cloudletFileSize,
                cloudletOutputSize,
                utilizationModelCpu,
                utilizationModelRam,
                utilizationModelBw,
                false);
        setRequiredFiles(fileList);
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     *
     * @param cloudletId the unique ID of this cloudlet
     * @param cloudletLength the length or size (in MI) of this cloudlet to be
     * executed in a PowerDatacenter
     * @param cloudletFileSize the file size (in byte) of this cloudlet
     * <tt>BEFORE</tt> submitting to a PowerDatacenter
     * @param cloudletOutputSize the file size (in byte) of this cloudlet
     * <tt>AFTER</tt> finish executing by a PowerDatacenter
     * @param record record the history of this object or not
     * @param pesNumber the pes number
     * @param utilizationModelCpu the utilization model of cpu
     * @param utilizationModelRam the utilization model of ram
     * @param utilizationModelBw the utilization model of bw
     *
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
    public Cloudlet(
            final int cloudletId,
            final long cloudletLength,
            final int pesNumber,
            final long cloudletFileSize,
            final long cloudletOutputSize,
            final UtilizationModel utilizationModelCpu,
            final UtilizationModel utilizationModelRam,
            final UtilizationModel utilizationModelBw,
            final boolean record) {
        num = new DecimalFormat("#0.00#"); 
        userId = NOT_ASSIGNED;          // to be set by a Broker or user
        status = Status.CREATED;
        this.cloudletId = cloudletId;
        numberOfPes = pesNumber;
        execStartTime = 0.0;
        finishTime = NOT_ASSIGNED;    // meaning this Cloudlet hasn't finished yet
        classType = 0;
        netToS = 0;

        // Cloudlet length, Input and Output size should be at least 1 byte.
        this.cloudletLength = Math.max(1, cloudletLength);
        this.cloudletFileSize = Math.max(1, cloudletFileSize);
        this.cloudletOutputSize = Math.max(1, cloudletOutputSize);

	// Normally, a Cloudlet is only executed on a resource without being
        // migrated to others. Hence, to reduce memory consumption, set the
        // size of this ArrayList to be less than the default one.
        resList = new ArrayList<Resource>(2);
        index = NOT_ASSIGNED;
        this.record = record;

        vmId = NOT_ASSIGNED;
        accumulatedBwCost = 0.0;
        costPerBw = 0.0;

        requiredFiles = new LinkedList<String>();

        setUtilizationModelCpu(utilizationModelCpu);
        setUtilizationModelRam(utilizationModelRam);
        setUtilizationModelBw(utilizationModelBw);
    }

    // ////////////////////// INTERNAL CLASS ///////////////////////////////////
    /**
     * Internal class that keeps track of Cloudlet's movement in different
     * {@link Datacenter Datacenters}. Each time a cloudlet is run on a given Datacenter, the cloudlet's
     * execution history on each Datacenter is registered at {@link Cloudlet#resList}
     */
    private static class Resource {

        /**
         * Cloudlet's submission (arrival) time to a Datacenter.
         */
        public double submissionTime = 0.0;

        /**
         * The time this Cloudlet resides in a Datacenter (from arrival time
         * until departure time, that may include waiting time).
         */
        public double wallClockTime = 0.0;

        /**
         * The total time the Cloudlet spent being executed in a Datacenter.
         */
        public double actualCPUTime = 0.0;

        /**
         * Cost per second a Datacenter charge to execute this Cloudlet.
         */
        public double costPerSec = 0.0;

        /**
         * Cloudlet's length finished so far (in MI).
         */
        public long finishedSoFar = 0;

        /**
         * a Datacenter id.
         */
        public int resourceId = NOT_ASSIGNED;

        /**
         * The Datacenter name.
         */
        public String resourceName = "";
    }

    // ////////////////////// End of Internal Class //////////////////////////
    
    /**
     * Sets the id of the reservation made for this cloudlet.
     *
     * @param resId the reservation ID
     * @return <tt>true</tt> if the ID has successfully been set or
     * <tt>false</tt> otherwise.
     */
    public boolean setReservationId(final int resId) {
        if (resId <= 0) {
            return false;
        }
        reservationId = resId;
        return true;
    }

    /**
     * Gets the reservation ID that owns this Cloudlet.
     *
     * @return a reservation ID
     * @pre $none
     * @post $none
     */
    public int getReservationId() {
        return reservationId;
    }

    /**
     * Checks whether this Cloudlet is submitted by reserving or not.
     *
     * @return <tt>true</tt> if this Cloudlet was reserved before,
     * i.e, its {@link #reservationId} is not equals to {@link #NOT_ASSIGNED}; 
     * <tt>false</tt> otherwise
     */
    public boolean hasReserved() {
        return reservationId > NOT_ASSIGNED;
    }
    
    /**
     * Sets the length or size (in MI) of this Cloudlet to be executed in a
     * CloudResource. It has to be the length for each individual Pe,
     * <tt>not</tt> the total length (the sum of length to be executed by each
     * Pe).
     *
     * @param cloudletLength the length or size (in MI) of this Cloudlet to be
     * executed in a CloudResource
     * @return <tt>true</tt> if the cloudletLength is valid, <tt>false</tt> otherwise
     *
     * @see #getCloudletTotalLength() }
     * @pre cloudletLength > 0
     * @post $none
     */
    public boolean setCloudletLength(final long cloudletLength) {
        if (cloudletLength <= 0) {
            return false;
        }

        this.cloudletLength = cloudletLength;
        return true;
    }

    /**
     * Sets the network service level (ToS) for sending this cloudlet over a
     * network.
     *
     * @param netServiceLevel determines the type of service (ToS) this cloudlet
     * receives in the network (applicable to selected PacketScheduler class
     * only)
     * @return <code>true</code> if the netServiceLevel is valid, false otherwise.
     * @pre netServiceLevel >= 0
     * @post $none
     *
     * @todo The name of the setter is inconsistent with the attribute name,
     * what might be misinterpreted by other developers.
     */
    public boolean setNetServiceLevel(final int netServiceLevel) {
        if (netServiceLevel > 0) {
            netToS = netServiceLevel;
            return true;
        }

        return false;
    }

    /**
     * Gets the network service level (ToS) for sending this cloudlet over a
     * network.
     *
     * @return the network service level
     * @pre $none
     * @post $none
     * @todo The name of the getter is inconsistent with the attribute name,
     * what might be misinterpreted by other developers.
     */
    public int getNetServiceLevel() {
        return netToS;
    }

    /**
     * Gets the time the cloudlet had to wait before start executing on a
     * resource.
     *
     * @return the waiting time when the cloudlet waited 
     * to execute; or 0 if there wasn't any waiting time
     * or the cloudlet hasn't started to execute.
     * @pre $none
     * @post $none
     */
    public double getWaitingTime() {
        if (index == NOT_ASSIGNED) {
            return 0;
        }

        // use the latest resource submission time
        final double subTime = resList.get(index).submissionTime;
        return execStartTime - subTime;
    }

    /**
     * Sets the classType or priority of this Cloudlet for scheduling on a
     * resource.
     *
     * @param classType classType of this Cloudlet
     * @return <tt>true</tt> if it is classType is valid, <tt>false</tt> otherwise
     *
     * @pre classType > 0
     * @post $none
     */
    public boolean setClassType(final int classType) {
        if (classType > 0) {
            this.classType = classType;
            return true;
        }

        return false;
    }

    /**
     * Gets the classtype or priority of this Cloudlet for scheduling on a
     * resource.
     *
     * @return classtype of this cloudlet
     * @pre $none
     * @post $none
     */
    public int getClassType() {
        return classType;
    }

    /**
     * Sets the number of PEs required to run this Cloudlet. <br>
     * NOTE: The Cloudlet length is computed only for 1 Pe for simplicity. <br>
     * For example, consider a Cloudlet that has a length of 500 MI and requires
     * 2 PEs. This means each Pe will execute 500 MI of this Cloudlet.
     *
     * @param numberOfPes number of Pe
     * @return <tt>true</tt> if it is numberOfPes is valid, <tt>false</tt> otherwise
     *
     * @pre numPE > 0
     * @post $none
     */
    public boolean setNumberOfPes(final int numberOfPes) {
        if (numberOfPes > 0) {
            this.numberOfPes = numberOfPes;
            return true;
        }
        return false;
    }

    /**
     * Gets the number of PEs required to run this Cloudlet.
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $none
     */
    public int getNumberOfPes() {
        return numberOfPes;
    }

    /**
     * Gets the transaction history of this Cloudlet. The layout of this history
     * is in a readable table column with <tt>time</tt> and <tt>description</tt>
     * as headers.
     *
     * @return a String containing the history of this Cloudlet object.
     * @pre $none
     * @post $result != null
     */
    public String getCloudletHistory() {
        if (history == null) 
            return "No history is recorded for Cloudlet #" + cloudletId;
        
        return history.toString();
    }

    /**
     * Gets the length of this Cloudlet that has been executed so far from the
     * latest CloudResource (in MI). This method is useful when trying to move this
     * Cloudlet into different CloudResources or to cancel it.
     *
     * @return the length of a partially executed Cloudlet, or the full Cloudlet
     * length if it is completed
     * @pre $none
     * @post $result >= 0.0
     */
    public long getCloudletFinishedSoFar() {
        if (index == NOT_ASSIGNED) {
            return 0;
        }

        final long finish = resList.get(index).finishedSoFar;
        return Math.min(finish, cloudletLength);
    }

    /**
     * Checks whether this Cloudlet has finished execution or not.
     *
     * @return <tt>true</tt> if this Cloudlet has finished execution,
     * <tt>false</tt> otherwise
     * @pre $none
     * @post $none
     */
    public boolean isFinished() {
        if (index == NOT_ASSIGNED) {
            return false;
        }

        final long finishedMI = resList.get(index).finishedSoFar;
        final long remainingMI = cloudletLength - finishedMI;
        
        return remainingMI <= 0.0;
    }

    /**
     * Sets the length of this Cloudlet that has been executed so far. This
     * method is used by ResCloudlet class when an application is decided to
     * cancel or to move this Cloudlet into different CloudResources.
     *
     * @param length length of this Cloudlet
     * @return true if the length is valid and the cloudlet already has assigned
     * to a Datacenter, false otherwise
     * @see gridsim.AllocPolicy
     * @see gridsim.ResCloudlet
     * @pre length >= 0.0
     * @post $none
     */
    public boolean setCloudletFinishedSoFar(final long length) {
        if (length < 0.0 || index <= NOT_ASSIGNED) {
            return false;
        }

        final Resource res = resList.get(index);
        res.finishedSoFar = length;

        write("Sets the length's finished so far to %d", length);
        return true;
    }

    /**
     * Sets the user or owner ID of this Cloudlet. It is <tt>VERY</tt> important
     * to set the user ID, otherwise this Cloudlet will not be executed in a
     * CloudResource.
     *
     * @param userId the new user ID
     * @pre id >= 0
     * @post $none
     */
    public void setUserId(final int userId) {
        this.userId = userId;
        write("Assigns the Cloudlet to %s (ID #%d)", CloudSim.getEntityName(userId), userId);
    }

    /**
     * Gets the ID of the user or owner of this Cloudlet.
     *
     * @return the user ID or <tt>{@link #NOT_ASSIGNED}</tt> if the user ID has not been set before
     * @pre $none
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the ID of the latest {@link Datacenter} that has processed this Cloudlet.
     *
     * @return the Datacenter ID or <tt>{@link #NOT_ASSIGNED}</tt> if the Cloudlet
     * has not being processed yet.
     * @pre $none
     */
    public int getResourceId() {
        if (index == NOT_ASSIGNED) {
            return NOT_ASSIGNED;
        }
        return resList.get(index).resourceId;
    }

    /**
     * Gets the input file size of this Cloudlet <tt>BEFORE</tt> submitting to a
     * CloudResource.
     *
     * @return the input file size of this Cloudlet
     * @pre $none
     * @post $result >= 1
     */
    public long getCloudletFileSize() {
        return cloudletFileSize;
    }

    /**
     * Gets the output size of this Cloudlet <tt>AFTER</tt> submitting and
     * executing to a CloudResource.
     *
     * @return the Cloudlet output file size
     * @pre $none
     * @post $result >= 1
     */
    public long getCloudletOutputSize() {
        return cloudletOutputSize;
    }

    /**
     * Sets the parameters of the Datacenter where the Cloudlet is going to be
     * executed. From the second time this method is called, every call make the
     * cloudlet to be migrated to the indicated Datacenter.<br>
     *
     * NOTE: This method <tt>should</tt> be called only by a resource entity,
     * not the user or owner of this Cloudlet.
     *
     * @param resourceID the id of Datacenter where the cloudlet will be executed
     * @param cost the cost per second of running the cloudlet on the given Datacenter 
     *
     * @pre resourceID >= 0
     * @pre cost > 0.0
     * @post $none
     * @todo the method may have a better name, such as assignCloudletToDatacenter
     */
    public void setResourceParameter(final int resourceID, final double cost) {
        final Resource res = new Resource();
        res.resourceId = resourceID;
        res.costPerSec = cost;
        res.resourceName = CloudSim.getEntityName(resourceID);

        // add into a list if moving to a new cloud datacenter
        resList.add(res);

        if(record){
            if (index == NOT_ASSIGNED) {
                write(
                    "Allocates this Cloudlet to %s (ID #%d) with cost = $%s/sec",
                    res.resourceName,  resourceID, num.format(cost));
            } else {
                final int id = resList.get(index).resourceId;
                final String name = resList.get(index).resourceName;
                write(
                    "Moves Cloudlet from %s (ID #%d) to %s (ID #%d) with cost = $%s/sec",
                    name, id, res.resourceName, resourceID, num.format(cost));
            }
        }

        index++;  
    }

    /**
     * Sets the submission (arrival) time of this Cloudlet into a CloudResource.
     *
     * @param clockTime the submission time
     * @return true if the submission time is valid and 
     * the cloudlet has already being assigned to a datacenter for execution
     * @pre clockTime >= 0.0
     * @post $none
     */
    public boolean setSubmissionTime(final double clockTime) {
        if (clockTime < 0.0 || index <= NOT_ASSIGNED) {
            return false;
        }

        final Resource res = resList.get(index);
        res.submissionTime = clockTime;

        write("Sets the submission time to %s", num.format(clockTime));
        return true;
    }

    /**
     * Gets the submission (arrival) time of this Cloudlet from the latest
     * CloudResource.
     *
     * @return the submission time or <tt>0.0</tt> if 
     * the cloudlet has never being assigned to a datacenter
     * @pre $none
     * @post $result >= 0.0
     */
    public double getSubmissionTime() {
        if (index == NOT_ASSIGNED) {
            return 0.0;
        }
        return resList.get(index).submissionTime;
    }

    /**
     * Sets the execution start time of this Cloudlet inside a Datacenter.
     * <br/>
     * <b>NOTE:</b> With new functionalities, such as being able to cancel / to
     * pause / to resume this Cloudlet, the execution start time only holds the
     * latest one. Meaning, all previous execution start time are ignored.
     *
     * @param clockTime the latest execution start time
     * @pre clockTime >= 0.0
     * @post $none
     */
    public void setExecStartTime(final double clockTime) {
        execStartTime = clockTime;
        write("Sets the execution start time to %s", num.format(clockTime));
    }

    /**
     * Gets the latest execution start time.
     *
     * @return the latest execution start time
     * @pre $none
     * @post $result >= 0.0
     */
    public double getExecStartTime() {
        return execStartTime;
    }

    /**
     * Sets the Cloudlet's execution parameters. These parameters are set by the
     * Datacenter before departure or sending back to the original Cloudlet's
     * owner.
     *
     * @param wallTime the time of this Cloudlet resides in a Datacenter
     * (from arrival time until departure time).
     * @param actualTime the total execution time of this Cloudlet in a
     * Datacenter.
     * @return true if the submission time is valid and 
     * the cloudlet has already being assigned to a datacenter for execution
     * @see Resource#wallClockTime
     * @see Resource#actualCPUTime
     *
     * @pre wallTime >= 0.0
     * @pre actualTime >= 0.0
     * @post $none
     */
    public boolean setExecParam(final double wallTime, final double actualTime) {
        if (wallTime < 0.0 || actualTime < 0.0 || index <= NOT_ASSIGNED) {
            return false;
        }

        final Resource res = resList.get(index);
        res.wallClockTime = wallTime;
        res.actualCPUTime = actualTime;

        write("Sets the wall clock time to %s and the actual CPU time to %s",
              num.format(wallTime), num.format(actualTime));
        return true;
    }

    /**
     * Sets the execution status code of this Cloudlet.
     *
     * @param newStatus the status code of this Cloudlet
     * @return true if the cloudlet status was changed,
     * i.e, if the newStatus is different from the current status; false otherwise
     * @post $none
     */
    public boolean setCloudletStatus(final Status newStatus) {
        // if the new status is same as current one, then ignore the rest
        if (status == newStatus) {
            return false;
        }

        if (newStatus == Status.SUCCESS) {
            finishTime = CloudSim.clock();
        }

        write("Sets Cloudlet status from %s to %s",
              getCloudletStatusString(),
              Cloudlet.getCloudletStatusString(newStatus));

        status = newStatus;
        return true;
    }

    /**
     * Gets the status code of this Cloudlet.
     *
     * @return the status code of this Cloudlet
     * @pre $none
     * @post $result >= 0
     * @deprecated Use the getter {@link #getStatus()} instead
     */
    @Deprecated
    public Status getCloudletStatus() {
        return status;
    }

    /**
     * Gets the string representation of the current Cloudlet status code.
     *
     * @return the Cloudlet status code as a string or <tt>null</tt> if the
     * status code is unknown
     * @pre $none
     * @post $none
     */
    public String getCloudletStatusString() {
        return Cloudlet.getCloudletStatusString(status);
    }

    /**
     * Gets the string representation of the given Cloudlet status code.
     *
     * @param status The status to get a string representation
     * @return the Cloudlet status code as a string or <tt>null</tt> if the
     * status code is unknown
     * @pre $none
     * @post $none
     */
    public static String getCloudletStatusString(Status status) {
        return status.name();
    }

    /**
     * Gets the length of this Cloudlet.
     *
     * @return the length of this Cloudlet
     * @pre $none
     * @post $result >= 0.0
     */
    public long getCloudletLength() {
        return cloudletLength;
    }

    /**
     * Gets the total length (across all PEs) of this Cloudlet (in MI). It considers the
     * {@link #cloudletLength} of the cloudlet to be executed in each Pe and the
     * {@link #numberOfPes}.<br/>
     *
     * For example, setting the cloudletLenght as 10000 MI and
     * {@link #numberOfPes} to 4, each Pe will execute 10000 MI. Thus, the
     * entire cloudlet has a total length of 40000 MI.
     *
     *
     * @return the total length of this Cloudlet (in MI)
     *
     * @see #setCloudletLength(long)
     * @pre $none
     * @post $result >= 0.0
     */
    public long getCloudletTotalLength() {
        return getCloudletLength() * getNumberOfPes();
    }

    /**
     * Gets the cost/sec of running the Cloudlet in the latest Datacenter.
     *
     * @return the cost associated with running this Cloudlet or <tt>0.0</tt> if
     * was not assigned to any Datacenter yet
     * @pre $none
     * @post $result >= 0.0
     */
    public double getCostPerSec() {
        if (index == NOT_ASSIGNED) {
            return 0.0;
        }
        return resList.get(index).costPerSec;
    }

    /**
     * Gets the time of this Cloudlet resides in the latest Datacenter (from
     * arrival time until departure time).
     *
     * @return the time of this Cloudlet resides in the latest Datacenter
     * @pre $none
     * @post $result >= 0.0
     */
    public double getWallClockTime() {
        if (index == NOT_ASSIGNED) {
            return 0.0;
        }
        return resList.get(index).wallClockTime;
    }

    /**
     * Gets the names of all Datacenters that executed this Cloudlet.
     *
     * @return an array of Datacenter names where the Cloudlet has being executed
     * @pre $none
     * @post $none
     */
    public String[] getAllResourceName() {
        String[] data = new String[resList.size()];

        int i = 0;
        for (Resource res: resList) {
            data[i++] = res.resourceName;
        }

        return data;
    }

    /**
     * Gets the IDs of all Datacenters that executed this Cloudlet.
     *
     * @return an array of Datacenter IDs where the Cloudlet has being executed
     * @pre $none
     * @post $none
     */
    public Integer[] getAllResourceId() {
        Integer[] data = new Integer[resList.size()];

        int i = 0;
        for (Resource res: resList) {
            data[i++] = res.resourceId;
        }

        return data;
    }

    /**
     * Gets the total execution time of this Cloudlet in a given Datacenter
     * ID.
     *
     * @param resId the Datacenter entity ID
     * @return the total execution time of this Cloudlet in the given Datacenter 
     * or 0 if the Cloudlet was not executed there
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getActualCPUTime(final int resId) {
        Resource resource = getResourceById(resId);
        if (resource != null) {
            return resource.actualCPUTime;
        }
        return 0.0;
    }

    /**
     * Gets the cost running this Cloudlet in a given Datacenter ID.
     *
     * @param resId the Datacenter entity ID
     * @return the cost associated with running this Cloudlet in the given Datacenter 
     * or 0 if the Cloudlet was not executed there
     * not found
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getCostPerSec(final int resId) {
        Resource resource = getResourceById(resId);
        if (resource != null) {
            return resource.costPerSec;
        }
        return 0.0;
    }

    /**
     * Gets the length of this Cloudlet that has been executed so far in a given
     * Datacenter. This method is useful when trying to move this Cloudlet
     * into different CloudResources or to cancel it.
     *
     * @param resId the Datacenter entity ID
     * @return the length of a partially executed Cloudlet; the full Cloudlet
     * length if it is completed; or 0 if the Cloudlet has never being executed in the given Datacenter
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public long getCloudletFinishedSoFar(final int resId) {
        Resource resource = getResourceById(resId);
        if (resource != null) {
            return resource.finishedSoFar;
        }
        return 0;
    }

    /**
     * Gets the submission (arrival) time of this Cloudlet in the given Datacenter.
     *
     * @param resId the Datacenter entity ID
     * @return the submission time or 0 if the Cloudlet has never being executed in the given Datacenter
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getSubmissionTime(final int resId) {
        Resource resource = getResourceById(resId);
        if (resource != null) {
            return resource.submissionTime;
        }
        return 0.0;
    }

    /**
     * Gets the time of this Cloudlet resides in a given Datacenter (from
     * arrival time until departure time).
     *
     * @param resId a Datacenter entity ID
     * @return the time of this Cloudlet resides in the Datacenter 
     * or 0 if the Cloudlet has never being executed there
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getWallClockTime(final int resId) {
        Resource resource = getResourceById(resId);
        if (resource != null) {
            return resource.wallClockTime;
        }
        return 0.0;
    }

    /**
     * Gets the name of a Datacenter where the cloudlet has executed.
     *
     * @param resId the Datacenter entity ID
     * @return the Datacenter name or "" if the Cloudlet has never being executed in the given Datacenter
     * @pre resId >= 0
     * @post $none
     */
    public String getResourceName(final int resId) {
        Resource resource = getResourceById(resId);
        if (resource != null) {
            return resource.resourceName;
        }
        return "";
    }

    /**
     * Gets information about the cloudlet execution on a given Datacenter.
     *
     * @param resourceId the Datacenter entity ID
     * @return the Cloudlet execution information on the given datacenter
     * or null if the Cloudlet has never being executed there
     * @pre resId >= 0
     * @post $none
     */
    private Resource getResourceById(final int resourceId) {
        for (Resource resource : resList) {
            if (resource.resourceId == resourceId) {
                return resource;
            }
        }
        return null;
    }

    /**
     * Gets the finish time of this Cloudlet in the last Datacenter.
     *
     * @return the finish or completion time of this Cloudlet; or <tt>-1</tt> if
     * not finished yet.
     * @pre $none
     */
    public double getFinishTime() {
        return finishTime;
    }

    /**
     * Writes a particular history transaction of this Cloudlet into a log.
     *
     * @param str a history transaction of this Cloudlet
     * @pre str != null
     * @post $none
     */
    protected void write(final String str) {
        if (!record) {
            return;
        }

        if (num == null || history == null) { 
            // Creates the transaction history of this Cloudlet
            newline = System.getProperty("line.separator");
            history = new StringBuffer(1000);
            history.append("Time below denotes the simulation time.");
            history.append(System.getProperty("line.separator"));
            history.append("Time (sec)       Description Cloudlet #").append(cloudletId);
            history.append(System.getProperty("line.separator"));
            history.append("------------------------------------------");
            history.append(System.getProperty("line.separator"));
            history.append(num.format(CloudSim.clock()));
            history.append("   Creates Cloudlet ID #").append(cloudletId);
            history.append(System.getProperty("line.separator"));
        }

        history.append(num.format(CloudSim.clock()));
        history.append("   ").append(str).append(newline);
    }
    
    
    /**
     * Writes a formatted particular history transaction of this Cloudlet into a log.
     *
     * @param format the format of the Cloudlet's history transaction, according
     * to the format parameter of {@link String#format(java.lang.String, java.lang.Object...)}
     * @param args The list of values to be shown in the history,
     * that are referenced by the format.
     * @pre format != null
     * @post $none
     * @see #write(java.lang.String) 
     */
    protected void write(final String format, Object... args) {
        final String str = String.format(format, args);
        write(str);
    }

    /**
     * Gets the status of the Cloudlet.
     *
     * @return status of the Cloudlet
     * @pre $none
     * @post $none
     *
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the ID of this Cloudlet.
     *
     * @return Cloudlet Id
     * @pre $none
     * @post $none
     */
    public int getCloudletId() {
        return cloudletId;
    }

    /**
     * Gets the ID of the VM that will run this Cloudlet.
     *
     * @return VM Id, {@link #NOT_ASSIGNED} if the Cloudlet was not assigned to a VM yet
     * @pre $none
     * @post $none
     */
    public int getVmId() {
        return vmId;
    }

    /**
     * Sets the ID of the VM that will run this Cloudlet.
     *
     * @param vmId the vm id
     * @pre id >= 0
     * @post $none
     */
    public void setVmId(final int vmId) {
        this.vmId = vmId;
    }

    /**
     * Returns the total execution time of the Cloudlet.
     *
     * @return time in which the Cloudlet was running 
     * or {@link #NOT_ASSIGNED} if it hasn't finished yet
     * @pre $none
     * @post $none
     */
    public double getActualCPUTime() {
        if(getFinishTime() == NOT_ASSIGNED)
            return NOT_ASSIGNED;
        
        return getFinishTime() - getExecStartTime();
    }

    /**
     * Sets the parameters of the Datacenter where the Cloudlet is going to be
     * executed. <br>
     * NOTE: This method <tt>should</tt> be called only by a resource entity,
     * not the user or owner of this Cloudlet.
     *
     * @param resourceID the id of Datacenter where the cloudlet will be executed
     * @param costPerCPU the cost per second of running this Cloudlet on the Datacenter
     * @param costPerBw the cost per byte of data transfer of the Datacenter
     *
     * @pre resourceID >= 0
     * @pre cost > 0.0
     * @post $none
     */
    public void setResourceParameter(final int resourceID, final double costPerCPU, final double costPerBw) {
        setResourceParameter(resourceID, costPerCPU);
        this.costPerBw = costPerBw;
        accumulatedBwCost = costPerBw * getCloudletFileSize();
    }

    /**
     * Gets the total cost of processing or executing this Cloudlet
     * <tt>Processing Cost = input data transfer + processing cost + output
     * transfer cost</tt> .
     *
     * @return the total cost of processing Cloudlet
     * @pre $none
     * @post $result >= 0.0
     */
    public double getProcessingCost() {
        // cloudlet cost: execution cost...
        // double cost = getProcessingCost();
        double cost = 0;
        // ...plus input data transfer cost...
        cost += accumulatedBwCost;
        // ...plus output cost
        cost += costPerBw * getCloudletOutputSize();
        return cost;
    }

    /**
     * Gets the required files.
     *
     * @return the required files
     */
    public List<String> getRequiredFiles() {
        return requiredFiles;
    }

    /**
     * Sets the required files.
     *
     * @param requiredFiles the new required files
     */
    protected final void setRequiredFiles(final List<String> requiredFiles) {
        if(requiredFiles == null)
            this.requiredFiles = new LinkedList<String>();
        else this.requiredFiles = requiredFiles;
    }

    /**
     * Adds the required filename to the list.
     *
     * @param fileName the required filename
     * @return <tt>true</tt> if the file was added (it didn't exist in the 
     * list of required files), <tt>false</tt> otherwise (it did already exist)
     */
    public boolean addRequiredFile(final String fileName) {
        //check whether filename already exists or not
        for (int i = 0; i < getRequiredFiles().size(); i++) {
            final String temp = getRequiredFiles().get(i);
            if (temp.equals(fileName)) {
                return false;
            }
        }

        getRequiredFiles().add(fileName);
        return true;
    }

    /**
     * Deletes the given filename from the list.
     *
     * @param filename the given filename to be deleted
     * @return <tt>true</tt> if the file was found and removed, <tt>false</tt> 
     * if not found
     */
    public boolean deleteRequiredFile(final String filename) {
        for (int i = 0; i < getRequiredFiles().size(); i++) {
            final String temp = getRequiredFiles().get(i);

            if (temp.equals(filename)) {
                getRequiredFiles().remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether this cloudlet requires any files or not.
     *
     * @return <tt>true</tt> if required, <tt>false</tt> otherwise
     */
    public boolean requiresFiles() {
        boolean result = false;
        if (getRequiredFiles() != null && getRequiredFiles().size() > 0) {
            result = true;
        }

        return result;
    }

    /**
     * Gets the utilization model of cpu.
     *
     * @return the utilization model cpu
     */
    public UtilizationModel getUtilizationModelCpu() {
        return utilizationModelCpu;
    }

    /**
     * Sets the utilization model of cpu.
     *
     * @param utilizationModelCpu the new utilization model of cpu
     */
    public final void setUtilizationModelCpu(final UtilizationModel utilizationModelCpu) {
        if(utilizationModelCpu == null)
            throw new IllegalArgumentException("The CPU utilization model cannot be null");
        this.utilizationModelCpu = utilizationModelCpu;
    }

    /**
     * Gets the utilization model of ram.
     *
     * @return the utilization model of ram
     */
    public UtilizationModel getUtilizationModelRam() {
        return utilizationModelRam;
    }

    /**
     * Sets the utilization model of ram.
     *
     * @param utilizationModelRam the new utilization model of ram
     */
    public final void setUtilizationModelRam(final UtilizationModel utilizationModelRam) {
        if(utilizationModelRam == null)
            throw new IllegalArgumentException("The RAM utilization model cannot be null");
        this.utilizationModelRam = utilizationModelRam;
    }

    /**
     * Gets the utilization model of bw.
     *
     * @return the utilization model of bw
     */
    public UtilizationModel getUtilizationModelBw() {
        return utilizationModelBw;
    }

    /**
     * Sets the utilization model of bw.
     *
     * @param utilizationModelBw the new utilization model of bw
     */
    public final void setUtilizationModelBw(final UtilizationModel utilizationModelBw) {
        if(utilizationModelBw == null)
            throw new IllegalArgumentException("The BW utilization model cannot be null");
        this.utilizationModelBw = utilizationModelBw;
    }

    /**
     * Gets the utilization percentage of cpu.
     *
     * @param time the time
     * @return the utilization of cpu
     */
    public double getUtilizationOfCpu(final double time) {
        return getUtilizationModelCpu().getUtilization(time);
    }

    /**
     * Gets the utilization percentage of memory.
     *
     * @param time the time
     * @return the utilization of memory
     */
    public double getUtilizationOfRam(final double time) {
        return getUtilizationModelRam().getUtilization(time);
    }

    /**
     * Gets the utilization percentage of bw.
     *
     * @param time the time
     * @return the utilization of bw
     */
    public double getUtilizationOfBw(final double time) {
        return getUtilizationModelBw().getUtilization(time);
    }

}
