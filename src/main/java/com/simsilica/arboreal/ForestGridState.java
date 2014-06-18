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

import com.simsilica.builder.BuilderState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector4f;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.props.PropertyPanel;
import com.simsilica.lemur.style.ElementId;


/**
 *  Manages a ForestGrid instance as well as hooking up
 *  the UI and materials necessary for it to work.
 *  
 *  @author    Paul Speed
 */
public class ForestGridState extends BaseAppState {

    private ForestGrid forestGrid;
    private TreeBuilderReference mainTree;
 
    private Label vertsLabel;
    private Label trisLabel;
    private VersionedHolder<Boolean> building = new VersionedHolder<Boolean>(true);
 
    private VersionedReference<TreeParameters> treeParameters;
    private VersionedReference<PropertyPanel> gridParameters;
    
    private Texture bark;
    private Texture barkNormals;
    private Texture barkBumps;
    private Texture leafAtlas;
    private Texture testPattern;
    private Texture noise;
    
    private Material treeMaterial;
    private Material wireMaterial;
    private Material flatMaterial;
    private Material impostorMaterial;
    private Material leafMaterial;

    private boolean showTestPattern = false;
    private boolean showTrunkBumps = true;
    private boolean useWind = false;

    private Vector4f windCurve = new Vector4f();

    public ForestGridState() {
    }
 
    public VersionedReference<Boolean> getBuildingRef() {
        return building.createReference();
    }
 
    public TreeBuilderReference getMainTree() {
        return mainTree;
    }
 
    public Node getMainTreeNode() {
        return mainTree.getTreeNode();
    }
    
    public void rebuild() {
        building.setObject(true);
        forestGrid.markChanged();
        forestGrid.rebuild();
    }
 
    public void setShowTestPattern( boolean b ) {
        if( this.showTestPattern == b ) {
            return;
        }
        this.showTestPattern = b;
        if( showTestPattern ) {
            treeMaterial.setTexture("DiffuseMap", testPattern);         
            flatMaterial.setTexture("DiffuseMap", testPattern);         
        } else {
            treeMaterial.setTexture("DiffuseMap", bark);         
            flatMaterial.setTexture("DiffuseMap", bark);         
        }
    }
 
    public boolean getShowTestPattern() {
        return showTestPattern;
    }

    public void setShowTrunkBumps( boolean b ) {
        if( this.showTrunkBumps == b ) {
            return;
        }
        this.showTrunkBumps = b;
        if( showTrunkBumps ) {
            treeMaterial.setTexture("NormalMap", barkNormals);
            treeMaterial.setTexture("ParallaxMap", barkBumps);
        } else {
            treeMaterial.clearParam("NormalMap");
            treeMaterial.clearParam("ParallaxMap");
        }
    }

    public boolean getShowTrunkBumps() {
        return showTrunkBumps;
    }
    
    public void setUseWind( boolean b ) {
        if( this.useWind == b ) {
            return;
        }
        this.useWind = b;
        treeMaterial.setBoolean("UseWind", useWind);
        leafMaterial.setBoolean("UseWind", useWind);
        flatMaterial.setBoolean("UseWind", useWind);
        impostorMaterial.setBoolean("UseWind", useWind);
    }
    
    public boolean getUseWind() {
        return useWind;
    }
    
