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
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Node;
import com.simsilica.arboreal.builder.Builder;
import java.util.Random;


/**
 *
 *  @author    Paul Speed
 */
public class ForestGrid {

    private int width;
    private int height;
    private float spacing;
    private int rootSeed;
    private int seedRange;
    
    private float rotationVariation = 1f;
    private float leanVariation = 0.1f;
    private float scaleVariation = 0.3f;
    private float positionVariation = 0;
    
    private Node root;
 
    private Builder builder;   
    private TreeBuilderReference[][] trees;
 
    private TreeParameters treeParameters; 
    private Material treeMaterial; 
    private Material wireMaterial;
    private Material leafMaterial;
    private Material flatMaterial;
 
    private boolean showWireframe;
    private boolean showLeaves;
       
    public ForestGrid( TreeParameters treeParameters, 
                       Material treeMaterial,
                       Material wireMaterial,
                       Material leafMaterial,
                       Material flatMaterial,
                       Builder builder ) {         
        this.treeParameters = treeParameters;
        this.treeMaterial = treeMaterial;
        this.wireMaterial = wireMaterial;
        this.leafMaterial = leafMaterial;                       
        this.flatMaterial = flatMaterial;                       
        this.builder = builder;
        this.root = new Node("Forest"); 
        this.spacing = 5;
        this.seedRange = 9;
        setSize(1, 1);        
    }
 
    public Node getRootNode() {
        return root;
    }
    
    public TreeBuilderReference getTree( int i, int j ) {
        return trees[i][j];
    }
    
    public void setShowWireframe( boolean b ) {
        if( this.showWireframe == b ) {
            return;
        }
        this.showWireframe = b;
        refreshWireframe();
    } 

    public void setSeedRange( int range ) {
        if( this.seedRange == range ) {
            return;
        }
        this.seedRange = range;
        refreshSeed();
    }
 
    public int getSeedRange() {
        return seedRange;
    }
    
    public void setSpacing( float f ) {
        if( this.spacing == f ) {
            return;
        }
        this.spacing = f;
        refreshVariation();
    }
    
    public float getSpacing() {
        return spacing;
    }

    public void setRotationVariation( float f ) {
        if( this.rotationVariation == f ) {
            return;
        }
        this.rotationVariation = f;
        refreshVariation();
    }
    
    public float getRotationVariation() {
        return rotationVariation;
    }

    public void setLeanVariation( float f ) {
        if( this.leanVariation == f ) {
            return;
        }
        this.leanVariation = f;
        refreshVariation();
    }
    
    public float getLeanVariation() {
        return leanVariation;
    }

    public void setScaleVariation( float f ) {
        if( this.scaleVariation == f ) {
            return;
        }
        this.scaleVariation = f;
        refreshVariation();
    }
    
    public float getScaleVariation() {
        return scaleVariation;
    }

    public void setPositionVariation( float f ) {
        if( this.positionVariation == f ) {
            return;
        }
        this.positionVariation = f;
        refreshVariation();
    }
    
    public float getPositionVariation() {
        return positionVariation;
    }

    
    public void setWidth( int width ) {
        setSize(width, height);       
    }
 
    public int getWidth() {
        return width;
    }
    
