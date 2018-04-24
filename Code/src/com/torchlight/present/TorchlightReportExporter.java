package com.torchlight.present;
/*
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import com.archimatetool.editor.diagram.util.DiagramUtils;
import com.archimatetool.editor.diagram.util.ModelReferencedImage;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.INameable;
import com.archimatetool.model.IProperty;
import com.coreoz.ppt.PptMapper;
import com.torchlight.present.MyObject;





  

/**
 * Torchlight Reports export class
 * 
 * @author Jeff Parker
 */
public class TorchlightReportExporter {
	

	private IArchimateModel fModel;
	private File fExportFileName;
	private File fMainTemplateFile;
	private File targetImageFolder;
	private String result = "";
	private boolean error = false;
	
	boolean DEBUG = false;
	
	
	
	/**
     * Export model to Torchlight Reports
     * @param model             The ArchiMate model
     * @param exportFileName    The report file name
     * @param mainTemplateFile  The template file to be used
     */
	
    public TorchlightReportExporter(IArchimateModel model, File exportFileName, File templateFileName ) {
        fModel = model;
        fExportFileName = exportFileName;
        fMainTemplateFile = templateFileName;
        
    }
    
    public void debug(String msg)
    {
    	if (DEBUG)
    		System.out.println(msg);
    }
    
    public String result() {
    	return result;
    }
    
    public boolean error() {
    	return error;
    }
    
    public void Run()  {
    	
    	
    	
    	
    	
    	RunPptReport();
    }
    
    private void RunPptReport() {
    	try {
    		debug("Exporting: " + fModel.getName());
            
            // reset
            error = false;
            
            // create temporary folder to contain the images
            File tempFile = TorchLightPlugin.INSTANCE.getDefaultUserTempFolder();
        	targetImageFolder = new File( tempFile,"/images");
            targetImageFolder.mkdirs();
            
            // create ppt object based on the supplied template
            InputStream t = new FileInputStream(fMainTemplateFile);
            XMLSlideShow ppt = new XMLSlideShow(t);
            t.close();
           
            // get the first slide master set
            XSLFSlideMaster slideMasterSet = ppt.getSlideMasters().get(0);
            
            // get the default layout
            XSLFSlideLayout titleLayout = slideMasterSet.getLayout("model");
            if (titleLayout == null) {
            	result = "Slide master name 'model' is not present in template "+ fMainTemplateFile ;
    			error = true;
    			ppt.close();
    			return;
            }

            //creating a slide with title layout
            XSLFSlide slide1 = ppt.createSlide(titleLayout);
            
            
            
            // add the content
            new PptMapper()
			//.hide("hidden", arg -> "true".equals(arg))
			.text("modelname", fModel.getName())
	        .text("modelpurpose", normalise(fModel.getPurpose()))
			.processSlide(ppt, slide1);
            
            // write out archimate objects
            
            for(EObject objectToExport : fModel.getFolders()) {
            	String name = ((INameable) objectToExport).getName();
                //debug(objectToExport.getClass().getName()+ " " + ((INameable) objectToExport).getName()+"\n");
                if (name.equals("Views")) {
                	IFolder viewFolder = (IFolder) objectToExport;
                	writeFoldersAsPpt(viewFolder, ppt, slideMasterSet);
                }
            }
            
            
            // output the result
            FileOutputStream out = new FileOutputStream(fExportFileName);
			ppt.write(out);
			out.close();
			ppt.close();
			if (error == false) {
				result = "Content generated into "+ fExportFileName.getName();
			}
            
    	}
    	// handle exceptions
        catch ( IOException e )
        {
        	// show dialog with template error
       	 	MultiStatus status = createMultiStatus(e.getLocalizedMessage(),e);
            ErrorDialog.openError(Display.getCurrent().getActiveShell(), "IO Error", "Some kind of error", status);
            result = e.toString();
            error = true;
       	 	e.printStackTrace();
        }
    	catch(NullPointerException e) {
    		// show dialog with template error
       	 	MultiStatus status = createMultiStatus(e.getLocalizedMessage(),e);
            ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Some kind of error", status);
            result = e.toString();
            error = true;
       	 	e.printStackTrace();
    	}
    }
    
