/*
 * ${Id}
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
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;


/**
 *  Manages a ForestGrid instance as well as hooking up
 *  the UI and materials necessary for it to work.
 *  
 *  @author    Paul Speed
 */
public class ForestGridState extends BaseAppState {

    private ForestGrid forestGrid;
    private TreeBuilderReference mainTree;
 
    private VersionedReference<TreeParameters> treeParameters;
    
    private Texture bark;
    private Texture barkNormals;
    private Texture barkBumps;
    private Texture leafAtlas;
    private Texture testPattern;
    
    private Material treeMaterial;
    private Material wireMaterial;
    private Material leafMaterial;

    public ForestGridState() {
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
 
        treeParameters = getState(TreeParametersState.class).getTreeParametersRef();
        
        forestGrid = new ForestGrid(treeParameters.get(), 
                                createTreeMaterial(),
                                createWireMaterial(),
                                createLeafMaterial(),
                                getState(BuilderState.class).getBuilder());
 
        mainTree = forestGrid.getTree(0, 0);   
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
    @Override
    public void update( float tpf ) {
    
        nextUpdateCheck += tpf;
        if( nextUpdateCheck <= 0.1f ) {
            return;
        }
        nextUpdateCheck = 0;
        if( treeParameters.update() ) {
            forestGrid.markChanged();
            forestGrid.rebuild();
        }
    }

    @Override
    protected void disable() {
        forestGrid.getRootNode().removeFromParent();                                           
    }
    
    protected Material createTreeMaterial() {
        if( treeMaterial != null ) {
            return treeMaterial;
        }
           
        treeMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.Yellow, true).getMaterial();
        treeMaterial.setColor("Diffuse", ColorRGBA.White);
        treeMaterial.setColor("Ambient", ColorRGBA.White);
        treeMaterial.setBoolean("UseMaterialColors", true);
        treeMaterial.setTexture("DiffuseMap", bark);            
        treeMaterial.setTexture("NormalMap", barkNormals);
        treeMaterial.setTexture("ParallaxMap", barkBumps);                    
        treeMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        return treeMaterial;
    }
    
    protected Material createWireMaterial() {
        Material mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Yellow, false).getMaterial();
        mat.getAdditionalRenderState().setWireframe(true);
        return mat;
    }
    
    protected Material createLeafMaterial() {
        if( leafMaterial != null ) {
            return leafMaterial;
        }
        
        //AssetManager assets = getApplication().getAssetManager();
        //leafMaterial = new Material(assets, "MatDefs/LeafLighting.j3md");
        leafMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.Yellow, true).getMaterial();             
        leafMaterial.setColor("Diffuse", ColorRGBA.White);
        leafMaterial.setColor("Ambient", ColorRGBA.White);
        leafMaterial.setBoolean("UseMaterialColors", true);
        leafMaterial.setTexture("DiffuseMap", leafAtlas);         
            
        leafMaterial.setFloat("AlphaDiscardThreshold", 0.5f);         
        leafMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        return leafMaterial;
    }
 
}


