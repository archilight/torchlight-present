/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.torchlight.present;
 
import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.torchlight.present.messages"; //$NON-NLS-1$

    public static String MyExporter_0;
    public static String MyExporter_1;
    public static String TorchLightNoTemplate;
    public static String TorchLightFileError;
    public static String TorchLightExportError;
    public static String TorchLightBadFileName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
