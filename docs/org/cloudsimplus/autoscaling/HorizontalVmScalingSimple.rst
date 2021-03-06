.. java:import:: org.cloudbus.cloudsim.brokers DatacenterBroker

.. java:import:: org.cloudbus.cloudsim.util Log

.. java:import:: org.cloudbus.cloudsim.vms Vm

.. java:import:: java.util Objects

.. java:import:: java.util.function Predicate

.. java:import:: java.util.function Supplier

HorizontalVmScalingSimple
=========================

.. java:package:: org.cloudsimplus.autoscaling
   :noindex:

.. java:type:: public class HorizontalVmScalingSimple extends VmScalingAbstract implements HorizontalVmScaling

   A \ :java:ref:`HorizontalVmScaling`\  implementation that allows defining the condition to identify an overloaded VM based on any desired criteria, such as current RAM, CPU and/or Bandwidth utilization. A \ :java:ref:`DatacenterBroker`\  thus monitors the VMs that have an HorizontalVmScaling object in order to create or destroy VMs on demand..

   The condition in fact has to be defined by the user of this class, by providing a \ :java:ref:`Predicate`\  using the \ :java:ref:`setOverloadPredicate(Predicate)`\  method.

   :author: Manoel Campos da Silva Filho

Constructors
------------
HorizontalVmScalingSimple
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public HorizontalVmScalingSimple()
   :outertype: HorizontalVmScalingSimple

Methods
-------
getVmSupplier
^^^^^^^^^^^^^

.. java:method:: @Override public Supplier<Vm> getVmSupplier()
   :outertype: HorizontalVmScalingSimple

requestScaling
^^^^^^^^^^^^^^

.. java:method:: @Override protected boolean requestScaling(double time)
   :outertype: HorizontalVmScalingSimple

setVmSupplier
^^^^^^^^^^^^^

.. java:method:: @Override public final HorizontalVmScaling setVmSupplier(Supplier<Vm> supplier)
   :outertype: HorizontalVmScalingSimple