    /**
     * Write out folder content as PPT object
     * @param folder
     * @param ppt
     * @throws IOException
     */
    private void writeFoldersAsPpt(IFolder folder, XMLSlideShow ppt, XSLFSlideMaster slideMasterSet) throws IOException
    {
    	
    	List <MyObject> docTree = new ArrayList <MyObject>();
    	
    	populateTree(folder,docTree, "");
    	//printTree(docTree,"");
    	
    	populatePptOutput( docTree, ppt, slideMasterSet);
    		
    }
    
    private void populatePptOutput(List<MyObject> docTree, XMLSlideShow ppt, XSLFSlideMaster slideMasterSet) throws IOException {
    	String documentation;
    	File imageFile = null;
    	IDiagramModel diagram;
    	IDiagramModelArchimateObject elem;
    	String elemName, elemDocumentation;
    	String torchlightName, torchlightImage;
    	String torchlightImageWidth;
    	String slideMasterName = "default";
    	
    	
    	for(MyObject leaf : docTree) {
    		PptMapper pptMapper = new PptMapper(); // set collection of objects for a slide
    		
    		if (leaf.getbFolder()) { // true if folder
    			
    			//------------ add folder ---------------------------------------------------------------
    			slideMasterName = getProperty("#template", ((IFolder) leaf.getObject()).getProperties());
    			if (slideMasterName.length()==0) {
        			slideMasterName = "default";
    			}
    			
    			documentation = ((IFolder) leaf.getObject()).getDocumentation();
    			imageFile = new File(TorchLightPlugin.INSTANCE.getPluginFolder().getAbsolutePath()+"/blank.png");
    			torchlightName = getProperty("#name", ((IFolder) leaf.getObject()).getProperties());
    			if (torchlightName.length()==0) {
    				torchlightName = "name";
    			}
    			torchlightImage = "image";
    			torchlightImageWidth = "";
    		}
    		else  // not folder so diagram
    		{
    			//------------ add diagram object -------------------------------------------------------
    			// diagram
    			
    			
    			slideMasterName = "defaultimage";
    			diagram = ((IDiagramModel) leaf.getObject());
    			
    			slideMasterName = getProperty("#template", diagram.getProperties());
    			if (slideMasterName.length()==0) {
        			slideMasterName = "defaultimage";
    			}
    			torchlightName = getProperty("#name", diagram.getProperties());
    			if (torchlightName.length()==0) {
    				torchlightName = "name";
    			}
    			torchlightImage = getProperty("#image", diagram.getProperties());
    			if (torchlightImage.length()==0) {
    				torchlightImage = "image";
    			}
    			torchlightImageWidth = getProperty("#imagewidth", diagram.getProperties());
    			documentation = diagram.getDocumentation();
    			
    			 						
    			// write image
    			ModelReferencedImage geoImage = DiagramUtils.createModelReferencedImage(diagram, 1, 10);
            	String diagramName = diagram.getId()+".png";
            	Image image = geoImage.getImage();
            	                
                try {
                    ImageLoader loader = new ImageLoader();
                    loader.data = new ImageData[] { image.getImageData() };
                    imageFile = new File(targetImageFolder, diagramName);
                    loader.save(imageFile.getAbsolutePath(), SWT.IMAGE_PNG);
                }
                finally {
                    image.dispose();
                }
                
                //------------ add any elements ---------------------------------------------------------
                // filter by #element class value if set
    			
    			
    			
    			for (IDiagramModelObject obj: diagram.getChildren()) {
	    			if (obj instanceof IDiagramModelArchimateObject) {
	        			// process diagram object
	        			elem = (IDiagramModelArchimateObject) obj;
	        			//elemClass = elem.getArchimateElement().eClass().getName();
	        			//debug("writeView:class:"+elemClass);
	        			
	        			String name = getProperty("#name", (elem.getArchimateElement()).getProperties());
	        			if (name.length()>0) {
	        				
	        				// grab the elements
	        				elemName = elem.getName();
	        				elemDocumentation = elem.getArchimateConcept().getDocumentation();
	        				//debug(name+"="+elemName+ ", doc=" + normalise(elemDocumentation));    
	        				
	        				// set the elements
	        				pptMapper
	        	    		.text(name+".name", elemName)
	        	    		.text(name+".documentation", normalise(elemDocumentation));  		
	        				
	        			}
	        		}		
    			}	
    		}		
    		
    		// set image size if #imagewidth property is set
    		if (torchlightImageWidth.length() > 0) {
    			float width = 0.0f;
    			try {
    				width = Float.parseFloat(torchlightImageWidth);
    			}
    			catch (java.lang.NumberFormatException e) {
    				result = "Property #imagename="+torchlightImageWidth+" should be a number in view "+leaf.getName();
    			    error = true;
    			}
    			
    			// set value
    			if (width > 0) {
    				debug("setting...");
    				//view.setImageWidth(width);
    			}
    		}
    		
    		// output to any specific properties
    	    //   #name markup name to use
    	    //	 #image name to store the image of the diagram
    		
    		XSLFSlideLayout titleLayout = slideMasterSet.getLayout(slideMasterName);
    		if (titleLayout == null) {
    			result = "Slide name '"+slideMasterName + "' is not present in template "+ fMainTemplateFile + " for view "+ leaf.getName();
    			error = true;
    			return;
    		}

            //creating a slide with title layout
            XSLFSlide slide = ppt.createSlide(titleLayout);
    		debug(torchlightName+"="+leaf.getName()+" "+torchlightImage+"="+imageFile);
            InputStream img = new FileInputStream(imageFile);
            
            // fudge empty values as "" don't get replaced.
            if (documentation.isEmpty())
            	documentation = " ";
    		
            // add title, replace ${document} placeholder and image placeholder
            pptMapper
    		.text(torchlightName, leaf.getName())
    		.text("documentation", normalise(documentation))  		
			.image(torchlightImage, IOUtils.toByteArray(img))
    		//            	context.put(torchlightName+".documentation", documentation);
			.processSlide(ppt, slide);	
    		
    		img.close();
            
    		// add documentation to slide notes
    		XSLFNotes notesSlide = ppt.getNotesSlide(slide);
    	    for (XSLFTextShape shape : notesSlide.getPlaceholders()) {
    	        if (shape.getTextType() == Placeholder.BODY) {
    	            shape.setText(normalise(documentation));
    	            break;
    	        }
    	    }
    		
     		// process the children of this object (should only happen for folders)
    		populatePptOutput(leaf.getChildren(), ppt, slideMasterSet);
    		
        } 
    }
    
