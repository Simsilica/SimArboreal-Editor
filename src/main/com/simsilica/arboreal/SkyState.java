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
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Sphere;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;


/**
 *
 *  @author    Paul Speed
 */
public class SkyState extends BaseAppState {

    private ColorRGBA skyColor;
    private ColorRGBA sunColor;
    private Geometry sky;
    private Geometry sun;
    private boolean showSky;
    
    private VersionedReference<Vector3f> lightDir;

    public SkyState() {
        this.sunColor = new ColorRGBA(1, 1, 0.9f, 1);
        this.skyColor = new ColorRGBA(0.5f, 0.5f, 1f, 1);
    }

    public void setShowSky( boolean b ) {
        this.showSky = b;
        resetShowSky();
    }
    
    protected void resetShowSky() {
        if( sky == null ) {
            return;
        }
        if( showSky ) {
            sky.setCullHint(CullHint.Inherit);
        } else {
            sky.setCullHint(CullHint.Always);
        }
    }

    public boolean getShowSky() {
        return sky.getCullHint() == CullHint.Inherit; 
    }

    @Override
    protected void initialize( Application app ) {
    
        lightDir = getState(LightingState.class).getLightDirRef();
    
 
        // Add a sun sphere
        Sphere orb = new Sphere(6, 12, 50);
        sun = new Geometry("Sun", orb);
        sun.setMaterial(GuiGlobals.getInstance().createMaterial(sunColor, false).getMaterial());
        sun.move(lightDir.get().mult(-900));


        // Add a sky sphere
        orb = new Sphere(6, 12, 800, true, true);
        sky = new Geometry("Sky", orb);
        sky.setMaterial(GuiGlobals.getInstance().createMaterial(skyColor, false).getMaterial());
        sky.setQueueBucket(Bucket.Sky);
        sky.setIgnoreTransform(true);
        
        resetShowSky();
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        Node rootNode = ((SimpleApplication)getApplication()).getRootNode();
        rootNode.attachChild(sun);
        rootNode.attachChild(sky);
    }

    @Override
    public void update( float tpf ) {
        if( lightDir.update() ) {
            sun.setLocalTranslation(lightDir.get().mult(-900));
        }
    }

    @Override
    protected void disable() {
        sun.removeFromParent();
        sky.removeFromParent();
    }
}
