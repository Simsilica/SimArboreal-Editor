/*
 * $Id$
 * 
 * Copyright (c) 2014, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
package com.simsilica.arboreal;

import com.jme3.app.Application;
import com.jme3.math.FastMath;
import com.simsilica.arboreal.ui.CheckboxModelGroup;
import com.simsilica.arboreal.ui.PropertyPanel;
import com.simsilica.arboreal.ui.RollupPanel;
import com.simsilica.arboreal.ui.TabbedPanel;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.style.ElementId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  Manages the main TreeParameters objects and the
 *  editor panels for modifying its values.
 *
 *  @author    Paul Speed
 */
public class TreeParametersState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(TreeParametersState.class);
    
    private TreeParameters treeParameters;
    private VersionedHolder<TreeParameters> treeParametersHolder = new VersionedHolder<TreeParameters>();
    
    // Keep track of the panels so that we can easily refresh them
    // when loading new files or otherwise changing parameters
    // outside of the UI.
    private List<PropertyPanel> treePanels;
    
    // Keep the array of version refs for easy tracking
    // of changes across all parameters.       
    private VersionedReference[] versions;
 
    private VersionedReference<Boolean> building;   
    private LevelStats[] levelStats;
    
    public TreeParametersState() {
        this.treeParameters = new TreeParameters(5);
        this.treeParametersHolder.setObject(treeParameters);
    }
 
    public TreeParameters getTreeParameters() {
        return treeParameters;
    }
 
    public VersionedReference<TreeParameters> getTreeParametersRef() {
        return treeParametersHolder.createReference();
    }
 
    @Override
    protected void initialize( Application app ) {

        List<VersionedReference> versionsList = new ArrayList<VersionedReference>();
    
        TabbedPanel tabs = getState(TreeOptionsState.class).getParameterTabs();
                       
        treePanels = new ArrayList<PropertyPanel>();                
        PropertyPanel properties;
 
        // Biggest real life tree is 25 feet in diameter... or 7.62 meters.  We'll
        // let things get more ridiculous
        properties = new PropertyPanel("glass");
        treePanels.add(properties);
        versionsList.add(properties.createReference());
        tabs.addTab("Tree", properties);
        
        properties.addIntProperty("Seed", treeParameters, "seed", 0, 100, 1);
        properties.addFloatProperty("Radius (m)", treeParameters, "trunkRadius", 0.05f, 10f, 0.05f);
        properties.addFloatProperty("Height (m)", treeParameters, "trunkHeight", 0.1f, 50f, 0.1f);
        properties.addFloatProperty("Root Height (m)", treeParameters, "rootHeight", 0f, 10f, 0.1f);
        properties.addFloatProperty("Y Offset (m)", treeParameters, "YOffset", 0f, 10f, 0.1f);
        properties.addIntProperty("Texture U Repeat", treeParameters, "textureURepeat", 1, 12, 1);
        properties.addFloatProperty("Texture V Scale", treeParameters, "textureVScale", 0.01f, 10f, 0.1f);
        
        Container branchPanels = new Container("glass");
        tabs.addTab("Branches", branchPanels);
        
        // Now the sub-panels for each branch
        RollupPanel first = null;
        CheckboxModelGroup rollupGroup = new CheckboxModelGroup();
        for( int i = 0; i < treeParameters.getDepth(); i++ ) {
            BranchParameters branch = treeParameters.getBranch(i);
            
            Container nested = new Container("glass");            
            properties = new PropertyPanel(new ElementId("nestedProperties"), "glass");
            nested.addChild(properties);
            
            treePanels.add(properties);
            versionsList.add(properties.createReference());
            RollupPanel rollup = branchPanels.addChild(new RollupPanel("Level " + i, nested, "glass"));                     
            rollup.setOpenModel(rollupGroup.addChild(rollup.getOpenModel()));
            if( i > 0 ) {
                // The first panel cannot be disabled so we only
                // apply the enabled/disabled checkbox to the others    
                properties.setEnabledProperty(branch, "enabled");
                rollup.getTitleContainer().addChild(new Checkbox("", properties.getEnabledModel(), "glass"));
                properties.addBooleanField("Inherit", branch, "inherit");
                rollup.setOpen(false);
            } else {
                first = rollup; 
            }  
            
            properties.addIntField("Radial Segments", branch, "radialSegments", 3, 24, 1);
            properties.addIntField("Length Segments", branch, "lengthSegments", 1, 10, 1);
            properties.addFloatField("Segment Variation (*)", branch, "segmentVariation", 0, 1, 0.01f);
            properties.addFloatField("Taper (*)", branch, "taper", 0.1f, 1f, 0.01f);           
            properties.addFloatField("Twist (rads)", branch, "twist", 0f, FastMath.PI, 0.01f);                                   
            properties.addFloatField("Gravity (*)", branch, "gravity", -1f, 1f, 0.1f);           
 
 
            properties.addIntField("Side Joints", branch, "sideJointCount", 0, 8, 1);
 
            properties = new PropertyPanel(new ElementId("nestedProperties"), "glass");
            nested.addChild(properties);
            treePanels.add(properties);
            versionsList.add(properties.createReference());
            
            properties.addFloatField("Angle (rads)", branch, "sideJointStartAngle", 0, FastMath.PI, 0.01f);
            properties.addFloatField("Inclination (rads)", branch, "inclination", 0f, FastMath.HALF_PI, 0.01f);           
            properties.addFloatField("Radius Scale (*)", branch, "radiusScale", 0.01f, 2f, 0.01f);
            properties.addFloatField("Length Scale (*)", branch, "lengthScale", 0.01f, 5f, 0.01f);
                                  
            properties.addBooleanField("Tip Joint", branch, "hasEndJoint");
            properties.addFloatField("Tip Rotation (rads)", branch, "tipRotation", 0f, FastMath.PI, 0.01f);
        }
        first.setOpen(true);
        
        
        Container rootPanels = new Container("glass");
        tabs.addTab("Roots", rootPanels);
        
        // Now the sub-panels for each root
        first = null;
        rollupGroup = new CheckboxModelGroup();
        for( int i = 0; i < treeParameters.getDepth(); i++ ) {
            BranchParameters branch = treeParameters.getRoot(i);
            
            Container nested = new Container("glass");            
            properties = new PropertyPanel(new ElementId("nestedProperties"), "glass");
            nested.addChild(properties);
            
            treePanels.add(properties);
            versionsList.add(properties.createReference());
            RollupPanel rollup = rootPanels.addChild(new RollupPanel("Level " + i, nested, "glass"));                     
            rollup.setOpenModel(rollupGroup.addChild(rollup.getOpenModel()));
            if( i > 0 ) {
                // The first panel cannot be disabled so we only
                // apply the enabled/disabled checkbox to the others    
                properties.setEnabledProperty(branch, "enabled");
                rollup.getTitleContainer().addChild(new Checkbox("", properties.getEnabledModel(), "glass"));
                properties.addBooleanField("Inherit", branch, "inherit");
                rollup.setOpen(false);
            } else {
                first = rollup; 
            }  
            
            properties.addIntField("Radial Segments", branch, "radialSegments", 3, 24, 1);
            properties.addIntField("Length Segments", branch, "lengthSegments", 1, 10, 1);
            properties.addFloatField("Segment Variation (*)", branch, "segmentVariation", 0, 1, 0.01f);
            properties.addFloatField("Taper (*)", branch, "taper", 0.1f, 1f, 0.01f);           
            properties.addFloatField("Twist (rads)", branch, "twist", 0f, FastMath.PI, 0.01f);                                   
            properties.addFloatField("Gravity (*)", branch, "gravity", -1f, 1f, 0.1f);           
 
            properties.addIntField("Side Joints", branch, "sideJointCount", 0, 8, 1);
 
            properties = new PropertyPanel(new ElementId("nestedProperties"), "glass");
            nested.addChild(properties);
            treePanels.add(properties);
            versionsList.add(properties.createReference());
            
            properties.addFloatField("Angle (rads)", branch, "sideJointStartAngle", 0, FastMath.PI, 0.01f);
            properties.addFloatField("Inclination (rads)", branch, "inclination", 0f, FastMath.HALF_PI, 0.01f);           
            properties.addFloatField("Radius Scale (*)", branch, "radiusScale", 0.01f, 2f, 0.01f);
            properties.addFloatField("Length Scale (*)", branch, "lengthScale", 0.01f, 4f, 0.01f);
                                  
            properties.addBooleanField("Tip Joint", branch, "hasEndJoint");
            properties.addFloatField("Tip Rotation (rads)", branch, "tipRotation", 0f, FastMath.PI, 0.01f);
        }
        first.setOpen(true);
        
        // And now the leaves panel
        properties = new PropertyPanel("glass");
        treePanels.add(properties);
        versionsList.add(properties.createReference());
        tabs.addTab("Leaves", properties);
        properties.addBooleanProperty("Enabled", treeParameters, "generateLeaves");
        properties.addFloatProperty("Size (m)", treeParameters, "leafScale", 0.1f, 10f, 0.1f);
        
 
 
        // LOD tab
        Container lodPanels = new Container("glass");
        tabs.addTab("LOD", lodPanels);
                       
        levelStats = new LevelStats[treeParameters.getLodCount()];
         
        first = null;
        rollupGroup = new CheckboxModelGroup();
        for( int i = 0; i < treeParameters.getLodCount(); i++ ) {
            LevelOfDetailParameters lod = treeParameters.getLod(i);
        
            String name = (i == 0) ? "Highest" : ("Level " + i);
            
            Container nested = new Container(new BorderLayout());
            levelStats[i] = nested.addChild(new LevelStats(i), BorderLayout.Position.South);
            properties = new PropertyPanel("glass");
            nested.addChild(properties, BorderLayout.Position.Center);
            treePanels.add(properties);
            versionsList.add(properties.createReference());
            RollupPanel rollup = lodPanels.addChild(new RollupPanel(name, nested, "glass"));
            rollup.setOpenModel(rollupGroup.addChild(rollup.getOpenModel()));
            if( i == 0 ) {
                first = rollup;
            } else {
                rollup.setOpen(false);
            }
            
            properties.addFloatField("Distance (m)", lod, "distance", 0, 1000, 1);
            properties.addIntField("Branch Depth", lod, "branchDepth", 1, treeParameters.getDepth(), 1); 
            properties.addIntField("Root Depth", lod, "rootDepth", 1, treeParameters.getDepth(), 1); 
            properties.addIntField("Max Radial Segments", lod, "maxRadialSegments", 3, 24, 1);
            properties.addEnumField("Mesh Type", lod, "reduction"); 
        }        
        first.setOpen(true);
 
 
        versions = new VersionedReference[versionsList.size()];
        versions = versionsList.toArray(versions);        
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
    }
 
    float nextUpdateCheck = 0.1f;
    @Override
    public void update( float tpf ) {
    
        nextUpdateCheck += tpf;
        if( nextUpdateCheck > 0.1f ) {
            boolean changed = false;
            for( VersionedReference ref : versions ) {
                if( ref.update() ) {
                    changed = true;
                }
            }            
            if( changed ) {
                treeParametersHolder.incrementVersion();
            }
        }
        
        // See if we need to update the LOD stats
        if( building == null ) {
            // We haven't retrieved it yet.  We get attached before
            // ForedGridState so we have to grab this lazily.
            building = getState(ForestGridState.class).getBuildingRef();
            refreshStats();
        } else if( building.update() ) {
            refreshStats();
        }        
    }
    
    @Override
    protected void disable() {
    }
 
    public void refreshTreePanels() {
        for( PropertyPanel p : treePanels ) {
            p.refresh();
        }
    }
    
    protected void refreshStats() {
        boolean ready = !building.get();
        for( LevelStats stats : levelStats ) {
            stats.update(ready);
        }
    }   
    
    private class LevelStats extends Container {
        
        int level;
        Label verts;
        Label tris;
        
        public LevelStats( int level ) {
            super(new SpringGridLayout(Axis.X, Axis.Y, FillMode.Even, FillMode.Even));
            this.level = level;
            this.verts = addChild(new Label("LOD verts: ???", "glass"));
            this.tris = addChild(new Label("tris: ???", "glass"));
        }
        
        protected void update( boolean ready ) {
            if( ready ) {
                TreeBuilderReference tree = getState(ForestGridState.class).getMainTree();
                verts.setText("LOD verts: " + tree.getVertexCount(level));
                tris.setText("tris: " + tree.getTriangleCount(level));
            } else {
                verts.setText("LOD verts: ???");
                tris.setText("tris: ???");
            }
        }
    }
}

