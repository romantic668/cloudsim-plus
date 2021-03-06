.. java:import:: java.util ArrayList

.. java:import:: java.util Collections

.. java:import:: java.util HashMap

.. java:import:: java.util Iterator

.. java:import:: java.util List

.. java:import:: java.util Map

.. java:import:: org.cloudbus.cloudsim.hosts Host

.. java:import:: org.cloudbus.cloudsim.resources Pe

.. java:import:: org.cloudbus.cloudsim.vms Vm

VmSchedulerSpaceShared
======================

.. java:package:: org.cloudbus.cloudsim.schedulers.vm
   :noindex:

.. java:type:: public class VmSchedulerSpaceShared extends VmSchedulerAbstract

   VmSchedulerSpaceShared is a VMM allocation policy that allocates one or more PEs from a host to a Virtual Machine Monitor (VMM), and doesn't allow sharing of PEs. The allocated PEs will be used until the VM finishes running. If there is no enough free PEs as required by a VM, or whether the available PEs doesn't have enough capacity, the allocation fails. In the case of fail, no PE is allocated to the requesting VM.

   :author: Rodrigo N. Calheiros, Anton Beloglazov

Constructors
------------
VmSchedulerSpaceShared
^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public VmSchedulerSpaceShared()
   :outertype: VmSchedulerSpaceShared

   Instantiates a new vm space-shared scheduler.

Methods
-------
allocatePesForVm
^^^^^^^^^^^^^^^^

.. java:method:: @Override public boolean allocatePesForVm(Vm vm, List<Double> mipsShareRequested)
   :outertype: VmSchedulerSpaceShared

deallocatePesForVm
^^^^^^^^^^^^^^^^^^

.. java:method:: @Override public void deallocatePesForVm(Vm vm)
   :outertype: VmSchedulerSpaceShared

getCpuOverheadDueToVmMigration
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Override public double getCpuOverheadDueToVmMigration()
   :outertype: VmSchedulerSpaceShared

getFreePesList
^^^^^^^^^^^^^^

.. java:method:: protected final List<Pe> getFreePesList()
   :outertype: VmSchedulerSpaceShared

   Gets the free pes list.

   :return: the free pes list

getPeAllocationMap
^^^^^^^^^^^^^^^^^^

.. java:method:: protected Map<Vm, List<Pe>> getPeAllocationMap()
   :outertype: VmSchedulerSpaceShared

   Gets the pe allocation map.

   :return: the pe allocation map

getTotalCapacityToBeAllocatedToVm
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: protected List<Pe> getTotalCapacityToBeAllocatedToVm(List<Double> vmRequestedMipsShare)
   :outertype: VmSchedulerSpaceShared

   Checks if the requested amount of MIPS is available to be allocated to a VM

   :param vmRequestedMipsShare: a VM's list of requested MIPS
   :return: the list of PEs that can be allocated to the VM or an empty list if there isn't enough capacity that can be allocated

isSuitableForVm
^^^^^^^^^^^^^^^

.. java:method:: @Override public boolean isSuitableForVm(Vm vm)
   :outertype: VmSchedulerSpaceShared

setFreePesList
^^^^^^^^^^^^^^

.. java:method:: protected final void setFreePesList(List<Pe> freePesList)
   :outertype: VmSchedulerSpaceShared

   Sets the free pes list.

   :param freePesList: the new free pes list

setHost
^^^^^^^

.. java:method:: @Override public VmScheduler setHost(Host host)
   :outertype: VmSchedulerSpaceShared

setPeAllocationMap
^^^^^^^^^^^^^^^^^^

.. java:method:: protected final void setPeAllocationMap(Map<Vm, List<Pe>> peAllocationMap)
   :outertype: VmSchedulerSpaceShared

   Sets the pe allocation map.

   :param peAllocationMap: the pe allocation map

