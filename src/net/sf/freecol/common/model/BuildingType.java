/**
 *  Copyright (C) 2002-2013   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;
import net.sf.freecol.common.model.UnitLocation.NoAddReason;


/**
 * Encapsulates data common to all instances of a particular kind of
 * {@link Building}, such as the number of workplaces, and the types
 * of goods it produces and consumes.
 */
public final class BuildingType extends BuildableType
    implements Comparable<BuildingType> {

    /** The level of building. */
    private int level = 1;

    /** The number of work places a unit can work in buildings of this type. */
    private int workPlaces = 3;

    /** The minimum unit skill to work in buildings of this type. */
    private int minSkill = UNDEFINED;
    /** The maximum unit skill to work in buildings of this type. */
    private int maxSkill = INFINITY;

    /** Upkeep per turn for buildings ot this type. */
    private int upkeep = 0;

    /** Consumption order. */
    private int priority = Consumer.BUILDING_PRIORITY;

    /** The building type this upgrades from. */
    private BuildingType upgradesFrom = null;

    /** The building type this upgrades to. */
    private BuildingType upgradesTo = null;

    /** The possible production types of this building type. */
    private final List<ProductionType> productionTypes
        = new ArrayList<ProductionType>();


    /**
     * Creates a new <code>BuildingType</code> instance.
     *
     * @param id The object identifier.
     * @param specification The <code>Specification</code> to refer to.
     */
    public BuildingType(String id, Specification specification) {
        super(id, specification);
    }


    /**
     * Get the level of this BuildingType.
     *
     * @return The building level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the number of workplaces, that is the maximum number of
     * Units that can work in this BuildingType.
     *
     * @return The number of work places.
     */
    public int getWorkPlaces() {
        return workPlaces;
    }

    /**
     * Gets the amount of gold necessary to maintain a Building of
     * this type for one turn.
     *
     * @return The per turn upkeep for this building type.
     */
    public int getUpkeep() {
        return upkeep;
    }

    /**
     * The consumption priority of a Building of this type. The higher
     * the priority, the earlier will the Consumer be allowed to
     * consume the goods it requires.
     *
     * @return The consumption priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Gets the reason why a given unit type can not be added to a
     * building of this type.
     *
     * @param unitType The <code>UnitType</code> to test.
     * @return The reason why adding would fail.
     */
    public NoAddReason getNoAddReason(UnitType unitType) {
        if (workPlaces == 0) {
            return NoAddReason.CAPACITY_EXCEEDED;
        } else if (!unitType.hasSkill()) {
            return NoAddReason.MISSING_SKILL;
        } else if (unitType.getSkill() < minSkill) {
            return NoAddReason.MINIMUM_SKILL;
        } else if (unitType.getSkill() > maxSkill) {
            return NoAddReason.MAXIMUM_SKILL;
        } else {
            return NoAddReason.NONE;
        }
    }

    /**
     * Can a unit of a given type be added to a Building of this type?
     *
     * @param unitType The <code>UnitType</code> to check.
     * @return True if the unit type can be added.
     */
    public boolean canAdd(UnitType unitType) {
        return getNoAddReason(unitType) == NoAddReason.NONE;
    }

    /**
     * Gets the type of the building type, which is trivially just this
     * object.
     *
     * @return This.
     */
    public FreeColGameObjectType getType() {
        return this;
    }

    /**
     * Gets the BuildingType this BuildingType upgrades from.
     *
     * @return The <code>BuildingType</code> that upgrades to this one.
     */
    public BuildingType getUpgradesFrom() {
        return upgradesFrom;
    }

    /**
     * Get the BuildingType this BuildingType upgrades to.
     *
     * @return The <code>BuildingType</code> to upgrade to from this one.
     */
    public BuildingType getUpgradesTo() {
        return upgradesTo;
    }

    /**
     * Gets the first level of this BuildingType.
     *
     * @return The base <code>BuildingType</code>.
     */
    public BuildingType getFirstLevel() {
        BuildingType buildingType = this;
        while (buildingType.getUpgradesFrom() != null) {
            buildingType = buildingType.getUpgradesFrom();
        }
        return buildingType;
    }

    /**
     * Is this building type automatically built in any colony?
     *
     * @return True if this building type is automatically built.
     */
    public boolean isAutomaticBuild() {
        return !needsGoodsToBuild() && getUpgradesFrom() == null;
    }

    /**
     * Add a production type to this building type.
     *
     * @param productionType The <code>ProductionType</code> to add.
     */
    public void addProductionType(ProductionType productionType) {
        if (productionType != null) productionTypes.add(productionType);
    }

    /**
     * Get the production types provided by this building type at the
     * current difficulty level.
     *
     * @param unattended Whether the production is unattended.
     * @return A list of <code>ProductionType</code>s.
     */
    public List<ProductionType> getAvailableProductionTypes(boolean unattended) {
        return getAvailableProductionTypes(unattended, null);
    }

    /**
     * Gets the production types available at the current difficulty
     * level.
     *
     * TODO: TileType.getAvailableProductionTypes(boolean) uses the
     * GameOptions.TILE_PRODUCTION option.  We should implement a
     * corresponding one for BuildingTypes.
     *
     * @param unattended Whether the production is unattended.
     * @param level The production level (NYI).
     * @return A list of <code>ProductionType</code>s.
     */
    public List<ProductionType> getAvailableProductionTypes(boolean unattended,
                                                            String level) {
        List<ProductionType> result = new ArrayList<ProductionType>();
        for (ProductionType productionType : productionTypes) {
            if (productionType.isUnattended() == unattended
                && productionType.appliesTo(level)) {
                result.add(productionType);
            }
        }
        return result;
    }

    // @compat 0.10.6
    /**
     * Get the type of goods consumed by this BuildingType.
     *
     * @return The consumed <code>GoodsType</code>.
     */
    private GoodsType getConsumedGoodsType() {
        if (productionTypes.isEmpty()) return null;
        List<AbstractGoods> inputs = productionTypes.get(0).getInputs();
        return (inputs.isEmpty()) ? null : inputs.get(0).getType();
    }
    // end @compat

    /**
     * Gets the type of goods produced by this BuildingType.
     *
     * @return The produced <code>GoodsType</code>.
     */
    public GoodsType getProducedGoodsType() {
        if (productionTypes.isEmpty()) return null;
        List<AbstractGoods> outputs = productionTypes.get(0).getOutputs();
        return (outputs.isEmpty()) ? null : outputs.get(0).getType();
    }

    /**
     * Is this a defence-related building type?  Such buildings
     * (stockade et al) are visible to other players.
     *
     * @return True if this is a defence related building.
     */
    public boolean isDefenceType() {
        return containsModifierKey(Modifier.DEFENCE);
    }

    /**
     * Can a tile of this type produce a given goods type?
     *
     * @param goodsType The <code>GoodsType</code> to produce.
     * @param unitType An optional <code>UnitType</code> that is to do
     *     the work, if null the unattended production is considered.
     * @return True if this tile type produces the goods.
     */
    public boolean canProduce(GoodsType goodsType, UnitType unitType) {
        return goodsType != null
            && ProductionType.canProduce(goodsType,
                getAvailableProductionTypes(unitType == null));
    }

    /**
     * Get the base production of a given goods type for an optional
     * unit type.
     *
     * @param productionType An optional <code>ProductionType</code> to use,
     *     if null the best available one is used.
     * @param goodsType The <code>GoodsType</code> to produce.
     * @param unitType An optional <code>UnitType</code> that is to do
     *     the work, if null the unattended production is considered.
     * @return The amount of goods produced.
     */
    public int getBaseProduction(ProductionType productionType,
                                 GoodsType goodsType, UnitType unitType) {
        if (goodsType == null) return 0;
        if (productionType == null) {
            productionType = ProductionType.getBestProductionType(goodsType,
                getAvailableProductionTypes(unitType == null));
        }
        if (productionType == null) return 0;
        AbstractGoods best = productionType.getOutput(goodsType);
        return (best == null) ? 0 : best.getAmount();
    }

    /** 
     * Get the amount of goods of a given goods type the given unit
     * type could produce on a tile of this tile type.
     *
     * @param goodsType The <code>GoodsType</code> to produce.
     * @param unitType An optional <code>UnitType</code> that is to do
     *     the work, if null the unattended production is considered.
     * @return The amount of goods produced.
     */
    public int getPotentialProduction(GoodsType goodsType,
                                      UnitType unitType) {
        if (goodsType == null) return 0;
        int amount = (int)applyModifier(getBaseProduction(null, goodsType,
                                                          unitType),
                                        goodsType.getId(), unitType);
        return (amount < 0) ? 0 : amount;
    }


    // Override FreeColGameObjectType

    /**
     * {@inheritDoc}
     */
    @Override
    public int getModifierIndex(String id) {
        GoodsType produces = getProducedGoodsType();
        return (produces != null && produces.getId().equals(id))
            ? Modifier.AUTO_PRODUCTION_INDEX
            : Modifier.BUILDING_PRODUCTION_INDEX;
    }


    // Interface Comparable

    /**
     * Compares this BuildingType to another.  BuildingTypes are
     * simply sorted according to the order in which they are defined
     * in the specification.
     *
     * @param other The other <code>BuildingType</code> to compare with.
     * @return A comparison result.
     */
    public int compareTo(BuildingType other) {
        return getIndex() - other.getIndex();
    }


    // Serialization

    private static final String BASIC_PRODUCTION_TAG = "basicProduction";
    private static final String CONSUMES_TAG = "consumes";
    private static final String MAX_SKILL_TAG = "maxSkill";
    private static final String MIN_SKILL_TAG = "minSkill";
    private static final String PRIORITY_TAG = "priority";
    private static final String PRODUCES_TAG = "produces";
    private static final String PRODUCTION_TAG = "production";
    private static final String UPGRADES_FROM_TAG = "upgradesFrom";
    private static final String UPKEEP_TAG = "upkeep";
    private static final String WORKPLACES_TAG = "workplaces";


    /**
     * {@inheritDoc}
     */
    @Override
        protected void writeAttributes(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeAttributes(xw);

        if (upgradesFrom != null) {
            xw.writeAttribute(UPGRADES_FROM_TAG, upgradesFrom);
        }

        xw.writeAttribute(WORKPLACES_TAG, workPlaces);

        if (minSkill != UNDEFINED) {
            xw.writeAttribute(MIN_SKILL_TAG, minSkill);
        }

        if (maxSkill < INFINITY) {
            xw.writeAttribute(MAX_SKILL_TAG, maxSkill);
        }

        if (upkeep > 0) {
            xw.writeAttribute(UPKEEP_TAG, upkeep);
        }

        if (priority != Consumer.BUILDING_PRIORITY) {
            xw.writeAttribute(PRIORITY_TAG, priority);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeChildren(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeChildren(xw);

        for (ProductionType productionType : productionTypes) {
            productionType.toXML(xw);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        super.readAttributes(xr);

        final Specification spec = getSpecification();

        BuildingType parent = xr.getType(spec, EXTENDS_TAG,
            BuildingType.class, this);

        upgradesFrom = xr.getType(spec, UPGRADES_FROM_TAG,
            BuildingType.class, (BuildingType)null);
        if (upgradesFrom == null) {
            level = 1;
        } else {
            upgradesFrom.upgradesTo = this;
            level = upgradesFrom.level + 1;
        }

        workPlaces = xr.getAttribute(WORKPLACES_TAG, parent.workPlaces);

        minSkill = xr.getAttribute(MIN_SKILL_TAG, parent.minSkill);

        maxSkill = xr.getAttribute(MAX_SKILL_TAG, parent.maxSkill);

        upkeep = xr.getAttribute(UPKEEP_TAG, parent.upkeep);

        priority = xr.getAttribute(PRIORITY_TAG, parent.priority);

        // @compat 0.10.6
        int basicProduction = xr.getAttribute(BASIC_PRODUCTION_TAG, -1);
        if (basicProduction > 0) {
            GoodsType consumes = xr.getType(spec, CONSUMES_TAG, GoodsType.class,
                parent.getConsumedGoodsType());
            GoodsType produces = xr.getType(spec, PRODUCES_TAG, GoodsType.class,
                parent.getProducedGoodsType());
            productionTypes.add(new ProductionType(consumes, produces,
                    basicProduction));
        }
        // end @compat

        if (parent != this) { // Handle "extends" for super-type fields
            if (!xr.hasAttribute(REQUIRED_POPULATION_TAG)) {
                setRequiredPopulation(parent.getRequiredPopulation());
            }

            addFeatures(parent);
            if (parent.isAbstractType()) {
                getFeatureContainer().replaceSource(parent, this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readChild(FreeColXMLReader xr) throws XMLStreamException {
        final Specification spec = getSpecification();
        final String tag = xr.getLocalName();

        if (PRODUCTION_TAG.equals(tag)) {
            if (xr.getAttribute(DELETE_TAG, false)) {
                productionTypes.clear();
                xr.closeTag(PRODUCTION_TAG);

            } else {
                addProductionType(new ProductionType(xr, spec));
            }

        } else {
            super.readChild(xr);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getXMLTagName() { return getXMLElementTagName(); }

    /**
     * Gets the tag name of the root element representing this object.
     *
     * @return "building-type".
     */
    public static String getXMLElementTagName() {
        return "building-type";
    }
}
