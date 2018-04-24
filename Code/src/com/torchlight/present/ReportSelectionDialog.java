package com.torchlight.present;
/*
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.archimatetool.editor.ui.UIUtils;
import com.archimatetool.model.IArchimateModel;

/**
 * Torchlight Reports Dialog Page
 * 
 * @author Jeff Parker
 * 
 */
public class ReportSelectionDialog extends TitleAreaDialog {
	
	// UI Elements
	Button generateButton, closeButton;
	
	Group reportsGroup;
	Text reportsText;
	Button reportsButton;
	
	Group templatesGroup;
	ComboViewer templatesViewer;
	
	Image icon;
	
	// 
	IArchimateModel fModel;
	
	// used to select report type
	String MY_EXTENSION = ".pptx"; //$NON-NLS-1$
    String MY_EXTENSION_WILDCARD = "*.pptx"; //$NON-NLS-1$
    String FILTER =  "*.{pptx}"; //  "*.{pptx,docx}";
    
    // template list
    private List<File> templateFiles; 
    File templateFile = null; // currently selected File in TemplateFiles
    
    // flags to enable generate button
    boolean bTemplateSelected = false;
    
	
	public ReportSelectionDialog(Shell parentShell, IArchimateModel model) {
        super(parentShell);
        fModel = model;
        templateFiles = new ArrayList <File>();
        
        templateFile = TorchLightPlugin.INSTANCE.getTemplateFile();
        
        // dialog image
        File plugin = TorchLightPlugin.INSTANCE.getPluginFolder(); 
        String pluginiconPath = plugin.getAbsolutePath()+"/torchlight.jpg";
        File pluginiconFile = new File(pluginiconPath);
        if (!pluginiconFile.exists()) {
        	MessageDialog.openError(Display.getCurrent().getActiveShell(), "Config error",  pluginiconPath + " does not exist!");
        	return;
        }
        icon = new Image(Display.getDefault(),pluginiconPath);
    }

	// set dialog title
	@Override
	protected void configureShell(Shell shell) {
	    super.configureShell(shell);
	    shell.setText("Torchlight Export: "+fModel.getName());
	}
	
    @Override
    public void create() {
        super.create();
        
        setTitleAreaColor(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB());
    }
    
    @Override
    public boolean close() {
        if (icon != null)
            icon.dispose();
        return super.close();
    }

    
    // create dialogue content
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        //container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        //GridLayout layout = new GridLayout(2, false);
        //container.setLayout(layout);
        
        container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        container.setLayout(new GridLayout(1, false));
        //GridData gd = new GridData(GridData.FILL_VERTICAL);
        //gd.widthHint = 500;
        //container.setLayoutData(gd);
        

        createTemplatesGroup(container);
        createReportsGroup(container);
        