    @Override
    protected void initialize( Application app ) {
 
        AssetManager assets = app.getAssetManager();
           
        bark = assets.loadTexture("Textures/bark128.jpg");
        bark.setWrap(Texture.WrapMode.Repeat);
        barkNormals = assets.loadTexture("Textures/bark128-norm.jpg");
        barkNormals.setWrap(Texture.WrapMode.Repeat);
        barkBumps = assets.loadTexture("Textures/bark128-bump.png");
        barkBumps.setWrap(Texture.WrapMode.Repeat); 
                                     
        testPattern = assets.loadTexture("Textures/test-pattern.png");
        testPattern.setWrap(Texture.WrapMode.Repeat);            

        leafAtlas = assets.loadTexture("Textures/leaf-atlas.png");
        leafAtlas.setWrap(Texture.WrapMode.Repeat);
        
        noise = assets.loadTexture("Textures/noise-x3-512.png");                        
        noise.setWrap(Texture.WrapMode.Repeat);
 
        treeParameters = getState(TreeParametersState.class).getTreeParametersRef();
        
        forestGrid = new ForestGrid(treeParameters.get(), 
                                getTreeMaterial(),
                                getWireMaterial(),
                                getLeafMaterial(),
                                getFlatMaterial(),
                                getImpostorMaterial(),
                                getState(BuilderState.class).getBuilder());
 
        mainTree = forestGrid.getTree(0, 0);
        
        
        // Add some options check boxes for rendering
        TreeOptionsState options = getState(TreeOptionsState.class);
        options.addOptionToggle("Wireframe", forestGrid, "setShowWireframe");
        options.addOptionToggle("Test Pattern", this, "setShowTestPattern");
        Checkbox cb = options.addOptionToggle("Bump-map", this, "setShowTrunkBumps");
        cb.setChecked(true);
 
        options.addOptionToggle("Wind", this, "setUseWind");               
        
        PropertyPanel properties = new PropertyPanel("glass");
        gridParameters = properties.createReference();
        options.getParameterTabs().addTab("Grid", properties);
        
        properties.addIntProperty("Width", forestGrid, "width", 1, 10, 1);
        properties.addIntProperty("Height", forestGrid, "height", 1, 10, 1);
        properties.addFloatProperty("Spacing (m)", forestGrid, "spacing", 0.3f, 40, 0.1f);
        properties.addIntProperty("Seed Range", forestGrid, "seedRange", 1, 100, 1);
        properties.addFloatProperty("Rotation Variation (*)", forestGrid, "rotationVariation", 0, 1, 0.01f);
        properties.addFloatProperty("Lean Variation (*)", forestGrid, "leanVariation", 0, 1, 0.01f);
        properties.addFloatProperty("Scale Variation (*)", forestGrid, "scaleVariation", 0, 1, 0.01f);
        properties.addFloatProperty("Position Variation (*)", forestGrid, "positionVariation", 0, 1, 0.01f);
        
        
        // Add a stats panel to the bottom... could have done it as another
        // state but we already manage all of the geometry here
        Container stats = options.getContents().addChild(new Container(new SpringGridLayout(), new ElementId("stats"), "glass"));
        vertsLabel = stats.addChild(new Label("verts:", "glass"));
        trisLabel = stats.addChild(new Label("tris:", "glass"), 1);
                        
    }    

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        Node rootNode = ((SimpleApplication)getApplication()).getRootNode();
        rootNode.attachChild(forestGrid.getRootNode());                                           
    }

    private float nextUpdateCheck = 0.1f;
    private float time;
    @Override
    public void update( float tpf ) {


        // Calculate the wind curves
        time += tpf;
        windCurve.x = time;         
    
        nextUpdateCheck += tpf;
        if( nextUpdateCheck <= 0.1f ) {
            return;
        }
        nextUpdateCheck = 0;
        
        boolean changed = treeParameters.update();
        if( gridParameters.update() ) {
            changed = true;
        }
         
        if( changed ) {
            building.setObject(true);
            forestGrid.markChanged();
            forestGrid.rebuild();
            refreshStats();
            refreshWindParms();
        }
        
        if( building.getObject() && getState(BuilderState.class).getBuilder().getPending() == 0 ) {
            building.setObject(false);
            refreshStats();
        }
    }

    @Override
    protected void disable() {
        forestGrid.getRootNode().removeFromParent();                                           
    }
    
    protected void refreshStats() {
        if( building.getObject() ) {
            vertsLabel.setText("verts: ???");
            trisLabel.setText("tris: ???");
        } else {
            vertsLabel.setText("verts: " + mainTree.getVertexCount());
            trisLabel.setText("tris: " + mainTree.getTriangleCount());
        }        
    }
    
    protected void refreshWindParms() {
        if( treeMaterial == null ) {
            // too soon
            return;
        }
        TreeParameters tp = treeParameters.get();
 
        treeMaterial.setFloat("FlexHeight", tp.getFlexHeight());
        treeMaterial.setFloat("TrunkFlexibility", tp.getTrunkFlexibility());
        treeMaterial.setFloat("BranchFlexibility", tp.getBranchFlexibility());

        leafMaterial.setFloat("FlexHeight", tp.getFlexHeight());
        leafMaterial.setFloat("TrunkFlexibility", tp.getTrunkFlexibility());
        leafMaterial.setFloat("BranchFlexibility", tp.getBranchFlexibility());
 
        flatMaterial.setFloat("FlexHeight", tp.getFlexHeight());
        flatMaterial.setFloat("TrunkFlexibility", tp.getTrunkFlexibility());
        flatMaterial.setFloat("BranchFlexibility", tp.getBranchFlexibility());
 
        impostorMaterial.setFloat("TrunkFlexibility", tp.getTrunkFlexibility());
       
    }
    
    public Material getTreeMaterial() {
        if( treeMaterial != null ) {
            return treeMaterial;
        }
           
        treeMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.Yellow, true).getMaterial();
        treeMaterial = new Material(getApplication().getAssetManager(), "MatDefs/TreeLighting.j3md");
        treeMaterial.setColor("Diffuse", ColorRGBA.White);
        treeMaterial.setColor("Ambient", ColorRGBA.White);
        treeMaterial.setBoolean("UseMaterialColors", true);
        treeMaterial.setBoolean("UseWind", false);
        treeMaterial.setTexture("WindNoise", noise);
        treeMaterial.setTexture("DiffuseMap", bark);            
        treeMaterial.setTexture("NormalMap", barkNormals);
        treeMaterial.setTexture("ParallaxMap", barkBumps);                    
        //treeMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        return treeMaterial;
    }
 
    public Material getFlatMaterial() {
        if( flatMaterial != null ) {
            return flatMaterial;
        }
        
        flatMaterial = new Material(getApplication().getAssetManager(), "MatDefs/AxisBillboardLighting.j3md");
        flatMaterial.setColor("Diffuse", ColorRGBA.White);
        flatMaterial.setColor("Ambient", ColorRGBA.White);
        flatMaterial.setBoolean("UseMaterialColors", true);
        flatMaterial.setTexture("DiffuseMap", bark);            
        flatMaterial.setBoolean("UseWind", false);
        flatMaterial.setTexture("WindNoise", noise);
        flatMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        return flatMaterial;
    }

    public Material getImpostorMaterial() {
        if( impostorMaterial != null ) {
            return impostorMaterial;
        }
        
        impostorMaterial = new Material(getApplication().getAssetManager(), "MatDefs/IndexedBillboardLighting.j3md");
        impostorMaterial.setColor("Diffuse", ColorRGBA.White);
        impostorMaterial.setColor("Ambient", ColorRGBA.White);
        impostorMaterial.setBoolean("UseMaterialColors", true);
        impostorMaterial.setFloat("AlphaDiscardThreshold", 0.5f);

        impostorMaterial.setBoolean("UseWind", false);
        impostorMaterial.setTexture("WindNoise", noise);
        
        impostorMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        impostorMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        return impostorMaterial;
    }
    
    public Material getWireMaterial() {
        if( wireMaterial != null ) {
            return wireMaterial;
        }
        Material mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Yellow, false).getMaterial();
        wireMaterial = mat;
        mat.getAdditionalRenderState().setWireframe(true);
        return mat;
    }
    
    public Material getLeafMaterial() {
        if( leafMaterial != null ) {
            return leafMaterial;
        }
        
        AssetManager assets = getApplication().getAssetManager();
        leafMaterial = new Material(assets, "MatDefs/LeafLighting.j3md");
        leafMaterial.setColor("Diffuse", ColorRGBA.White);
        leafMaterial.setColor("Ambient", ColorRGBA.White);
        leafMaterial.setBoolean("UseMaterialColors", true);
        leafMaterial.setTexture("DiffuseMap", leafAtlas);         

        leafMaterial.setBoolean("UseWind", false);
        leafMaterial.setTexture("WindNoise", noise);
            
        leafMaterial.setFloat("AlphaDiscardThreshold", 0.5f);         
        leafMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        return leafMaterial;
    }
 
}


