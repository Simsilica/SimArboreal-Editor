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

import com.jme3.util.SafeArrayList;
import com.simsilica.lemur.CheckboxModel;
import com.simsilica.lemur.DefaultCheckboxModel;
import com.simsilica.lemur.core.VersionedReference;
import java.util.ArrayList;
import java.util.List;


/**
 *  Provides checkbox models that are linked so that
 *  only one is checked at a time.
 *
 *  @author    Paul Speed
 */
public class CheckboxModelGroup {

    private List<CheckboxModel> models = new SafeArrayList<CheckboxModel>(CheckboxModel.class);
    private List<VersionedReference<Boolean>> refs = new ArrayList<VersionedReference<Boolean>>();

    public CheckboxModelGroup() {
    }

    public CheckboxModel createChild( boolean checked ) {
        return addChild(new DefaultCheckboxModel(checked));
    }

    public CheckboxModel addChild( CheckboxModel model ) {
        int index = models.size();
        models.add(model);
        refs.add(model.createReference());        
        if( model.isChecked() ) {
            updateChecked(index);
        }
        return new CheckboxModelWrapper(model);
    }
    
    public void removeChild( CheckboxModel model ) {
        int index = models.indexOf(model);
        if( index < 0 ) {
            return;
        }
        models.remove(index);
        refs.remove(index);
    }

    public void update() {
        for( int i = 0; i < refs.size(); i++ ) {
            VersionedReference<Boolean> ref = refs.get(i);
            if( ref.update() && ref.get() ) {
                updateChecked(i);
                break;
            } 
        }
    }
    
    protected void updateChecked( int index ) {
        
        for( int i = 0; i < models.size(); i++ ) {
            if( i == index ) {
                continue;
            }
            models.get(i).setChecked(false);
        }
    }
    
    protected class CheckboxModelWrapper implements CheckboxModel {
        private CheckboxModel delegate;
        
        public CheckboxModelWrapper( CheckboxModel delegate ) {
            this.delegate = delegate;
        }
        
        @Override
        public void setChecked( boolean b ) {
            delegate.setChecked(b);
            if( b ) {
                update();
            }
        }
        
        @Override
        public boolean isChecked() {
            return delegate.isChecked();
        }
        
        @Override
        public long getVersion() {
            return delegate.getVersion();
        }

        @Override
        public Boolean getObject() {
            return delegate.getObject();
        }

        @Override
        public VersionedReference<Boolean> createReference() {
            return new VersionedReference<Boolean>(this);
        }
        
        @Override
        public String toString() {
            return "CheckboxModelWrapper[" + delegate + "]";
        } 
    } 
}

