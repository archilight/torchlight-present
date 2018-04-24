package org.apache.poi;

public class PptPoiBridge {
	public static void removeRelation(POIXMLDocumentPart parent, POIXMLDocumentPart child) {
		parent.removeRelation(child);
	}
}
