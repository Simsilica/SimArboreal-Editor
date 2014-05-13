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
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.BaseAppState;


/**
 *  Shows some sample people for scale.
 *
 *  @author    Paul Speed
 */
public class AvatarState extends BaseAppState {

    private Node avatars;
    private Spatial male;
    private Spatial female;
    
    public AvatarState() {
    }

    @Override
    protected void initialize( Application app ) {

        AssetManager assets = app.getAssetManager();

        // Add an avatar for scale
        avatars = new Node("Avatars");
        avatars.move(2, 0, 0);
        
        female = (Node)assets.loadModel("Models/female-parts.j3o");
        BoundingBox bb = (BoundingBox)female.getWorldBound();
        float height = bb.getYExtent() * 2;
        float femaleScale = 1.62f / height;
        female.move(0, bb.getYExtent(), 0);
        female.setLocalScale(femaleScale);
        Material mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Gray, true).getMaterial();
        mat.setColor("Ambient", ColorRGBA.Gray);
        female.setMaterial(mat);
        female.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);        
        avatars.attachChild(female);

        // Add an avatar for scale
        male = (Node)assets.loadModel("Models/male-parts-no-bones.j3o");
        bb = (BoundingBox)male.getWorldBound();
        height = bb.getYExtent() * 2;
        float maleScale = 1.77f / height;
        male.move(bb.getCenter().negate());         
        male.move(1, bb.getYExtent(), 0);
        male.setLocalScale(maleScale);
        male.setMaterial(mat);
        male.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);        
        avatars.attachChild(male);    
    } 

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        Node rootNode = ((SimpleApplication)getApplication()).getRootNode();
        rootNode.attachChild(avatars);
    }

    @Override
    protected void disable() {
        avatars.removeFromParent();
    }
}
