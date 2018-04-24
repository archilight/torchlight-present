/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.torchlight.present.preferences;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.ui.UIUtils;
import com.torchlight.present.TorchLightPlugin;


/**
 * Canvas Preferences Page
 * 
 * @author Phillip Beauvoir adapted for Torchlight by Jeff Parker
 */
public class TorchlightPresentPreferencesPage
extends PreferencePage
implements IWorkbenchPreferencePage, ITorchlightPreferenceConstants {
    
    private static String HELP_ID = "com.torchlight.present.help"; //$NON-NLS-1$
    
    private Text fUserReportsFolderTextField, fUserReportsOutFolderTextField;
    private Spinner fUserImageWidthFloatField;
    
	public void TorchlightReportsPreferencesPage() {
		setPreferenceStore(TorchLightPlugin.INSTANCE.getPreferenceStore());
	}
	
    @Override
    protected Control createContents(Composite parent) {
        // Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, HELP_ID);

        Composite client = new Composite(parent, SWT.NULL);
        client.setLayout(new GridLayout());
                
        Group settingsGroup = new Group(client, SWT.NULL);
        settingsGroup.setText(Messages.TorchlightReportsPreferencesPage_0); // Settings
        settingsGroup.setLayout(new GridLayout(3, false));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 500;
        settingsGroup.setLayoutData(gd);
        
        // Text input Template location ---------------------------------------------------------------------  
        
        // set label
        Label label = new Label(settingsGroup, SWT.NULL);
        label.setText(Messages.TorchlightReportsPreferencesPage_1); // User Templates location
        
        fUserReportsFolderTextField = new Text(settingsGroup, SWT.BORDER | SWT.SINGLE);
        fUserReportsFolderTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
        // Single text control so strip CRLFs
        UIUtils.conformSingleTextControl(fUserReportsFolderTextField);
        
        
        // set button for FolderTemplates
        Button folderButton = new Button(settingsGroup, SWT.PUSH);
        folderButton.setText(Messages.TorchlightReportsPreferencesPage_2); // choose ...
        folderButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String folderPath = chooseTemplateFolderPath();
                if(folderPath != null) {
                    fUserReportsFolderTextField.setText(folderPath);
                }
            }
        });
        
        // Text input Output location ---------------------------------------------------------------------   
        // set label
        Label label2 = new Label(settingsGroup, SWT.NULL);
        label2.setText(Messages.TorchlightReportsPreferencesPage_5); // User Templates location
        
        fUserReportsOutFolderTextField = new Text(settingsGroup, SWT.BORDER | SWT.SINGLE);
        fUserReportsOutFolderTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        // Single text control so strip CRLFs
        UIUtils.conformSingleTextControl(fUserReportsOutFolderTextField);
        
        // set button for FolderTemplates
        Button folderButton2 = new Button(settingsGroup, SWT.PUSH);
        folderButton2.setText(Messages.TorchlightReportsPreferencesPage_2); // choose ...
        folderButton2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String folderPath = chooseOutputFolderPath();
                if(folderPath != null) {
                    fUserReportsOutFolderTextField.setText(folderPath);
                }
            }
        });
        
        // Float input for DefaultWidth -------------------------------------------------------------------
        Label label3 = new Label(settingsGroup, SWT.NULL);
        label3.setText("Image Width");     
        fUserImageWidthFloatField = new Spinner(settingsGroup, SWT.BORDER);
        
        // set default values
        setValues();
        
        return client;
    }

    private String chooseTemplateFolderPath() {
        DirectoryDialog dialog = new DirectoryDialog(Display.getCurrent().getActiveShell());
        dialog.setText(Messages.TorchlightReportsPreferencesPage_3);
        dialog.setMessage(Messages.TorchlightReportsPreferencesPage_4);
        File file = new File(fUserReportsFolderTextField.getText());
        if(file.exists()) {
            dialog.setFilterPath(fUserReportsFolderTextField.getText());
        }
        return dialog.open();
    }

    private String chooseOutputFolderPath() {
        DirectoryDialog dialog = new DirectoryDialog(Display.getCurrent().getActiveShell());
        dialog.setText(Messages.TorchlightReportsPreferencesPage_5);
        dialog.setMessage(Messages.TorchlightReportsPreferencesPage_7);
        File file = new File(fUserReportsOutFolderTextField.getText());
        if(file.exists()) {
            dialog.setFilterPath(fUserReportsOutFolderTextField.getText());
        }
        return dialog.open();
    }
    
    private void setValues() {
        fUserReportsFolderTextField.setText(TorchLightPlugin.INSTANCE.getUserTemplatesFolder().getAbsolutePath());
        fUserReportsOutFolderTextField.setText(TorchLightPlugin.INSTANCE.getUserReportsFolder().getAbsolutePath());
        
        int imageWidth = Math.round(TorchLightPlugin.INSTANCE.getImageWidth());
        fUserImageWidthFloatField.setValues(imageWidth, 0, 1000, 0, 1, 10);
    }
    
    @Override
    public boolean performOk() {
		IPreferenceStore store = TorchLightPlugin.INSTANCE.getPreferenceStore();
        int width = fUserImageWidthFloatField.getSelection();
        store.setValue(TORCHLIGHT_IMAGEWIDTH,(float) width);
        store.setValue(TORCHLIGHT_USER_TEMPLATES_FOLDER, fUserReportsFolderTextField.getText());
        store.setValue(TORCHLIGHT_USER_REPORTS_OUT_FOLDER, fUserReportsOutFolderTextField.getText());
        return true;
    }
    
    @Override
    protected void performDefaults() {
        fUserReportsFolderTextField.setText(TorchLightPlugin.INSTANCE.getDefaultUserTemplatesFolder().getAbsolutePath());
        fUserReportsOutFolderTextField.setText(TorchLightPlugin.INSTANCE.getDefaultUserReportsFolder().getAbsolutePath());
        
        int imageWidth = Math.round(TorchLightPlugin.INSTANCE.getDefaultImageWidth());
        fUserImageWidthFloatField.setValues(imageWidth, 1, 1000, 0, 1, 10);
        super.performDefaults();
    }
    
    public void init(IWorkbench workbench) {
    }
}