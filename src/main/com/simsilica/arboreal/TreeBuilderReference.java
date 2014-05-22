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


import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.simsilica.arboreal.builder.BuilderReference;
import com.simsilica.arboreal.mesh.BillboardedLeavesMeshGenerator;
import com.simsilica.arboreal.mesh.LodSwitchControl;
import com.simsilica.arboreal.mesh.SkinnedTreeMeshGenerator;
import com.simsilica.arboreal.mesh.Vertex;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *  @author    Paul Speed
 */
public class TreeBuilderReference implements BuilderReference
{
    static Logger log = LoggerFactory.getLogger(TreeBuilderReference.class);
    
    private int priority;
 
    private int seed;   
    private Material treeMaterial;
    private Material wireMaterial;
    private Material leafMaterial;
    private TreeParameters treeParameters;
    
    private Node treeNode;
 
    private LevelGeometry[] lods;
    private volatile LevelGeometry[] newLods;
     
    private boolean showWire;
    
    private AtomicInteger needsUpdate = new AtomicInteger(1);
    
    // For debugging    
    private volatile boolean check = false;
 
    public TreeBuilderReference( TreeParameters treeParameters, 
                                 Material treeMaterial, 
                                 Material wireMaterial,
                                 Material leafMaterial ) {
        this.treeParameters = treeParameters;
        this.treeMaterial = treeMaterial;
        this.wireMaterial = wireMaterial;
        this.leafMaterial = leafMaterial;
 
        lods = new LevelGeometry[treeParameters.getLodCount()];       
        treeNode = new Node("Tree");
        treeNode.addControl(new LodSwitchControl());
    }        
 
    public void setSeed( int seed ) {
        this.seed = seed;
    }  
 
    public void setWireFrame( boolean b ) {
        if( showWire == b ) {
            return;
        }
        this.showWire = b;
        for( LevelGeometry level : lods ) {
            if( level == null ) {
                continue;
            }
            if( showWire ) {
                level.wireGeom.setCullHint(CullHint.Inherit);
            } else {
                level.wireGeom.setCullHint(CullHint.Always);
            }
        }
    }    
    
    public void markChanged() {
        needsUpdate.incrementAndGet();
    }
 
    public Node getTreeNode() {
        return treeNode;
    }
 
    public int getVertexCount( int lod ) {
        LevelGeometry level = lods[lod];
        if( level == null ) {
            return 0;
        }
        
        int result = 0;
        if( level.treeGeom != null ) {
            Mesh mesh = level.treeGeom.getMesh();
            result += mesh.getVertexCount();
        }
        if( level.leafGeom != null ) {
            Mesh mesh = level.leafGeom.getMesh();
            result += mesh.getVertexCount();
        }
        return result;       
    }

    public int getTriangleCount( int lod ) {
        LevelGeometry level = lods[lod];
        if( level == null ) {
            return 0;
        }
        
        int result = 0;
        if( level.treeGeom != null ) {
            Mesh mesh = level.treeGeom.getMesh();
            result += mesh.getTriangleCount();
        }
        if( level.leafGeom != null ) {
            Mesh mesh = level.leafGeom.getMesh();
            result += mesh.getTriangleCount();
        }
        return result;       
    }
 
    public int getVertexCount() {
        return getVertexCount(0);
    }
    
