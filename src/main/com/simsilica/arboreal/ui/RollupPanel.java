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
 
package com.simsilica.arboreal.ui;


import com.simsilica.lemur.*;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;


/**
 *
 *  @author    Paul Speed
 */
public class RollupPanel extends Panel {

    private BorderLayout layout;
    private Container titleContainer;
    private Button title;
    private Panel contents;
    private CheckboxModel openModel = new OpenCheckboxModel(true);
    private VersionedReference<Boolean> openRef = openModel.createReference();    

    public RollupPanel( String title, String style ) {
        this(title, null, true, new ElementId("rollup"), style);
    }

    public RollupPanel( String title, Panel contents, String style ) {
        this(title, contents, true, new ElementId("rollup"), style);
    }
    
    protected RollupPanel( String titleString, Panel contents, 
                           boolean applyStyles, ElementId elementId, String style ) {
        super(false, elementId, style);

        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);
 
        this.contents = contents;
        if( contents != null ) {
            layout.addChild(contents,  BorderLayout.Position.Center);
        }

        this.titleContainer = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.First, FillMode.Even),
                                       elementId.child("titlebar"), style);
        layout.addChild(titleContainer, BorderLayout.Position.North);
        this.title = new Button(titleString, elementId.child("title"), style);
        titleContainer.addChild(title);
        title.addClickCommands(new ToggleOpenCommand());       
 
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId, style);
        }
    
        resetOpen();
    }
    
    public void setContents( Panel p ) {
        if( this.contents == p ) {
            return;
        }
        if( this.contents != null ) {
            layout.removeChild(contents);
        }
        this.contents = p;
        if( this.contents != null ) {
            resetOpen();
        }
    }
 
    public Panel getContents() {
        return contents;
    }
 
    public void setTitle( String titleString ) {
        title.setText(titleString);
    }
    
    public String getTitle() {
        return title.getText();
    }
    
    public Button getTitleElement() {
        return title;
    }
    
    public Container getTitleContainer() {
        return titleContainer;
    } 
         
    public void setOpen( boolean open ) {
        openModel.setChecked(open);
        resetOpen();
    }
 
    public boolean isOpen() {
        return openModel.getObject();
    }
    
    public void setOpenModel( CheckboxModel cm ) {
        if( this.openModel == cm ) {
            return;
        }
        this.openModel = cm;
        this.openRef = openModel.createReference();
        resetOpen();
    }
    
    public CheckboxModel getOpenModel() {
        return openModel;
    } 
 
    @Override
    public void updateLogicalState( float tpf ) {
        super.updateLogicalState(tpf);
        
        if( openRef != null && openRef.update() ) {
            resetOpen();
        }
    }
    
    protected void resetOpen() {
        if( contents == null ) {
            return;
        }
        if( isOpen() ) {
            if( contents.getParent() == null ) { 
                layout.addChild(contents,  BorderLayout.Position.Center);
            }
        } else {
            if( contents.getParent() != null ) { 
                layout.removeChild(contents);
            }        
        }
    }
 
    protected class ToggleOpenCommand implements Command<Button> {
        @Override
        public void execute( Button source ) {
            setOpen(!isOpen());
        }
    }
 
    protected class OpenCheckboxModel extends DefaultCheckboxModel {
        public OpenCheckboxModel( boolean initial ) {
            super(initial);
        }
        
        public void setChecked( boolean b ) {
            super.setChecked(b);
            resetOpen();
        }
    }
}
