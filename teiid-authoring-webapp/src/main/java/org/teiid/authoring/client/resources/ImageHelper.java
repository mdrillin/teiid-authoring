/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.teiid.authoring.client.resources;

import org.teiid.authoring.share.Constants;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Contains methods for working with images
 */
public class ImageHelper {

	// ============================================
	// Static Variables

	private static ImageHelper instance = new ImageHelper();

	// ============================================
	// Static Methods
	/**
	 * Get the singleton instance
	 *
	 * @return instance
	 */
	public static ImageHelper getInstance() {
		return instance;
	}

	/*
	 * Create a VdbHelper
	 */
	private ImageHelper() {
	}

	/**
	 * Get the image resource for the provided datasource type
	 * @param dsType the dataSource type
	 * @param selected 'true' if image is 'selected'
	 * @return the image resource
	 */
    public ImageResource getDataSourceImageForType(String dsType,boolean selected) {
    	ImageResource img = null;
    	if(dsType.equals(Constants.DS_TYPE_FILE)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_file_DownImage()
    				         : AppResource.INSTANCE.images().dsType_file_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_GOOGLE)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_google_DownImage()
            	             : AppResource.INSTANCE.images().dsType_google_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_H2)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_h2_DownImage()
            	             : AppResource.INSTANCE.images().dsType_h2_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_INFINISPAN)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_infinispan_DownImage()
    	                     : AppResource.INSTANCE.images().dsType_infinispan_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_LDAP)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_ldap_DownImage()
    	                     : AppResource.INSTANCE.images().dsType_ldap_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_MODESHAPE)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_modeshape_DownImage()
    				         : AppResource.INSTANCE.images().dsType_modeshape_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_MONGODB)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_mongodb_DownImage()
    				         : AppResource.INSTANCE.images().dsType_mongodb_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_SALESFORCE)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_salesforce_DownImage()
    				         : AppResource.INSTANCE.images().dsType_salesforce_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_TEIID)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_teiid_DownImage()
    				         : AppResource.INSTANCE.images().dsType_teiid_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_TEIID_LOCAL)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_teiid_DownImage()
    				         : AppResource.INSTANCE.images().dsType_teiid_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_WEBSERVICE)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_webservice_DownImage()
    				         : AppResource.INSTANCE.images().dsType_webservice_UpImage();
    	} else if(dsType.equals(Constants.DS_TYPE_ACCUMULO)) {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_accumulo_DownImage()
    				         : AppResource.INSTANCE.images().dsType_accumulo_UpImage();
    	} else {
    		img = (selected) ? AppResource.INSTANCE.images().dsType_blankbox_DownImage()
    				         : AppResource.INSTANCE.images().dsType_blankbox_UpImage();
    	}
    	return img;
    }
    
	/**
	 * Determine if the type has a known image 
	 * @param dsType the dataSource type
	 * @return 'true' if a specific image is found, 'false' if not.
	 */
    public boolean hasKnownImage(String dsType) {
    	boolean hasImage = false;
    	if(dsType.equals(Constants.DS_TYPE_FILE) ||
    	   dsType.equals(Constants.DS_TYPE_GOOGLE) ||
    	   dsType.equals(Constants.DS_TYPE_H2) ||
    	   dsType.equals(Constants.DS_TYPE_INFINISPAN) ||
    	   dsType.equals(Constants.DS_TYPE_LDAP) ||
    	   dsType.equals(Constants.DS_TYPE_MODESHAPE) ||
    	   dsType.equals(Constants.DS_TYPE_MONGODB) ||
    	   dsType.equals(Constants.DS_TYPE_SALESFORCE) ||
    	   dsType.equals(Constants.DS_TYPE_TEIID) ||
    	   dsType.equals(Constants.DS_TYPE_TEIID_LOCAL) ||
    	   dsType.equals(Constants.DS_TYPE_WEBSERVICE)) {
    		hasImage = true;
    	} else {
    		hasImage = false;
    	}
    	return hasImage;
    }
    
	/**
	 * Get the image html string for the provided datasource type
	 * @param dsType the dataSource type
	 * @param selecte 'true' if the image is 'selected'
	 * @return the image html
	 */
    public String getDataSourceForTypeImageHtml(String dsType,boolean selected) {
    	String imageHtml = null;

    	if(dsType.equals(Constants.DS_TYPE_FILE)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_file_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_file_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_GOOGLE)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_google_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_google_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_H2)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_h2_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_h2_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_INFINISPAN)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_infinispan_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_infinispan_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_LDAP)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_ldap_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_ldap_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_MODESHAPE)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_modeshape_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_modeshape_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_MONGODB)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_mongodb_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_mongodb_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_SALESFORCE)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_salesforce_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_salesforce_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_TEIID)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_teiid_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_teiid_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_TEIID_LOCAL)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_teiid_local_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_teiid_local_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_WEBSERVICE)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_webservice_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_webservice_UpImage()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_ACCUMULO)) {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_accumulo_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_accumulo_UpImage()).getHTML();
    	} else {
    		imageHtml = (selected) ? AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_blankbox_DownImage()).getHTML()
    		                       : AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_blankbox_UpImage()).getHTML();
    	}
    	
    	return imageHtml;
    }
    
	/**
	 * Get the image html string for the provided datasource type
	 * @param dsType the dataSource type
	 * @return the image html
	 */
    public String getDataSourceForTypeSmallImageHtml(String dsType) {
    	String imageHtml = null;

    	if(dsType.equals(Constants.DS_TYPE_FILE)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_file_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_GOOGLE)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_google_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_H2)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_h2_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_INFINISPAN)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_infinispan_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_LDAP)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_ldap_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_MODESHAPE)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_modeshape_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_MONGODB)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_mongodb_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_SALESFORCE)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_salesforce_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_TEIID)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_teiid_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_TEIID_LOCAL)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_teiid_small_Image()).getHTML();
    	} else if(dsType.equals(Constants.DS_TYPE_WEBSERVICE)) {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_webservice_small_Image()).getHTML();
    	} else {
    		imageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().dsType_blankbox_small_Image()).getHTML();
    	}
    	
    	return imageHtml;
    }
    
}

