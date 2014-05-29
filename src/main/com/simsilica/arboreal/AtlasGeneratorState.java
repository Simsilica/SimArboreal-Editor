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
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.simsilica.arboreal.builder.Builder;
import com.simsilica.arboreal.builder.BuilderReference;
import com.simsilica.arboreal.mesh.BillboardedLeavesMeshGenerator;
import com.simsilica.arboreal.mesh.SkinnedTreeMeshGenerator;
import com.simsilica.arboreal.mesh.Vertex;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *  @author    Paul Speed
 */
public class AtlasGeneratorState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(AtlasGeneratorState.class);
    
    private VersionedReference<TreeParameters> treeParametersRef;
    private Material treeMaterial;    
    private Material leafMaterial;    

    private Builder builder;
    private AtlasTreeBuilderReference builderRef; 

    private Mesh trunkMesh;
    private Mesh leafMesh;
    
    private CellView[] cellViews = new CellView[8];    

    private BitmapFont font;
    
    private boolean debugTextures = false;
    
    private boolean useNormalMaps = true;

    @Override
    protected void initialize( Application app ) {
 
        this.treeParametersRef = getState(TreeParametersState.class).getTreeParametersRef();
        this.treeMaterial = getState(ForestGridState.class).getTreeMaterial();
        this.leafMaterial = getState(ForestGridState.class).getLeafMaterial();
 
        this.builder = getState(BuilderState.class).getBuilder();
        this.builderRef = new AtlasTreeBuilderReference();
        
 
        this.font = GuiGlobals.getInstance().loadFont("Interface/Fonts/Default.fnt");
        
        Camera camera = app.getCamera().clone();   
        camera.resize(256, 256, true);
        camera.resize(1024, 256, false);
  
 
        FrameBuffer fb1 = new FrameBuffer(1024, 256, 1);
        Texture2D fbTex1 = new Texture2D(1024, 256, Format.RGBA8);
        fb1.setDepthBuffer(Format.Depth);
        fb1.setColorTexture(fbTex1);
        getState(ForestGridState.class).getImpostorMaterial().setTexture("DiffuseMap", fbTex1);
    
        FrameBuffer fb2 = new FrameBuffer(1024, 256, 1);
        Texture2D fbTex2 = new Texture2D(1024, 256, Format.RGBA8);
        fb2.setDepthBuffer(Format.Depth);
        fb2.setColorTexture(fbTex2); 
        
        if( useNormalMaps ) {
            getState(ForestGridState.class).getImpostorMaterial().setTexture("NormalMap", fbTex2);
        }
        
 
        if( debugTextures ) {       
            Quad testQuad = new Quad(1024, 256);
            Geometry testGeom = new Geometry("test", testQuad);
            Material mat = GuiGlobals.getInstance().createMaterial(fbTex1, false).getMaterial();
            testGeom.setMaterial(mat);
            ((TreeEditor)app).getGuiNode().attachChild(testGeom);
            
            testQuad = new Quad(1024, 256);
            testGeom = new Geometry("test", testQuad);
            testGeom.setLocalTranslation(0, 256, 0);
            mat = GuiGlobals.getInstance().createMaterial(fbTex2, false).getMaterial();
            testGeom.setMaterial(mat);
            ((TreeEditor)app).getGuiNode().attachChild(testGeom);
        }
 
        DirectionalLight sun = new DirectionalLight();
        //sun.setDirection(new Vector3f(0, -1f, -1).normalizeLocal());
        sun.setDirection(new Vector3f(0, 0, -1).normalizeLocal());
 
        AmbientLight ambient = new AmbientLight();

        if( useNormalMaps ) {
            sun.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1));
            ambient.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1));
        } else {
            sun.setColor(new ColorRGBA(1, 1, 1, 1));
            ambient.setColor(new ColorRGBA(0.25f, 0.25f, 0.25f, 1));            
        }
 
        //-x * FastMath.TWO_PI - FastMath.QUARTER_PI
        // The texture quads actually run a, c, d, b starting with
        // the +, + quadrant
        cellViews[0] = new CellView(fb1, camera, sun, ambient, FastMath.QUARTER_PI, 0.25f * 0);
        cellViews[1] = new CellView(fb1, camera, sun, ambient, -FastMath.QUARTER_PI, 0.25f * 1);
        cellViews[2] = new CellView(fb1, camera, sun, ambient, FastMath.PI - FastMath.QUARTER_PI, 0.25f * 2);
        cellViews[3] = new CellView(fb1, camera, sun, ambient, FastMath.PI + FastMath.QUARTER_PI, 0.25f * 3);
        
        cellViews[4] = new NormalMapCellView(fb2, camera, sun, ambient, FastMath.QUARTER_PI, 0.25f * 0);
        cellViews[5] = new NormalMapCellView(fb2, camera, sun, ambient, -FastMath.QUARTER_PI, 0.25f * 1);
        cellViews[6] = new NormalMapCellView(fb2, camera, sun, ambient, FastMath.PI - FastMath.QUARTER_PI, 0.25f * 2);
        cellViews[7] = new NormalMapCellView(fb2, camera, sun, ambient, FastMath.PI + FastMath.QUARTER_PI, 0.25f * 3);
        
    }

    @Override
    protected void cleanup( Application app ) {
        for( CellView view : cellViews ) {
            app.getRenderManager().removeMainView(view.getViewPort());
        }
    }

    @Override
    protected void enable() {
    }

    @Override
    protected void disable() {
    }

    protected void updateTree( Mesh trunkMesh, Mesh leafMesh ) {
        if( this.trunkMesh == trunkMesh ) {
            return;
        }
               
        releaseMesh(this.trunkMesh);
        releaseMesh(this.leafMesh);
        this.trunkMesh = trunkMesh;
        this.leafMesh = leafMesh;
        
        for( CellView view : cellViews ) {
            if( view != null ) {
                view.updateMesh(trunkMesh, leafMesh);
            }
        }
    }

    protected void releaseMesh( Mesh mesh ) {
        if( mesh == null ) {
            return;
        }
 
        // Delete the old buffers
        for( VertexBuffer vb : mesh.getBufferList() ) {
            if( log.isTraceEnabled() ) {
                log.trace("--destroying buffer:" + vb);
            }
            BufferUtils.destroyDirectBuffer( vb.getData() );
        }                            
    }        

    private float nextUpdateCheck = 0.1f;
    private float lastTpf;
    @Override
    public void update( float tpf ) {
        lastTpf = tpf;
        
        nextUpdateCheck += tpf;
        if( nextUpdateCheck <= 0.1f ) {
            return;
        }
        nextUpdateCheck = 0;

        boolean changed = treeParametersRef.update();
        if( changed ) {
            builder.build(builderRef);
        }
    }

    @Override
    public void render( RenderManager rm ) {
        if( cellViews != null ) {
            // We update the logical state here because it is
            // done after the other updates.  So if another app
            // state or control has modified our root then we
            // are guaranteed to run after.
            for( CellView view : cellViews ) {
                if( view != null ) {
                    view.update(lastTpf);
                }               
            }
        }
    }

    private class CellView {
        private ViewPort viewport;
        private Camera camera;
        private Node root;
        private Mesh leafMesh;
        private Mesh trunkMesh;
        private Geometry trunkGeom;
        private Geometry leafGeom;
        private Geometry wireBounds;
        private boolean debugBounds = false;
        private boolean debugCell = false;
        
        public CellView( FrameBuffer fb, Camera templateCamera, DirectionalLight sun, AmbientLight ambient, float angle, float x ) {
        
            this.camera = templateCamera.clone();
            camera.setViewPort(x, x + 0.25f, 0, 1);
            
            this.root = new Node("CellRoot:" + x );
            this.viewport = getApplication().getRenderManager().createMainView("AtlasCell[" + x + "]", camera);
            this.viewport.setOutputFrameBuffer(fb);
            this.root.rotate(0, -angle, 0);
 
            if( debugCell ) {
                BitmapText label = new BitmapText(font);
                label.setText("u:" + x + "\na:" + angle);
                label.setLocalScale(0.01f);
                Quaternion labelRot = root.getLocalRotation().inverse(); 
                label.setLocalRotation(labelRot);
                label.setLocalTranslation(labelRot.mult(new Vector3f(0, 1, 2)));
                root.attachChild(label);
            }
            
            viewport.attachScene(root);
            root.addLight(sun);
            root.addLight(ambient);
            
            viewport.setClearFlags(true, true, true);
            viewport.setBackgroundColor(new ColorRGBA(0, 0, 0, 0));
            this.camera.lookAtDirection(new Vector3f(0, 0, -1), Vector3f.UNIT_Y);
        }

        public ViewPort getViewPort() {
            return viewport;
        }

        public void update( float tpf ) {
            root.updateLogicalState(tpf);
            root.updateGeometricState();
        }

        protected Material getTreeMaterial() {
            return treeMaterial;
        }
 
        protected Material getLeafMaterial() {
            return leafMaterial;       
        }            

        public void updateMesh( Mesh trunkMesh, Mesh leafMesh ) {
            if( trunkGeom == null ) {
                // Create it
                trunkGeom = new Geometry("Trunk", trunkMesh);
                trunkGeom.setMaterial(getTreeMaterial());
                root.attachChild(trunkGeom);
            } else {
                // Just swap out the mesh
                trunkGeom.setMesh(trunkMesh);
            }
            this.trunkMesh = trunkMesh;
            this.leafMesh = leafMesh;
            if( leafMesh == null ) {
                if( leafGeom != null ) {
                    leafGeom.removeFromParent();
                    leafGeom = null;
                }
            } else {            
                if( leafGeom == null ) {
                    // Create it
                    leafGeom = new Geometry("Leaves", leafMesh);
                    leafGeom.setMaterial(getLeafMaterial());
                    leafGeom.setQueueBucket(Bucket.Transparent);  
                    root.attachChild(leafGeom); 
                } else {
                    // Just swap out the mesh
                    leafGeom.setMesh(leafMesh);
                }
            }                
            updateCamera();
        }
        
        protected void updateCamera() {
 
            BoundingBox bb = (BoundingBox)trunkMesh.getBound();
            if( leafGeom != null ) {
                BoundingBox bb2 = (BoundingBox)leafMesh.getBound();
                bb = (BoundingBox)bb.merge(bb2);
            }
            
            Vector3f min = bb.getMin(null);
            Vector3f max = bb.getMax(null);
 
            float xSize = Math.max(Math.abs(min.x), Math.abs(max.x));
            float ySize = max.y - min.y;
            float zSize = Math.max(Math.abs(min.z), Math.abs(max.z));
 
            float size = ySize * 0.5f;
            size = Math.max(size, xSize);
            size = Math.max(size, zSize);
            
            // In the projection matrix, [1][1] should be:
            //      (2 * Zn) / camHeight
            // where Zn is distance to near plane.
            float m11 = camera.getViewProjectionMatrix().m11;

            // We want our position to be such that
            // 'size' is otherwise = cameraHeight when rendered.
            float z = m11 * size;
        
            // Add the z extents so that we adjust for the near plane
            // of the bounding box... well we will be rotating so
            // let's just be sure and take the max of x and z
            //float offset = Math.max(bb.getXExtent(), bb.getZExtent());
            //z += offset;
            // This creates problems because it makes way too much
            // space around the tree.  A proper solution would require
            // a bunch of math and in the end would also have to be duplicated
            // on the quad generation side or somehow stored with the atlas.
        
            Vector3f center = bb.getCenter();
 
            float sizeOffset = size - (ySize*0.5f); 
 
            Vector3f camLoc = new Vector3f(0, center.y + sizeOffset, z); 
            camera.setLocation(camLoc);
 
            if( debugBounds ) {       
                WireBox box;        
                if( wireBounds == null ) {
                    box = new WireBox();
                    wireBounds = new Geometry("wire box", box);
                    Material mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Yellow, false).getMaterial();
                    wireBounds.setMaterial(mat);
                    root.attachChild(wireBounds);
                } else {
                    box = (WireBox)wireBounds.getMesh();
                }
                box.updatePositions(bb.getXExtent(), bb.getYExtent(), bb.getZExtent());
                box.setBound(new BoundingBox(new Vector3f(0,0,0), 0, 0, 0));
                wireBounds.setLocalTranslation(bb.getCenter());
                wireBounds.setLocalRotation(leafGeom.getLocalRotation());
            }        
        }

    }


    private class NormalMapCellView extends CellView { 
        public NormalMapCellView( FrameBuffer fb, Camera templateCamera, DirectionalLight sun, AmbientLight ambient, float angle, float x ) {
            super(fb, templateCamera, sun, ambient, angle, x); 
        }

        @Override
        protected Material getTreeMaterial() {
            Material normalMaterial = treeMaterial.clone();
            normalMaterial.selectTechnique("PreNormalPass", getApplication().getRenderManager());
            return normalMaterial;
        }
 
        @Override
        protected Material getLeafMaterial() {
            Material normalMaterial = leafMaterial.clone();
            normalMaterial.selectTechnique("PreNormalPass", getApplication().getRenderManager());
            return normalMaterial;
        }            
    }
 

    private class AtlasTreeBuilderReference implements BuilderReference {

        private Mesh trunkMesh;
        private Mesh leafMesh;        

        @Override
        public int getPriority() {
            // A relatively low priority
            return 100;
        }

        @Override
        public void build() {

            TreeParameters treeParameters = treeParametersRef.get();
            
            TreeGenerator treeGen = new TreeGenerator();        
            Tree tree = treeGen.generateTree(treeParameters);
            
            SkinnedTreeMeshGenerator meshGen = new SkinnedTreeMeshGenerator();
        
            List<Vertex> tips = new ArrayList<Vertex>();
            trunkMesh = meshGen.generateMesh(tree,
                                             treeParameters.getLod(0),
                                             treeParameters.getYOffset(), 
                                             treeParameters.getTextureURepeat(),
                                             treeParameters.getTextureVScale(),
                                             tips);

            if( treeParameters.getGenerateLeaves() ) {
                BillboardedLeavesMeshGenerator leafGen = new BillboardedLeavesMeshGenerator();
                leafMesh = leafGen.generateMesh(tips, treeParameters.getLeafScale());
            } else {
                leafMesh = null;
            }            
        }

        @Override
        public void apply() {
            // Set the new trunk
            updateTree(trunkMesh, leafMesh);               
        }

        @Override
        public void release() {
            
        }        
    }
}
