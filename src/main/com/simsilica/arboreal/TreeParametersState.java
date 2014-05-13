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
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;
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
    private VersionedHolder<TreeParameters> treeParametersHolder = new VersionedHolder<TreeParameters>(treeParameters);
    
    private TabbedPanel tabs;
    
    // Keep track of the panels so that we can easily refresh them
    // when loading new files or otherwise changing parameters
    // outside of the UI.
    private List<PropertyPanel> treePanels;
    
    // Keep the array of version refs for easy tracking
    // of changes across all parameters.       
    private VersionedReference[] versions;
    
    public TreeParametersState() {
        this.treeParameters = new TreeParameters(5);
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
    
        tabs = new TabbedPanel("glass");
                       
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
        properties.addFloatProperty("Height (m)", treeParameters, "trunkHeight", 0.5f, 50f, 0.1f);
        properties.addFloatProperty("Root Height (m)", treeParameters, "rootHeight", 0f, 10f, 0.1f);
        properties.addIntProperty("Texture U Repeat", treeParameters, "textureURepeat", 1, 12, 1);
        properties.addFloatProperty("Texture V Scale", treeParameters, "textureVScale", 0.01f, 10f, 0.1f);
        
        Container branchPanels = new Container("glass");
        tabs.addTab("Branches", branchPanels);
        
        // Now the sub-panels for each branch
        RollupPanel first = null;
        CheckboxModelGroup rollupGroup = new CheckboxModelGroup();
        for( int i = 0; i < treeParameters.getDepth(); i++ ) {
            BranchParameters branch = treeParameters.getBranch(i);
            
            properties = new PropertyPanel("glass");
            treePanels.add(properties);
            versionsList.add(properties.createReference());
            RollupPanel rollup = branchPanels.addChild(new RollupPanel("Level " + i, properties, "glass"));                     
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
            properties.addFloatField("Angle (rads)", branch, "sideJointStartAngle", 0, FastMath.PI, 0.01f);
            properties.addFloatField("Inclination (rads)", branch, "inclination", 0f, FastMath.HALF_PI, 0.01f);           
            properties.addFloatField("Radius Scale (*)", branch, "radiusScale", 0.01f, 2f, 0.01f);
            properties.addFloatField("Length Scale (*)", branch, "lengthScale", 0.01f, 4f, 0.01f);
                                  
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
            
            properties = new PropertyPanel("glass");
            treePanels.add(properties);
            versionsList.add(properties.createReference());
            RollupPanel rollup = rootPanels.addChild(new RollupPanel("Level " + i, properties, "glass"));                     
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
        
 
        versions = new VersionedReference[versionsList.size()];
        versions = versionsList.toArray(versions);        
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        getState(TreeOptionsState.class).getContents().addChild(tabs);
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
    }
    
    @Override
    protected void disable() {
        getState(TreeOptionsState.class).getContents().removeChild(tabs);
    }
    
}
 /*
 
        properties = new PropertyPanel("Grid", "glass");
        versionsList.add(properties.createReference());
        tabs.addTab("Grid", properties);
        
        properties.addIntProperty("Width", forest, "width", 1, 10, 1);
        properties.addIntProperty("Height", forest, "height", 1, 10, 1);
        properties.addFloatProperty("Spacing (m)", forest, "spacing", 0.3f, 40, 0.1f);
        properties.addIntProperty("Seed Range", forest, "seedRange", 1, 100, 1);
        properties.addFloatProperty("Rotation Variation (*)", forest, "rotationVariation", 0, 1, 0.01f);
        properties.addFloatProperty("Lean Variation (*)", forest, "leanVariation", 0, 1, 0.01f);
        properties.addFloatProperty("Scale Variation (*)", forest, "scaleVariation", 0, 1, 0.01f);
        properties.addFloatProperty("Position Variation (*)", forest, "positionVariation", 0, 1, 0.01f);
        
        versions = new VersionedReference[versionsList.size()];
        versions = versionsList.toArray(versions);
*/
