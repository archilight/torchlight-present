/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.torchlight.present.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.archimatetool.editor.preferences.IPreferenceConstants;
import com.torchlight.present.TorchLightPlugin;



/**
 * Class used to initialize default preference values
 * 
 * @author Phillip Beauvoir modified by Jeff Parker
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
implements IPreferenceConstants, ITorchlightPreferenceConstants {

    @Override
    public void initializeDefaultPreferences() {
		IPreferenceStore store = TorchLightPlugin.INSTANCE.getPreferenceStore();
		store.setDefault(TORCHLIGHT_USER_TEMPLATES_FOLDER, TorchLightPlugin.INSTANCE.getDefaultUserTemplatesFolder().getAbsolutePath());
		store.setDefault(TORCHLIGHT_USER_REPORTS_OUT_FOLDER, TorchLightPlugin.INSTANCE.getDefaultUserReportsFolder().getAbsolutePath());
		store.setDefault(TORCHLIGHT_IMAGEWIDTH, TorchLightPlugin.INSTANCE.getDefaultImageWidth());
    }
}
