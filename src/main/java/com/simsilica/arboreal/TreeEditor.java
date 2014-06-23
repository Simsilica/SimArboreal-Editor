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
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.simsilica.fx.LightingState;
import com.simsilica.fx.sky.SkyState;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.style.BaseStyles;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Main class for the TreeEditor.
 *
 *  @author PSpeed
 */
public class TreeEditor extends SimpleApplication {

    static Logger log = LoggerFactory.getLogger(TreeEditor.class);

    public static final String GLASS_STYLES = "/com/simsilica/arboreal/ui/glass-styles.groovy";
 
    public static void main( String... args ) {        
        TreeEditor main = new TreeEditor();

        AppSettings settings = new AppSettings(false);
        settings.setTitle("SimArboreal Tree Editor");
        settings.setSettingsDialogImage("/com/simsilica/arboreal/images/TreeEditor-Splash.png");
        
        try {
            BufferedImage[] icons = new BufferedImage[] {
                    ImageIO.read( TreeEditor.class.getResource( "/com/simsilica/arboreal/images/TreeEditor-icon-128.png" ) ),
                    ImageIO.read( TreeEditor.class.getResource( "/com/simsilica/arboreal/images/TreeEditor-icon-32.png" ) ),
                    ImageIO.read( TreeEditor.class.getResource( "/com/simsilica/arboreal/images/TreeEditor-icon-16.png" ) )
                };
            settings.setIcons(icons);
        } catch( IOException e ) {
            log.warn( "Error loading globe icons", e );
        }        
        
        main.setSettings(settings);
        
        main.start();
    }
 
    public TreeEditor() {
        super(new StatsAppState(), new DebugKeysAppState(),
              new BuilderState(1, 1),
              new MovementState(),
              new DebugHudState(),
              new TreeOptionsState(),
              new LightingState(),
              new GroundState(),
              new SkyState(),
              new AvatarState(),
              new TreeParametersState(),
              new ForestGridState(),
              new AtlasGeneratorState(),
              new FileActionsState(),              
              new PostProcessorState(), 
              new ScreenshotAppState("", System.currentTimeMillis())); 
    }

    public void toggleRecordVideo() {
        VideoRecorderAppState recorder = stateManager.getState(VideoRecorderAppState.class);
        if( recorder == null ) {
            recorder = new VideoRecorderAppState(new File("trees-" + System.currentTimeMillis() + ".avi"));
            stateManager.attach(recorder);
        } else {
            stateManager.detach(recorder);
        }
    }
 
    @Override
    public void simpleInitApp() {
    
        GuiGlobals.initialize(this);

        cam.setLocation(new Vector3f(0, 1.8f, 10));

        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        MainFunctions.initializeDefaultMappings(inputMapper);
        inputMapper.activateGroup( MainFunctions.GROUP );        
        MovementFunctions.initializeDefaultMappings(inputMapper);

        inputMapper.addDelegate(MainFunctions.F_RECORD_VIDEO, this, "toggleRecordVideo");

        /*
        // Now create the normal simple test scene    
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", ColorRGBA.Blue);
        mat.setColor("Ambient", ColorRGBA.Green);
        mat.setBoolean("UseMaterialColors", true);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
        */ 

        BaseStyles.loadGlassStyle();

        TreeOptionsState treeOptions = stateManager.getState(TreeOptionsState.class);                
        treeOptions.addOptionToggle("Grass", stateManager.getState(GroundState.class), "setShowGrass");                
        treeOptions.addOptionToggle("Ground Atm.", stateManager.getState(GroundState.class), "setUseScattering");                
        treeOptions.addOptionToggle("Sky", stateManager.getState(SkyState.class), "setEnabled");
        stateManager.getState(SkyState.class).setEnabled(false);
    }    
}
