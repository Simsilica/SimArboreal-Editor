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
import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeSystem;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.event.BaseAppState;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.progeeks.json.JsonParser;
import org.progeeks.json.JsonPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  Manages the file-related actions.
 *
 *  @author    Paul Speed
 */
public class FileActionsState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(FileActionsState.class);

    private Container buttons;

    public FileActionsState() {
    }

    @Override
    protected void initialize( Application app ) {

        buttons = new Container();
        
        Button saveParms = buttons.addChild(new Button("Save Parms", "glass"));
        saveParms.addClickCommands(new SaveTreeParameters());
        saveParms.setTextHAlignment(HAlignment.Center);

        Button loadParms = buttons.addChild(new Button("Load Parms", "glass"), 1);
        loadParms.addClickCommands(new LoadTreeParameters());
        loadParms.setTextHAlignment(HAlignment.Center);
 
        Button saveJ3o = buttons.addChild(new Button("Export j3o", "glass"), 2);
        saveJ3o.addClickCommands(new SaveJ3o());
        saveJ3o.setTextHAlignment(HAlignment.Center);
        
        Button saveTreeAtlas = buttons.addChild(new Button("Save Tree Atlas", "glass"));
        saveTreeAtlas.addClickCommands(new SaveTreeAtlas());
        saveTreeAtlas.setTextHAlignment(HAlignment.Center);
    } 

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        getState(TreeOptionsState.class).getContents().addChild(buttons);
    }

    @Override
    protected void disable() {
        getState(TreeOptionsState.class).getContents().removeChild(buttons);
    }
 
    /**
     *  Filters out the stuff that we probably don't want to... or
     *  really shouldn't be saving in a j3o.  We should also remove
     *  the Impostor LODs at least until there is a way to save and/or
     *  embed the impostor image... but I don't right now.
     */
    protected Spatial filterClone( Spatial tree ) {
        Spatial result = tree.deepClone();
        result.depthFirstTraversal(new SceneGraphVisitorAdapter() {
                @Override
                public void visit( Geometry g ) {
                    if( g.getName().startsWith("wire:") ) {
                        g.removeFromParent();
                    }
                }
            });                                            
        return result;
    }
 
    private Map<String, File> lastRoots = new HashMap<String, File>();
    protected File chooseFile( String extension, final String description, final boolean save ) {
        final String ext = (!extension.startsWith(".") ? "." : "") + extension.toLowerCase();
 
        File lastRoot = lastRoots.get(ext);
        if( lastRoot == null ) {
            lastRoot = new File(".");
        }

        log.info("Creating file chooser dialog...");
        final JFileChooser openDialog = new JFileChooser();
        
        openDialog.setDialogTitle("Choose Location");
        if( save ) {
            openDialog.setDialogType(JFileChooser.SAVE_DIALOG);
        } else {
            openDialog.setDialogType(JFileChooser.OPEN_DIALOG); 
        }
        openDialog.setFileFilter(new FileFilter() {
 
                        @Override
                        public boolean accept( File file ) {
                            return file.isDirectory() || file.getName().toLowerCase().endsWith(ext);
                        }
    
                        @Override
                        public String getDescription() {
                            return description;
                        }
                    });
        openDialog.setCurrentDirectory(lastRoot);

        log.info("Opening file chooser dialog...");
        
        final int[] dialogResult = new int[1]; //JFileChooser.CANCEL_OPTION ;
        if( !SwingUtilities.isEventDispatchThread() ) {
            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                        @Override
                        public void run() {
                            if( save ) {
                                dialogResult[0] = openDialog.showSaveDialog(null);
                            } else {
                                dialogResult[0] = openDialog.showOpenDialog(null);
                            }                       
                        }
                    });
            } catch( Exception e ) {
                throw new RuntimeException("Error invoking", e);
            }
        } else {
            if( save ) {
                dialogResult[0] = openDialog.showSaveDialog(null);
            } else {
                dialogResult[0] = openDialog.showOpenDialog(null);
            }                       
        }
        
        if( dialogResult[0] != JFileChooser.APPROVE_OPTION ) {
            return null;
        }
        
        File result = openDialog.getSelectedFile();
        lastRoots.put(ext, result.getParentFile());
         
        if( save && !result.getName().toLowerCase().endsWith(ext) ) {
            result = new File(result.getParent(), result.getName() + ext);
        }
        
        return result;
    }

    protected void writeJson( File f, Map<String, Object> map ) throws IOException {
        
        FileWriter out = new FileWriter(f);
        try {            
            JsonPrinter json = new JsonPrinter();            
            json.write(map, out);
        } finally {
            out.close();
        }                       
    }
 
    protected Map<String, Object> readJson( File f ) throws IOException {
        FileReader in = new FileReader(f);
        try {
            JsonParser json = new JsonParser();
            return (Map<String, Object>)json.parse(in);
        } finally {
            in.close();
        }
    }
    
    public void saveJ3o( File f ) throws IOException {
        BinaryExporter exporter = BinaryExporter.getInstance();
        log.info("Writing:" + f);
        exporter.save(filterClone(getState(ForestGridState.class).getMainTreeNode()), f);
    }

    public void saveTreeParameters( File f ) throws IOException {    
        TreeParameters treeParameters = getState(TreeParametersState.class).getTreeParameters();
        Map<String, Object> map = treeParameters.toMap();
        log.info("Writing:" + f);
        writeJson(f, map);
    }

    public void loadTreeParameters( File f ) throws IOException {    
        Map<String, Object> map = readJson(f);
        TreeParameters treeParameters = getState(TreeParametersState.class).getTreeParameters();
        treeParameters.fromMap(map);
        getState(TreeParametersState.class).refreshTreePanels();
        getState(ForestGridState.class).rebuild();
    }

    public void savePng( File f, Image img ) throws IOException {
        OutputStream out = new FileOutputStream(f);
        try {            
            JmeSystem.writeImageFile(out, "png", img.getData(0), img.getWidth(), img.getHeight());  
        } finally {
            out.close();
        }             
    }

    public void saveTreeAtlas( File f ) throws IOException {

        Image diffuse = getState(AtlasGeneratorState.class).getDiffuseMap();
        savePng(f, diffuse);
        
        String normalName = f.getName();
        if( normalName.toLowerCase().endsWith(".png") ) {
            normalName = normalName.substring(0, normalName.length() - ".png".length());
        }
        f = new File(f.getParentFile(), normalName + "-normals.png");
        
        Image normal = getState(AtlasGeneratorState.class).getNormalMap();
        savePng(f, normal);
    }
    
    private class SaveJ3o implements Command<Button> {

        @Override
        public void execute( Button source ) {
            System.out.println( "Saving j3o..." ); 
            File f = chooseFile("j3o", "JME object file", true);
            System.out.println( "File:" + f );
            if( f == null ) {
                return;
            }
            if( f.exists() ) {
                int result = JOptionPane.showConfirmDialog(null, "Overwrite file?\n" + f, 
                                                           "File Already Exists",
                                                           JOptionPane.YES_NO_CANCEL_OPTION);
                if( result != JOptionPane.YES_OPTION ) {
                    return;
                }                                                            
            }
            
            try {
                saveJ3o(f);               
            } catch( IOException e ) {
                log.error( "Error saving tree to:" + f, e );
                JmeSystem.showErrorDialog("Error writing file:" + f + "\n" + e);                
            }
        }
    }
 
    private class SaveTreeParameters implements Command<Button> {

        @Override
        public void execute( Button source ) {
            System.out.println( "Saving stparms.json..." ); 
            File f = chooseFile("simap.json", "Tree Parameters File", true);
            System.out.println( "File:" + f );
            if( f == null ) {
                return;
            }
 
            if( f.exists() ) {
                int result = JOptionPane.showConfirmDialog(null, "Overwrite file?\n" + f, 
                                                           "File Already Exists",
                                                           JOptionPane.YES_NO_CANCEL_OPTION);
                if( result != JOptionPane.YES_OPTION ) {
                    return;
                }                                                            
            }
            
            try {
                saveTreeParameters(f);
            } catch( IOException e ) {
                log.error("Error writing file:" + f, e);
                JmeSystem.showErrorDialog("Error writing file:" + f + "\n" + e);                
            }
        }
    }

    private class LoadTreeParameters implements Command<Button> {

        @Override
        public void execute( Button source ) {
            System.out.println( "Loading stparms.json..." ); 
            File f = chooseFile("simap.json", "Tree Parameters File", false);
            System.out.println( "File:" + f );
            if( f == null ) {
                return;
            }
             
            try {
                loadTreeParameters(f);
            } catch( IOException e ) {
                log.error("Error reading file:" + f, e);
                JmeSystem.showErrorDialog("Error reading file:" + f + "\n" + e);                
            }
        }
    }
 
    private class SaveTreeAtlas implements Command<Button> {
    
        @Override
        public void execute( Button source ) {
        
            File f = chooseFile("png", "Tree Atlas Images", true);
            if( f == null ) {
                return;
            }
            if( f.exists() ) {
                int result = JOptionPane.showConfirmDialog(null, "Overwrite file?\n" + f, 
                                                           "File Already Exists",
                                                           JOptionPane.YES_NO_CANCEL_OPTION);
                if( result != JOptionPane.YES_OPTION ) {
                    return;
                }                                                            
            }
 
            try {
                saveTreeAtlas(f);       
            } catch( IOException e ) {
                log.error("Error writing file:" + f, e);
                JmeSystem.showErrorDialog("Error writing file:" + f + "\n" + e);                
            }
        }
    }
}