    public int getTriangleCount() {
        return getTriangleCount(0);
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    @Override
    public void build() {
        if( needsUpdate.get() == 0 ) {
            return;
        }
        needsUpdate.set(0);
 
        check = true;
        try {
            log.trace("******* rebuilding tree ********");        
            regenerateTree();
            log.trace("******* tree built ********" );
        } finally {
            check = false; 
        }        
    }
    
    @Override
    public void apply() {
        if( newLods == null ) {
            // Nothing was built
            return;
        }
 
        if( check ) {
            log.error( "Ships have passed in the night 1." );
        }
               
        log.trace("******* applying tree ********" );
 
        LodSwitchControl lodControl = treeNode.getControl(LodSwitchControl.class);
        lodControl.clearLevels();
                
        // Release the old ones if we had them.
        for( LevelGeometry g : lods ) {
            if( g != null ) {
                g.release();
            }
        }        
 
        // Add in the new ones
        for( int i = 0; i < lods.length; i++ ) {
            lods[i] = newLods[i];
            lods[i].attach(lodControl);
            if( !showWire && lods[i] != null ) {
                lods[i].wireGeom.setCullHint(CullHint.Always);
            }
        }
        newLods = null;
 
        log.trace("******* tree applied ********" );        
        if( check ) {
            log.error( "Ships have passed in the night 2." );
        }       
    }
    
    @Override
    public void release() {
        for( LevelGeometry g : lods ) {
            if( g != null ) {
                g.release();
            }
        }        
    }
 
    protected void releaseGeometry( Geometry geom ) {
        if( geom == null ) {
            return;
        }
 
        geom.removeFromParent();
        
        Mesh mesh = geom.getMesh();
                           
        // Delete the old buffers
        for( VertexBuffer vb : mesh.getBufferList() ) {
            if( log.isTraceEnabled() ) {
                log.trace("--destroying buffer:" + vb);
            }
            BufferUtils.destroyDirectBuffer( vb.getData() );
        }                            
    }
        
    protected void regenerateTree() {

        LevelGeometry[] levels = new LevelGeometry[treeParameters.getLodCount()];       

        TreeGenerator treeGen = new TreeGenerator();        
        Tree tree = treeGen.generateTree(seed, treeParameters);
 
        List<Vertex> baseTips = null;
 
        for( int i = 0; i < levels.length; i++ ) {
            
            LevelOfDetailParameters lodParms = treeParameters.getLod(i);
            LevelGeometry level = new LevelGeometry(lodParms.distance);
            levels[i] = level;
                 
            SkinnedTreeMeshGenerator meshGen = new SkinnedTreeMeshGenerator();
        
            List<Vertex> tips = null;
            if( baseTips == null ) {
                baseTips = tips = new ArrayList<Vertex>();
            }
            Mesh treeMesh = meshGen.generateMesh(tree,
                                                treeParameters.getLod(i),
                                                treeParameters.getYOffset(), 
                                                treeParameters.getTextureURepeat(),
                                                treeParameters.getTextureVScale(),
                                                tips);        

            level.treeGeom = new Geometry("Tree", treeMesh);
            level.treeGeom.setMaterial(treeMaterial);
            level.treeGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            level.treeGeom.setLocalTranslation(0, treeParameters.getRootHeight(), 0);
            
            level.wireGeom = new Geometry("Tree Wire", treeMesh);
            level.wireGeom.setMaterial(wireMaterial);
            level.wireGeom.setLocalTranslation(0, treeParameters.getRootHeight(), 0);
              
            if( treeParameters.getGenerateLeaves() ) {
                BillboardedLeavesMeshGenerator leafGen = new BillboardedLeavesMeshGenerator();
                Mesh leafMesh = leafGen.generateMesh(baseTips, treeParameters.getLeafScale());
                level.leafGeom = new Geometry("Leaves", leafMesh);
                level.leafGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);  
                level.leafGeom.setQueueBucket(Bucket.Transparent);  
                level.leafGeom.setMaterial(leafMaterial);  
                
                level.leafGeom.setLocalTranslation(0, treeParameters.getRootHeight(), 0);
            }
        }

        newLods = levels;
    }    
 
    /**
     *  Encapsulates all of the tree geometry for a
     *  particular level of detail.
     */   
    private class LevelGeometry {
 
        float distance;
        Node levelNode;   
        Geometry treeGeom;
        Geometry wireGeom;
        Geometry leafGeom;
        
        public LevelGeometry( float distance ) {
            this.distance = distance;
        }

        public void attach( LodSwitchControl control ) {
            levelNode = new Node("level:" + distance);
            levelNode.attachChild(treeGeom);       
            levelNode.attachChild(wireGeom);       
            if( leafGeom != null ) {
                levelNode.attachChild(leafGeom);
            }
            control.addLevel(distance, levelNode);            
        }
        
        public void release() {
            levelNode.removeFromParent();
            releaseGeometry(treeGeom);
            releaseGeometry(wireGeom);
            releaseGeometry(leafGeom);
        }
    }    
}


