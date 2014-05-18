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
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.simsilica.arboreal.builder.BuilderReference;
import com.simsilica.arboreal.mesh.BillboardedLeavesMeshGenerator;
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
    private Node treeNode;
    private Geometry treeGeom;
    private Material treeMaterial;
    private Geometry wireGeom;
    private Material wireMaterial;
    private Geometry leafGeom;
    private Material leafMaterial;
    private TreeParameters treeParameters;
 
    private boolean showWire;
    
    private volatile Geometry[] newGeometry;
    private AtomicInteger needsUpdate = new AtomicInteger(1);
    
    private volatile boolean check = false;
 
    public TreeBuilderReference( TreeParameters treeParameters, 
                                 Material treeMaterial, 
                                 Material wireMaterial,
                                 Material leafMaterial ) {
        this.treeParameters = treeParameters;
        this.treeMaterial = treeMaterial;
        this.wireMaterial = wireMaterial;
        this.leafMaterial = leafMaterial;
        
        treeNode = new Node("Tree");
    }        
 
    public void setSeed( int seed ) {
        this.seed = seed;
    }  
 
    public void setWireFrame( boolean b ) {
        if( showWire == b ) {
            return;
        }
        this.showWire = b;
        if( wireGeom == null ) {
            return;
        }
        if( showWire ) {
            wireGeom.setCullHint(CullHint.Inherit);
        } else {
            wireGeom.setCullHint(CullHint.Always);
        }
    }    
    
    public void markChanged() {
        needsUpdate.incrementAndGet();
    }
 
    public Node getTreeNode() {
        return treeNode;
    }
 
    public int getVertexCount() {
        int verts = 0;
        if( treeGeom != null ) {
            Mesh mesh = treeGeom.getMesh();
            verts += mesh.getVertexCount();
        }
        if( leafGeom != null ) {
            Mesh mesh = leafGeom.getMesh();
            verts += mesh.getVertexCount();
        }
        return verts;
    }
    
    public int getTriangleCount() {
        int result = 0;
        if( treeGeom != null ) {
            Mesh mesh = treeGeom.getMesh();
            result += mesh.getTriangleCount();
        }
        if( leafGeom != null ) {
            Mesh mesh = leafGeom.getMesh();
            result += mesh.getTriangleCount();
        }
        return result;
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
        if( newGeometry == null ) {
            // Nothing was built
            return;
        }
 
        if( check ) {
            log.error( "Ships have passed in the night 1." );
        }
               
        log.trace("******* applying tree ********" );        
        // Release the old ones if we had them.        
        release(treeGeom);
        release(wireGeom);
        release(leafGeom);
 
        // Add in the new ones
        treeGeom = newGeometry[0];
        wireGeom = newGeometry[1];
        leafGeom = newGeometry[2];
        newGeometry = null;
        
        treeNode.attachChild(treeGeom);       
        treeNode.attachChild(wireGeom);       
        if( leafGeom != null ) {
            treeNode.attachChild(leafGeom);
        }
               
        if( !showWire ) {
            wireGeom.setCullHint(CullHint.Always);
        }
                
        log.trace("******* tree applied ********" );        
        if( check ) {
            log.error( "Ships have passed in the night 2." );
        }       
    }
    
    @Override
    public void release() {
        release(treeGeom);
        release(wireGeom);
        release(leafGeom);
    }
 
    protected void release( Geometry geom ) {
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

        Geometry[] geometry = new Geometry[3];       
 
        TreeGenerator treeGen = new TreeGenerator();        
        Tree tree = treeGen.generateTree(seed, treeParameters);
        
        SkinnedTreeMeshGenerator meshGen = new SkinnedTreeMeshGenerator();
        
        List<Vertex> tips = new ArrayList<Vertex>();
        Mesh treeMesh = meshGen.generateMesh(tree,
                                             treeParameters.getRootHeight(), 
                                             treeParameters.getTextureURepeat(),
                                             treeParameters.getTextureVScale(),
                                             tips);        

        geometry[0] = new Geometry("Tree", treeMesh);        
        geometry[0].setMaterial(treeMaterial);
        geometry[0].setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
         

        geometry[1] = new Geometry("Tree Wire", treeMesh);
        geometry[1].setMaterial(wireMaterial);

        if( treeParameters.getGenerateLeaves() ) {
            //Sphere s = new Sphere(20, 20, treeParameters.getTrunkRadius() * 5);
            BillboardedLeavesMeshGenerator leafGen = new BillboardedLeavesMeshGenerator();
            Mesh leafMesh = leafGen.generateMesh(tips, treeParameters.getLeafScale());
            geometry[2] = new Geometry("Leaves", leafMesh);
            geometry[2].setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            geometry[2].setQueueBucket(Bucket.Transparent);
            geometry[2].setMaterial(leafMaterial);
        }

        for( Geometry geom : geometry ) {
            if( geom != null ) {
                geom.setLocalTranslation(0, treeParameters.getRootHeight(), 0);
            }
        }
        
        newGeometry = geometry;                
    }    
    
    
}


