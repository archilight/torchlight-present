package com.torchlight.present;

/**
* This program and the accompanying materials
* are made available under the terms of the License
* which accompanies this distribution in the file LICENSE.txt
*/

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.archimatetool.editor.actions.AbstractModelSelectionHandler;
import com.archimatetool.model.IArchimateModel;



/**
* Command Torchlight command Handler for Torchlight Reports
* 
* @author Jeff Parker
*/
public class TorchlightPresentHandler extends AbstractModelSelectionHandler {
   
   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
       IArchimateModel model = getActiveArchimateModel();
       if(model != null) {
    	   MyExporter exporter = new MyExporter();
    	   exporter.export(model);
       }

       return null;
   }
   
}