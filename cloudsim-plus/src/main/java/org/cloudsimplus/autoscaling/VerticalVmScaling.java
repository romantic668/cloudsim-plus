/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2016  Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.autoscaling;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import static org.cloudbus.cloudsim.utilizationmodels.UtilizationModel.Unit;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.function.Predicate;

/**
 * A Vm <a href="https://en.wikipedia.org/wiki/Scalability#Horizontal_and_vertical_scaling">Vertical Scaling</a> mechanism
 * used by a {@link DatacenterBroker} to dynamically scale VM resources up or down, according to the current resource usage.
 * For each resource that is supposed to be scaled, a different {@code VerticalVmScaling} instance should be provided.
 *
 * <p>A {@link Vm} runs a set of {@link Cloudlet}s. When you attach a {@code VerticalVmScaling} object to a {@link Vm},
 *    you have to define which {@link #getResourceClassToScale() resource will be scaled} ({@link Ram}, {@link Bandwidth}, etc)
 *    when it's {@link #getUnderloadPredicate() under} or {@link #getOverloadPredicate() overloaded}.
 * </p>
 *
 *
 * <h1>WARNING</h1>
 * <hr>
 *    Make sure that the {@link UtilizationModel} of some of these {@code Cloudlets}
 *    is defined as {@link Unit#ABSOLUTE ABSOLUTE}. Defining the {@code UtilizationModel}
 *    of all {@code Cloudlets} running inside the {@code Vm} as {@link Unit#PERCENTAGE PERCENTAGE}
 *    causes these {@code Cloudlets} to automatically increase/decrease their resource usage when the {@code Vm} resource
 *    is vertically scaled. This is not a CloudSim Plus issue, but the natural and maybe surprising effect
 *    that may trap researchers trying to implement and assess VM scaling policies.
 *
 *    <p>Consider the following example: a {@code VerticalVmScaling} is attached to a {@code Vm} to double its {@link Ram}
 *    when its usage reaches 50%. The {@code Vm} has 10GB of RAM.
 *    All {@code Cloudlets} running inside this {@code Vm} have a {@link UtilizationModel}
 *    for their RAM utilization define in {@link Unit#PERCENTAGE PERCENTAGE}. When the RAM utilization of all these
 *    {@code Cloudlets} reach the 50% (5GB), the {@code Vm} {@link Ram} will be doubled.
 *    However, as the RAM usage of the running {@code Cloudlets} is defined in percentage, they will
 *    continue to use 50% of {@code Vm}'s RAM, that now represents 10GB from the 20GB capacity.
 *    This way, the vertical scaling will have no real benefit.</p>
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.1
 */
public interface VerticalVmScaling extends VmScaling {
    /**
     * Gets the class of Vm resource that this scaling object will request up or down scaling.
     * Such a class can be {@link Ram}.class, {@link Bandwidth}.class or {@link Pe}.class.
     * @return
     */
    Class<? extends ResourceManageable> getResourceClassToScale();

    /**
     * Sets the class of Vm resource that this scaling object will request up or down scaling.
     * Such a class can be {@link Ram}.class, {@link Bandwidth}.class or {@link Pe}.class.
     * @param resourceClassToScale the resource class to set
     * @return
     */
    VerticalVmScaling setResourceClassToScale(Class<? extends ResourceManageable> resourceClassToScale);

    /**
     * Gets the factor that will be used to scale a Vm resource up or down,
     * whether such a resource is over or underloaded, according to the
     * defined predicates.
     *
     * <p>This is a percentage value in scale from 0 to 1. Every time the
     * VM needs to be scaled up or down, this factor will be applied
     * to increase or reduce a specific VM allocated resource.</p>
     *
     * @return the scaling factor
     * @see #getOverloadPredicate()
     */
    double getScalingFactor();

    /**
     * Gets the absolute amount of the Vm resource, defined
     * by {@link #getResourceClassToScale()}, that has to be
     * scaled up or down, based on the {@link #getScalingFactor() scaling factor}.
     * @return the absolute amount of the Vm resource to scale
     */
    double getResourceAmountToScale();

    /**
     * Sets the factor that will be used to scale a Vm resource up or down,
     * whether such a resource is over or underloaded, according to the
     * defined predicates.
     *
     * <p>This is a percentage value in scale from 0 to 1. Every time the
     * VM needs to be scaled up or down, this factor will be applied
     * to increase or reduce a specific VM allocated resource.</p>
     *
     * @param scalingFactor the scaling factor to set
     * @see #getOverloadPredicate()
     */
    VerticalVmScaling setScalingFactor(double scalingFactor);

    /**
     * Performs the vertical scale if the Vm is overloaded, according to the {@link #getOverloadPredicate()} predicate,
     * increasing the Vm resource to which the scaling object is linked to (that may be RAM, CPU, BW, etc),
     * by the factor defined a scaling factor.
     *
     * <p>The time interval in which it will be checked if the Vm is overloaded
     * depends on the {@link Datacenter#getSchedulingInterval()} value.
     * Make sure to set such a value to enable the periodic overload verification.</p>
     *
     * @param time current simulation time
     * @see #getScalingFactor()
     */
    @Override
    boolean requestScalingIfPredicateMatch(double time);

    /**
     * {@inheritDoc}
     *
     * <p>The up scaling is performed by increasing the amount of the {@link #getResourceClassToScale() resource}
     * the scaling is associated to.</p>
     * @return {@inheritDoc}
     */
    @Override
    Predicate<Vm> getOverloadPredicate();

    /**
     * {@inheritDoc}
     *
     * <p>The up scaling is performed by increasing the amount of the {@link #getResourceClassToScale() resource}
     * the scaling is associated to.</p>
     * @param predicate {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    VmScaling setOverloadPredicate(Predicate<Vm> predicate);

    /**
     * {@inheritDoc}
     *
     * <p>The down scaling is performed by decreasing the amount of the {@link #getResourceClassToScale() resource}
     * the scaling is associated to.</p>
     * @return {@inheritDoc}
     */
    @Override
    Predicate<Vm> getUnderloadPredicate();

    /**
     * {@inheritDoc}
     *
     * <p>The down scaling is performed by decreasing the amount of the {@link #getResourceClassToScale() resource}
     * the scaling is associated to.</p>
     * @param predicate {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    VmScaling setUnderloadPredicate(Predicate<Vm> predicate);

    /**
     * An attribute that implements the Null Object Design Pattern for {@link VerticalVmScaling}
     * objects.
     */
    VerticalVmScaling NULL = new VerticalVmScaling() {
        @Override public Class<? extends ResourceManageable> getResourceClassToScale() { return ResourceManageable.class; }
        @Override public VerticalVmScaling setResourceClassToScale(Class<? extends ResourceManageable> resourceClassToScale) { return this; }
        @Override public double getScalingFactor() { return 0; }
        @Override public double getResourceAmountToScale() { return 0; }
        @Override public VerticalVmScaling setScalingFactor(double scalingFactor) { return this; }
        @Override public boolean requestScalingIfPredicateMatch(double time) { return false; }
        @Override public Vm getVm() { return Vm.NULL; }
        @Override public VmScaling setVm(Vm vm) { return this; }
        @Override public Predicate<Vm> getOverloadPredicate() { return FALSE_PREDICATE; }
        @Override public VmScaling setOverloadPredicate(Predicate<Vm> predicate) { return this; }
        @Override public Predicate<Vm> getUnderloadPredicate() { return FALSE_PREDICATE; }
        @Override public VmScaling setUnderloadPredicate(Predicate<Vm> predicate) { return this; }
    };

}
