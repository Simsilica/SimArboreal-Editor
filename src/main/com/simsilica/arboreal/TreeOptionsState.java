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
import com.jme3.app.SimpleApplication;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.style.ElementId;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *  @author    Paul Speed
 */
public class TreeOptionsState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(TreeOptionsState.class);
    
    private Container mainWindow;
    private Container mainContents; 
 
    private Container actionsPanel;
    private Container filePanel;
    private Container checkboxPanel;
 
    private List<Checkbox> optionToggles = new ArrayList<Checkbox>();
    private Map<String, Checkbox> optionToggleMap = new HashMap<String, Checkbox>();
    
    public TreeOptionsState() {
    }
 
    public Checkbox addOptionToggle( String name, Object target, String method ) {
        Checkbox cb = new Checkbox(name, "glass");
        cb.addClickCommands(new ToggleHandler(target, method));
                        
        int column = optionToggles.size() % 2;
        if( checkboxPanel != null ) {
            checkboxPanel.addChild(cb, column);
        }
        optionToggles.add(cb);
        optionToggleMap.put(name, cb);
        return cb;        
    }
    
    public void toggleHud() {
        setEnabled( !isEnabled() );
    }

    @Override
    protected void initialize( Application app ) {
    
        // Always register for our hot key as long as
        // we are attached.
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addDelegate( MainFunctions.F_HUD, this, "toggleHud" );
                
        mainWindow = new Container(new BorderLayout(), new ElementId("window"), "glass");
        mainWindow.addChild(new Label("Tree Options", mainWindow.getElementId().child("title.label"), "glass"),
                           BorderLayout.Position.North); 
        mainWindow.setLocalTranslation(10, app.getCamera().getHeight() - 10, 0);        
        
        mainContents = mainWindow.addChild(new Container(mainWindow.getElementId().child("contents.container"), "glass"),
                                                        BorderLayout.Position.Center); 
               
        actionsPanel = mainContents.addChild(new Container());
        filePanel = mainContents.addChild(new Container());
        checkboxPanel = mainContents.addChild(new Container());
        
        // Add any toggles that were added before init
        int i = 0;
        for( Checkbox cb : optionToggles ) {
            int column = (i++) % 2;
            checkboxPanel.addChild(cb, column);
        }                     
    }

    @Override
    protected void cleanup( Application app ) {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate( MainFunctions.F_HUD, this, "toggleHud" ); 
    }

    @Override
    protected void enable() {
        ((SimpleApplication)getApplication()).getGuiNode().attachChild(mainWindow);
    }
    
    @Override
    protected void disable() {
        mainWindow.removeFromParent();
    }
    
    private class ToggleHandler implements Command<Button> {
        private Object object;
        private Method method;
        
        public ToggleHandler( Object object, String methodName ) {
            this.object = object;
            try {
                this.method = object.getClass().getMethod(methodName, Boolean.TYPE);
            } catch( Exception e ) {
                throw new RuntimeException("Error retrieving method for:" + methodName, e);
            }
        }
        
        @Override
        public void execute( Button source ) {
            try {
                method.invoke(object, ((Checkbox)source).isChecked());
            } catch( Exception e ) {
                throw new RuntimeException("Error sending state for:" + object + "->" + method, e);
            }
        } 
    }
         /*
        Button saveParms = buttons.addChild(new Button("Save Parms", "glass"));
        saveParms.addClickCommands(new SaveTreeParameters());
        saveParms.setTextHAlignment(HAlignment.Center);

        Button loadParms = buttons.addChild(new Button("Load Parms", "glass"), 1);
        loadParms.addClickCommands(new LoadTreeParameters());
        loadParms.setTextHAlignment(HAlignment.Center);
 
        Button saveJ3o = buttons.addChild(new Button("Export j3o", "glass"), 2);
        saveJ3o.addClickCommands(new SaveJ3o());
        saveJ3o.setTextHAlignment(HAlignment.Center);

        
        //needsRegenerate = options.addChild(new Label(" ", "glass"), 1);        

        //RollupPanel test = options.addChild(new RollupPanel("Checks", "glass"));         
 
        Container checks = options.addChild(new Container());
        //Container checks = new Container();
        //test.setContents(checks);
 
        Checkbox wire = checks.addChild(new Checkbox("Show Wireframe", "glass"));
        wire.addClickCommands(new SetWireframe());
                
        Checkbox showTP = checks.addChild(new Checkbox("Show Test Pattern", "glass"), 1);
        showTP.addClickCommands(new SetTestPattern());        
           */
}
