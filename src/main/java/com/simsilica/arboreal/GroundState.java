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
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.TangentBinormalGenerator;
import com.simsilica.fx.sky.SkyState;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.geom.MBox;


/**
 *
 *  @author    Paul Speed
 */
public class GroundState extends BaseAppState {

    private Material greenMaterial;
    private Material groundMaterial;
    private Geometry ground;

    private boolean showGrass = false;
    private boolean useScattering = false;

    public GroundState() {
    }

    public void setUseScattering( boolean b ) {
        if( this.useScattering == b ) {
            return;
        }
        this.useScattering = b;
        resetScattering();        
    }
    
    public boolean getUseScattering() {
        return useScattering;
    }

    public void setShowGrass( boolean b ) {
        this.showGrass = b;
        resetGrass();
    }
    
    public boolean getShowGrass() {
        return showGrass;
    }

    protected void resetGrass() {
        if( ground == null ) {
            return;
        }
        if( showGrass ) {
            ground.setMaterial(groundMaterial);
        } else {
            ground.setMaterial(greenMaterial);
        }
    }

    protected void resetScattering() {
        if( groundMaterial != null ) {
            groundMaterial.setBoolean("UseScattering", useScattering);
        }
    }
    

    @Override
    protected void initialize( Application app ) {
    
        AssetManager assets = app.getAssetManager();
    
        groundMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.Green, true).getMaterial(); 
        
        MBox b = new MBox(500, 0, 500, 50, 0, 50, MBox.TOP_MASK);
        TangentBinormalGenerator.generate(b);
        b.scaleTextureCoordinates(new Vector2f(1000, 1000));
        ground = new Geometry("Box", b);

        greenMaterial = new Material(assets, "Common/MatDefs/Light/Lighting.j3md");
        greenMaterial.setColor("Diffuse", ColorRGBA.Green);
        greenMaterial.setColor("Ambient", ColorRGBA.Green);
        greenMaterial.setBoolean("UseMaterialColors", true);
        ground.setMaterial(greenMaterial);

        
        groundMaterial = new Material(assets, "MatDefs/MultiResolution.j3md"); 
        groundMaterial.setColor("Diffuse", ColorRGBA.White); 
        groundMaterial.setColor("Specular", ColorRGBA.White); 
        groundMaterial.setColor("Ambient", ColorRGBA.White);
        groundMaterial.setFloat("Shininess", 0);
        groundMaterial.setBoolean("UseMaterialColors", true);

        // Hook up the scattering parameters
        getState(SkyState.class).getAtmosphericParameters().applyGroundParameters(groundMaterial, true);

        Texture texture;
 
        //texture = assets.loadTexture("Textures/test-pattern.png");       
        texture = assets.loadTexture("Textures/grass.jpg");
        texture.setWrap(WrapMode.Repeat);
        groundMaterial.setTexture("DiffuseMap", texture);
        
        texture = assets.loadTexture("Textures/grass-flat.jpg");
        texture.setWrap(WrapMode.Repeat);
        groundMaterial.setTexture("BackgroundDiffuseMap", texture);
 
        texture = assets.loadTexture("Textures/brown-dirt-norm.jpg");
        //texture = assets.loadTexture("Textures/bark128-norm.jpg");
        texture.setWrap(WrapMode.Repeat);
        groundMaterial.setTexture("NormalMap", texture);
       
        texture = assets.loadTexture("Textures/noise-x3-512.png");
        texture.setWrap(WrapMode.Repeat);
        groundMaterial.setTexture("NoiseMap", texture);

        resetGrass();
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        Node rootNode = ((SimpleApplication)getApplication()).getRootNode();
        rootNode.attachChild(ground); 
    }

    @Override
    protected void disable() {
        ground.removeFromParent();
    }
}
