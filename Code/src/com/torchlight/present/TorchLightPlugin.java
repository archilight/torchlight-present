package com.torchlight.present;
/*
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.archimatetool.editor.preferences.IPreferenceConstants;
import com.archimatetool.editor.preferences.Preferences;
import com.archimatetool.editor.utils.StringUtils;
import com.torchlight.present.preferences.ITorchlightPreferenceConstants;


/**
 * Activator
 * 
 * @author Juan Carlos Nova and modified by Jeff Parker
 * 
 * 
 */
public class TorchLightPlugin extends AbstractUIPlugin {
    
    public static final String PLUGIN_ID = "com.torchlight.present"; //$NON-NLS-1$

    /**
     * The shared instance
     */
    public static TorchLightPlugin INSTANCE;

    /**
     * The File location of this plugin folder
     */
    private static File fPluginFolder;
    
    /**
     * Export file settings
     */
    private static File templateFile = null;
    private static File reportFile = null;
    
    /*
     * Image width
     */
    private static float defaultImageWidth = 600f;

    public TorchLightPlugin() {
    	INSTANCE = this;
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        try {
        	//HTMLFolderReporter.cleanPreviewFiles();
        	//System.out.println("plugin stop");
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            super.stop(context);
        }
    }

    /**
     * @set and return templateFile
     */
    public File getTemplateFile()
    {
    	return templateFile;
    }
    
    public void setTemplateFile(File f)
    {
    	templateFile = f;
    }
    
   /**
    * @set and return reportFile
    */
   public File getReportFile()
   {
   	return reportFile;
   }
   
   public void setReportFile(File f)
   {
   	reportFile = f;
   }
    
    /**
     * @return The plugins folder
     */
    //public File getTemplatesFolder() {
        //return new File(getPluginFolder(), "templates"); //$NON-NLS-1$
    //}
        
    /**
     * @return The File Location of this plugin
     */
    public File getPluginFolder() {
    	if(fPluginFolder == null) {
            URL url = getBundle().getEntry("/"); //$NON-NLS-1$
            try {
                url = FileLocator.resolve(url);
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
            fPluginFolder = new File(url.getPath());
        }
        return fPluginFolder;
    }
    
    

    /**
     * @return Default user temp folder
     */
    public File getDefaultUserTempFolder() {
        File folder = new File(Preferences.STORE.getString(IPreferenceConstants.USER_DATA_FOLDER), "torchlight-present"); //$NON-NLS-1$
        if (!folder.exists())
        	folder.mkdirs();
        return folder;
    }
    
    /**
     * @return User-set user templates folder
     */
    public File getUserTemplatesFolder() {

        String s = getPreferenceStore().getString(ITorchlightPreferenceConstants.TORCHLIGHT_USER_TEMPLATES_FOLDER);
        if(StringUtils.isSetAfterTrim(s)) {
            File f = new File(s);
            f.mkdirs();
            if(f.exists() && f.isDirectory()) {
                return f;
            }
        }
        
        return getDefaultUserTemplatesFolder();
        
    }

    /**
     * @return Default user templates folder
     */
    public File getDefaultUserTemplatesFolder() {
        //File folder = new File(Preferences.STORE.getString(IPreferenceConstants.USER_DATA_FOLDER), "torchlight-templates"); //$NON-NLS-1$
    	File folder = new File(getPluginFolder(), "templates"); //$NON-NLS-1$
    	folder.mkdirs();
        
        return folder;
    }
    
    /**
     * @return User-set user reports folder
     */
    public File getUserReportsFolder() {
    	
        String s = getPreferenceStore().getString(ITorchlightPreferenceConstants.TORCHLIGHT_USER_REPORTS_OUT_FOLDER);
        if(StringUtils.isSetAfterTrim(s)) {
        	
            File f = new File(s);           
            f.mkdirs();
            if(f.exists() && f.isDirectory()) {
                return f;
            }
        }
        
        return getDefaultUserReportsFolder();
        
    }

    /**
     * @return Default user reports folder
     */
    public File getDefaultUserReportsFolder() {
        File folder = new File(Preferences.STORE.getString(IPreferenceConstants.USER_DATA_FOLDER), "torchlight-present"); 
    	folder.mkdirs();
        
        return folder;
    }

    /*
     * @return Float containing default image width
     */
    public float getDefaultImageWidth() {
    	return defaultImageWidth;    
    }
    
    public float getImageWidth() {
    	float currentWidth = getPreferenceStore().getFloat(ITorchlightPreferenceConstants.TORCHLIGHT_IMAGEWIDTH);    	
    	return currentWidth;
    }
    
    
}