    public void setHeight( int height ) {
        setSize(width, height);       
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setSize( int width, int height ) {
 
        if( this.width == width && this.height == height ) {
            return;
        }

        builder.pause();
        
        if( trees != null ) {
            // Cancel and remove the ones that will go away
            if( this.width > width ) {
                for( int i = width; i < this.width; i++ ) {
                    for( int j = 0; j < this.height; j++ ) {
                        trees[i][j].getTreeNode().removeFromParent();
                        builder.release(trees[i][j]);
                        trees[i][j] = null;
                    }
                }
            }
            if( this.height > height ) {
                for( int i = 0; i < this.width; i++ ) {
                    for( int j = height; j < this.height; j++ ) {
                        trees[i][j].getTreeNode().removeFromParent();
                        builder.release(trees[i][j]);
                        trees[i][j] = null;
                    }
                }
            }
        }

        // Copy what we can into a new structure
        TreeBuilderReference[][] newTrees = new TreeBuilderReference[width][height];
        int w = Math.min(width, this.width);
        int h = Math.min(height, this.height);
        for( int i = 0; i < w; i++ ) {
            for( int j = 0; j < h; j++ ) {
                newTrees[i][j] = trees[i][j];
            }
        }
        
        this.width = width;
        this.height = height;
 
        trees = newTrees;
        
        // Fill in any missing cells
        for( int i = 0; i < width; i++ ) {
            for( int j = 0; j < height; j++ ) {
                if( trees[i][j] == null ) {
                    trees[i][j] = new TreeBuilderReference(treeParameters, 
                                                           treeMaterial, 
                                                           wireMaterial, 
                                                           leafMaterial,
                                                           flatMaterial);
                    Node tree = trees[i][j].getTreeNode();
                    tree.setLocalTranslation(i * spacing, 0, j * spacing);
                    tree.setLocalScale(treeParameters.getBaseScale());
                    root.attachChild(tree);
                    builder.build(trees[i][j]);                                                           
                }   
            }
        }
 
        refreshSeed();
        refreshWireframe();
        refreshVariation();
        
        builder.resume();
    }
 
    public void markChanged() {
        for( int i = 0; i < width; i++ ) {
            for( int j = 0; j < height; j++ ) {
                if( trees[i][j] == null ) {
                    continue;
                }
                trees[i][j].markChanged();
            }
        }
        
        // Because it's a value we sort of cache and there
        // is no external way to detect it's changed.
        refreshSeed();
    }
    
    public void rebuild() {
        for( int i = 0; i < width; i++ ) {
            for( int j = 0; j < height; j++ ) {            
                if( trees[i][j] == null ) {
                    continue;
                }
                builder.build(trees[i][j]);
            }
        }
    }
    
    protected void refreshWireframe() {
        for( int i = 0; i < width; i++ ) {
            for( int j = 0; j < height; j++ ) {
                if( trees[i][j] == null ) {
                    continue;
                }
                trees[i][j].setWireFrame(showWireframe);
            }
        }
    }
        
    protected void refreshSeed() {
        int index = 0;
        int rootSeed = treeParameters.getSeed();
        for( int i = 0; i < width; i++ ) {
            for( int j = 0; j < height; j++ ) {
                if( trees[i][j] == null ) {
                    continue;
                }
                trees[i][j].setSeed(rootSeed + (index % seedRange));
                index++;
            }
        }
    }
    
    protected void refreshVariation() {
    
        // If there is only one tree then we won't vary it
        // at all... we could also have made sure 0,0 was always
        // 0 variation but I think this will be ok
        if( width == 1 && height == 1 ) {
            // Just make sure it doesn't have anything weird
            Node tree = trees[0][0].getTreeNode();
            tree.setLocalScale(treeParameters.getBaseScale());
            tree.setLocalRotation(new Quaternion());
            return;
        }
        
    
        Random rand = new Random(0);
        for( int i = 0; i < width; i++ ) {
            for( int j = 0; j < height; j++ ) {
                if( trees[i][j] == null ) {
                    continue;
                }
                Node tree = trees[i][j].getTreeNode();
                float x = leanVariation * ((rand.nextFloat() * 2) - 1) * FastMath.QUARTER_PI;
                float y = leanVariation * ((rand.nextFloat() * 2) - 1) * FastMath.QUARTER_PI;
                float scale = scaleVariation * ((rand.nextFloat() * 2) - 1);
                float angle = rotationVariation * ((rand.nextFloat() * 2) - 1) * FastMath.TWO_PI;
                
                Quaternion rot = new Quaternion();
                rot = rot.mult(new Quaternion().fromAngles(0, angle, 0));
                rot = rot.mult(new Quaternion().fromAngles(x, 0, 0));
                rot = rot.mult(new Quaternion().fromAngles(0, 0, y));
                
                tree.setLocalRotation(rot);
 
                if( scale < 0 ) 
                    scale *= 0.5f;               
                tree.setLocalScale((1 + scale) * treeParameters.getBaseScale());
                
                float xOffset = positionVariation * (rand.nextFloat() - 0.5f) * spacing; 
                float yOffset = positionVariation * (rand.nextFloat() - 0.5f) * spacing;
                tree.setLocalTranslation(i * spacing + xOffset, 0, j * spacing + yOffset); 
            }
        }
    }
}