        return area;
    }
    
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        
        setTitle("Export document ...");
        setMessage("Select Template and Report name", IMessageProvider.INFORMATION);      
        if (icon != null) super.setTitleImage(icon);
        
        return contents;
    } 
 
    private void createTemplatesGroup(Composite container) {   	
    // add reports UI to dialog
    	// set group 
    	templatesGroup = new Group(container, SWT.NULL);
    	templatesGroup.setText("Report Type");
    	templatesGroup.setLayout(new GridLayout(2, false));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 500;
        templatesGroup.setLayoutData(gd);
        
        // add drop down list
        templatesViewer = new ComboViewer(templatesGroup, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        //templatesList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        // allow this to display list of files names
        templatesViewer.setContentProvider(ArrayContentProvider.getInstance());
        templatesViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof File) {
                	// set file object chosen
                    File file = (File) element;
                    return file.getName();
                }
                return super.getText(element);
            }
        });
        
        // populate the content 
        if (populateTemplates()) {
        	templatesViewer.setInput(templateFiles);  // add contents
        	if (templateFile != null) {  // set default selection
        		ISelection defaultValue = new StructuredSelection(templateFile);
        		templatesViewer.setSelection(defaultValue);
        		bTemplateSelected = true;
        	}
        }
        else {
        	setMessage("Could not populate templates, set preferences", IMessageProvider.ERROR); 
        }
        
        
        // react to the selection change       
        templatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event
                    .getSelection();
                if (selection.size() > 0){
                	// process selection
                	templateFile = (File) selection.getFirstElement();
                	bTemplateSelected = true;
                    enableGenerateButton();
                	
                	String mesg = "Report file set to '"+ templateFile.getName()+"'";
                    setMessage(mesg, IMessageProvider.INFORMATION);            
                }
            }
        });
        
    }
    
    protected void enableGenerateButton() {
    // enable generate button after template has been selected.
    	if (bTemplateSelected) {
    		generateButton.setEnabled(true);
    	}
    }
	
    private void createReportsGroup(Composite container) {   
    // add reports UI to dialog	
    	// set group 
    	reportsGroup = new Group(container, SWT.NULL);
    	reportsGroup.setText("Destination File");
        reportsGroup.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 500;
        reportsGroup.setLayoutData(gd);
        
        // add text control
        reportsText = new Text(reportsGroup, SWT.BORDER | SWT.SINGLE);
        reportsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        // set text control value
        File exportFile = TorchLightPlugin.INSTANCE.getReportFile();
        if (exportFile != null) {
        	reportsText.setText(exportFile.getAbsolutePath());
        }
        
        // Single text control so strip CRLFs
        UIUtils.conformSingleTextControl(reportsText);
        
        // set button for Report destination
        reportsButton = new Button(reportsGroup, SWT.PUSH);
        reportsButton.setText("Browse..."); // choose ...
        
        // add listener for click browser button
        reportsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {       
            	File folderPath = askSaveFile();
                if(folderPath != null) {
                    reportsText.setText(folderPath.getAbsolutePath());
                    String mesg = "Report file set to '"+ folderPath.getName()+"'";
                    setMessage(mesg, IMessageProvider.INFORMATION);
                    TorchLightPlugin.INSTANCE.setReportFile(folderPath);
                }
            }
        });
    }
    
    // Lets user selects report folder path
    private File askSaveFile() {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setText(Messages.MyExporter_0);
        dialog.setFilterExtensions(new String[] { MY_EXTENSION_WILDCARD, "*.*" } ); //$NON-NLS-1$
        dialog.setFilterPath(TorchLightPlugin.INSTANCE.getUserReportsFolder().getAbsolutePath() );
        String path = dialog.open();
        if(path == null) {
            return null;
        }
        
        // Only Windows adds the extension by default
        if(dialog.getFilterIndex() == 0 && !path.endsWith(MY_EXTENSION)) {
            path += MY_EXTENSION;
        }
        
        File file = new File(path);
        
        // Make sure the file does not already exist
        if(file.exists()) {
            boolean result = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
                    Messages.MyExporter_0,
                    NLS.bind(Messages.MyExporter_1, file));
            if(!result) {
                return null;
            }
        }
        
        return file;
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    // generate button pressed
    private void generatePressed() {
    		
    	// Check template exists and is readable
        if( templateFile == null || !templateFile.isFile() ) {
        	String errorMsg = NLS.bind(Messages.TorchLightNoTemplate, templateFile);
        	MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.TorchLightFileError,  errorMsg);
        	setMessage(errorMsg, IMessageProvider.ERROR);
            return ;
        }
        
        // Get the export file from TextBox
        File xexportFile = new File(reportsText.getText());
        
        // Check report file
        boolean fileError = false;
        if (!xexportFile.exists()) {
    		try {
    			// error if it does not existing and cannot be created
    			fileError = !xexportFile.createNewFile();
    		}
    		catch (IOException ex) {
        		// need proper handler at some point
        	    System.err.println(ex);
        	    fileError = true;
        	}
		}
        else if (!xexportFile.canWrite()) {
        	// file exists but not writable
        	fileError = true;
        }
        
        if( fileError ) {
        	// blat out file error and exit
        	String errorMsg = NLS.bind(Messages.TorchLightBadFileName, xexportFile);
        	MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.TorchLightFileError,  errorMsg);
        	setMessage(errorMsg, IMessageProvider.ERROR);
        	return ;
        }
         
        // disable the GUI
    	generateButton.setEnabled(false);
    	closeButton.setEnabled(false);
    	generateButton.setText("Generating");
    	setMessage("Generating content...", IMessageProvider.INFORMATION);
        
        Runnable foo = new Runnable () {
            //@Override
            public void run() {
               try {
                	TorchlightReportExporter export = new TorchlightReportExporter(fModel, xexportFile, templateFile);
                	export.Run();
                	//System.out.println("result was"+ export.result());
                	if (export.error()) {
                		setMessage(export.result(), IMessageProvider.ERROR);
                	}
                	else {
                		setMessage(export.result(), IMessageProvider.INFORMATION);
                	}
                }
                catch(Exception ex) {
                   ex.printStackTrace();   
                   System.out.println(".....................exception................................");
                    MessageDialog.openError(getShell(), Messages.TorchLightExportError, ex.getMessage());
                }/*
                catch( XDocReportException e )
                {
                    // show dialog with template error
                	System.out.println("exception................................");
                    
                    //e.printStackTrace();
                }*/
            }
        };
        
        BusyIndicator.showWhile(null, foo);
        
        // complete export
        TorchLightPlugin.INSTANCE.setTemplateFile(templateFile);  // save the file
        TorchLightPlugin.INSTANCE.setReportFile(xexportFile);
        
        
        generateButton.setText("Generate Content");
    	generateButton.setEnabled(true);
    	closeButton.setEnabled(true);
					
    }
    //------------------------------------------------------------------------------------------------------------------
    
    // handle the close event
    private void closePressed() {
    	TorchLightPlugin.INSTANCE.setTemplateFile(templateFile);  // save the file
    	close();
    }
    
	// make the buttons generate and cancel
    protected void createButtonsForButtonBar(Composite parent) {
    	
    	// add cancel and generate button, disable generate button until content selected.
    	generateButton = createButton(parent, IDialogConstants.NO_ID, "Generate Content", true);
    	closeButton= createButton(parent, IDialogConstants.NO_ID, "Close", false);
    	
    	// enable button
    	generateButton.setEnabled(bTemplateSelected);
        
    	// listen for generate event
    	generateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	generatePressed();
            }
        });
    	
    	closeButton.addSelectionListener(new SelectionAdapter() {
    		@Override
            public void widgetSelected(SelectionEvent e) {
            	closePressed();
            }
    	});
    }
    
    
    // populates the list of templates in the templates directory
    public boolean populateTemplates() {
    	// get default location from Plugin preferences and convert to path
    	File tempFile = TorchLightPlugin.INSTANCE.getUserTemplatesFolder();
    	Path dir = Paths.get(tempFile.getAbsolutePath());
    	
    	String sPath;
    	File fPath;
    	boolean found = false;
    	
    	// only populate file that match filter
    	try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, FILTER)) {
    	    for (Path filePath: stream) {
    	        boolean isRegularFile = Files.isRegularFile(filePath) & Files.isReadable(filePath);
    	        if (isRegularFile) {
    	        	sPath = filePath.toString();
    	        	fPath = new File(sPath);
    	        	templateFiles.add(fPath);
    	        	if (templateFile != null) {
    	        		if (templateFile.compareTo(fPath) == 0)
    	        			found = true;
    	        	}
    	        }   	       
    	    }
    	    if (!found) templateFile = null;
    	    return true;
    	} catch (IOException | DirectoryIteratorException x) {
    	    // IOException can never be thrown by the iteration.
    	    // In this snippet, it can only be thrown by newDirectoryStream.
    		// need proper handler at some point
    	    System.err.println(x);
    	    return false;
    	}
    			
    }

    
    
}
