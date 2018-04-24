package com.torchlight.present;
/*
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
/**
 * Holder for Torchlight tree objects
 * @author ParkerJ
 * 
 * See Licence
 *
 */
 

public class MyObject implements Comparable<MyObject> {
	private EObject object;
	private String name;
	private List <MyObject> children;
	private Boolean bFolder; 
	
	public MyObject(EObject object, String name, Boolean bFolder) {
		this.object = object;
		this.name = name;
		this.setChildren(new ArrayList <MyObject>());
		this.bFolder = bFolder;
	}
	
	public EObject getObject() {
		return this.object;
	}
	

	@Override
	public int compareTo(MyObject arg0) {
		// call by sort eventually
		String objectname1 = arg0.name.toUpperCase();
		String objectname2 = this.name.toUpperCase();
		//ascending order
		return objectname2.compareTo(objectname1); // use string comparator

	}

	public String getName() {
		return name;
	}
	
	public List <MyObject> getChildren() {
		return children;
	}

	public void setChildren(List <MyObject> children) {
		this.children = children;
	}
	
	public void addChild(MyObject child) {
		this.children.add(child);
	}


	public Boolean getbFolder() {
		return bFolder;
	}

	public void setbFolder(Boolean bFolder) {
		this.bFolder = bFolder;
	}
	
	public void mysort() {
		Collections.sort(children);
	}
}