    // helper to return property key value
    // @return String - "" if key not found
    private String getProperty(String key, EList<IProperty> properties) {
    	for (IProperty prop: properties){
    		if (prop.getKey().contains(key)){
    			return prop.getValue();
    		}
		}
    	return "";
    }
    
    private String normalise(String s) {
        if(s == null) {
            return ""; //$NON-NLS-1$
        }
        
        s = s.replace("\r", "");
        //s = s.replaceAll("\n", "");
        s = s.replaceAll("\u000b","");  // this a line break character
        
        return s;
    }
    
    // helper for exception handling
    // @return MultiStatus for error handling
    private static MultiStatus createMultiStatus(String msg, Throwable t) {

        List<Status> childStatuses = new ArrayList<>();
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

        for (StackTraceElement stackTrace: stackTraces) {
            Status status = new Status(IStatus.ERROR,
                    "com.torchlight.present", stackTrace.toString());
            childStatuses.add(status);
        }

        MultiStatus ms = new MultiStatus("com.torchlight.present",
                IStatus.ERROR, childStatuses.toArray(new Status[] {}),
                t.toString(), t);
        return ms;
    }
    
    // recursively walk through archimate view folder and fill out myObject with elements
    private void populateTree(IFolder folder, List <MyObject> docTree, String tab) {
    	String name = folder.getName();
    	MyObject element = new MyObject(folder, name, true );
    	docTree.add(element);
		
    	// do all folders
    	for(IFolder f : folder.getFolders()) {
            populateTree(f, element.getChildren(), tab + ".");
        }
    	
    	// do all elements
        for(EObject object : folder.getElements()) {
        		IDiagramModel diagram = (IDiagramModel) object;   		
        		String name2 = diagram.getName();
        		element.addChild(new MyObject(object, name2, false));
        }
        
        // sort elements alphabetically  
        element.mysort();  
    }
    /*
    // used for debugging
    private void printTree(List <MyObject> docTree, String tab) {			
    	for(MyObject leaf : docTree) {
    		debug(tab+leaf.getName());
    		printTree(leaf.getChildren(), tab + ".");
        }    
    }
    */
}
