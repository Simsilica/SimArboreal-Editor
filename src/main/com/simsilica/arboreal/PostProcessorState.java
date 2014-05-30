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
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.simsilica.arboreal.ui.PropertyPanel;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;


/**
 *
 *  @author    Paul Speed
 */
public class PostProcessorState extends BaseAppState {

    private FilterPostProcessor fpp;
    private DirectionalLightShadowFilter shadows1;
    private DropShadowFilter shadows2;
    private VersionedReference<Vector3f> lightDir;
    private float shadowStrength = 0.3f;
    
    private Checkbox shadowToggle;
    private Checkbox dropShadowToggle;

    public PostProcessorState() {
    }

    public void setEnableShadows( boolean b ) {
        shadows1.setEnabled(b);
        if( b && shadows2.isEnabled() ) {
            shadows2.setEnabled(false);
            dropShadowToggle.setChecked(false);
        }
    }

    public void setEnableDropShadows( boolean b ) {
        shadows2.setEnabled(b);
        if( b && shadows1.isEnabled() ) {
            shadows1.setEnabled(false);
            shadowToggle.setChecked(false);
        }
    }
    
    public void setShadowStrength( float f ) {
        if( this.shadowStrength == f ) {
            return;
        }
        this.shadowStrength = f;
        resetShadowStrength();
    }
    
    public float getShadowStrength() {
        return shadowStrength;
    }

    protected void resetShadowStrength() {
        shadows1.setShadowIntensity(shadowStrength);
        shadows2.setShadowIntensity(shadowStrength);
    }

    @Override
    protected void initialize( Application app ) {
    
        lightDir = getState(LightingState.class).getLightDirRef();
 
        AssetManager assets = app.getAssetManager();
        
        fpp = new FilterPostProcessor(assets);
        AppSettings settings = app.getContext().getSettings();
        if( settings.getSamples() != 0 )
            {
            fpp.setNumSamples(settings.getSamples());
            }

        shadows2 = new DropShadowFilter();
        shadows2.setEnabled(false);
        fpp.addFilter(shadows2);
        
        shadows1 = new DirectionalLightShadowFilter(assets, 4096, 4);
        shadows1.setShadowIntensity(0.3f);
        shadows1.setLight(getState(LightingState.class).getSun());
        shadows1.setEnabled(false);
        fpp.addFilter(shadows1);

        // Go ahead and add some UI stuff here... normally I'd
        // put it in another state but it doesn't seem worth it.
        TreeOptionsState options = getState(TreeOptionsState.class);
        shadowToggle = options.addOptionToggle("Show Shadows", this, "setEnableShadows");
        dropShadowToggle = options.addOptionToggle("Show Drop Shadows", this, "setEnableDropShadows");
        
        
        // Add some parameters to the main options window
        PropertyPanel properties = new PropertyPanel("glass"); 
        getState(TreeOptionsState.class).getViewSettings().addChild(properties);
        
        properties.addFloatProperty("Time of day (*)", getState(LightingState.class), 
                                    "timeOfDay", 0, 1, 0.05f);
        properties.addFloatProperty("Shadow Strength (*)", this, "shadowStrength", 0, 1, 0.05f);        
 
        resetShadowStrength();                   
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        getApplication().getViewPort().addProcessor(fpp);        
    }

    @Override
    public void update( float tpf ) {
        if( lightDir.update() ) {
        }
    }

    @Override
    protected void disable() {
        getApplication().getViewPort().removeProcessor(fpp);        
    }
}
