package org.cloudbus.cloudsim.provisioners;

import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * A class that represents the provisioning policy
 * used by a host to allocate its PEs to virtual machines inside it.
 * It gets a physical PE and manage it in order to provide this PE as virtual PEs for VMs.
 * In that way, a given PE might be shared among different VMs.
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.1
 */
public interface PeProvisioner extends ResourceProvisioner {

    /**
     * Sets the {@link Pe} that this provisioner will manage.
     *
     * @param pe the Pe to set
     */
    void setPe(Pe pe);

    /**
     * Allocates an amount of MIPS from the physical Pe to a new virtual PE for a given VM.
     * The virtual PE to be added will use the total or partial MIPS capacity of the
     * physical PE.
     *
     * @param vm the virtual machine for which the new virtual PE is being allocated
     * @param mipsCapacity the MIPS to be allocated to the virtual PE of the given VM
     * @return $true if the virtual PE could be allocated; $false otherwise
     *
     * @pre $none
     * @post $none
     */
    @Override
    boolean allocateResourceForVm(Vm vm, long mipsCapacity);

    /**
     * Gets the amount of allocated MIPS from the physical Pe to a virtual PE of a VM.
     *
     * @param vm the virtual machine to get the allocated virtual Pe MIPS
     * @return the allocated virtual Pe MIPS
     *
     * @pre $none
     * @post $none
     */
    @Override
    long getAllocatedResourceForVm(Vm vm);

    /**
     * Releases the virtual Pe allocated to a given VM.
     *
     * @param vm the vm to release the virtual Pe
     *
     * @pre $none
     * @post none
     */
    @Override
    boolean deallocateResourceForVm(Vm vm);

    /**
     * Releases all virtual PEs allocated to all VMs.
     *
     * @pre $none
     * @post none
     */
    @Override
    void deallocateResourceForAllVms();

    /**
     * Gets the total allocated MIPS from the physical Pe.
     *
     * @return the total allocated MIPS
     */
    @Override
    long getTotalAllocatedResource();

    /**
     * Gets the utilization percentage of the Pe in scale from 0 to 1.
     *
     * @return the utilization percentage from 0 to 1
     */
    double getUtilization();

    /**
     * A property that implements the Null Object Design Pattern for
     * PeProvisioner objects.
     */
    PeProvisioner NULL = new PeProvisioner(){
        @Override public void setPe(Pe pe) {}
        @Override public boolean allocateResourceForVm(Vm vm, long newTotalVmResourceCapacity) { return false; }
        @Override public boolean allocateResourceForVm(Vm vm, double newTotalVmResource) { return false; }
        @Override public long getAllocatedResourceForVm(Vm vm) { return 0; }
        @Override public long getTotalAllocatedResource() { return 0; }
        @Override public double getUtilization() { return 0; }
        @Override public boolean deallocateResourceForVm(Vm vm) { return false; }
        @Override public void deallocateResourceForAllVms() {}
        @Override public boolean isSuitableForVm(Vm vm, long newVmTotalAllocatedResource) { return false; }
        @Override public ResourceManageable getResource() { return ResourceManageable.NULL; }
        @Override public long getCapacity() { return 0; }
        @Override public long getAvailableResource() { return 0; }
    };
}
