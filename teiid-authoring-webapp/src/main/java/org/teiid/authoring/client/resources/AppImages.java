/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.teiid.authoring.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * GWT managed images for Workbench
 */
public interface AppImages extends ClientBundle {

    @Source("images/teiid_user_logo.png")
    ImageResource ufUserLogo();
    
    @Source("images/EmptyLibraryImage.jpg")
    ImageResource emptyLibraryImage();
    
    @Source("images/Error_16x16.png")
    ImageResource errorIcon16x16Image();
    
    @Source("images/Error_32x32.png")
    ImageResource errorIcon32x32Image();
    
    @Source("images/Warning_16x16.png")
    ImageResource warningIcon16x16Image();
    
    @Source("images/Warning_32x32.png")
    ImageResource warningIcon32x32Image();
    
    @Source("images/Ok_16x16.png")
    ImageResource okIcon16x16Image();
    
    @Source("images/Ok_32x32.png")
    ImageResource okIcon32x32Image();
    
    @Source("images/dstype_blankbox.png")
    ImageResource dsType_blankbox_Image();

    @Source("images/dstype_google.png")
    ImageResource dsType_google_Image();

    @Source("images/dstype_h2.png")
    ImageResource dsType_h2_Image();

    @Source("images/dstype_infinispan.png")
    ImageResource dsType_infinispan_Image();

    @Source("images/dstype_modeshape.png")
    ImageResource dsType_modeshape_Image();

    @Source("images/dstype_mongodb.png")
    ImageResource dsType_mongodb_Image();

    @Source("images/dstype_mysql.png")
    ImageResource dsType_mysql_Image();

    @Source("images/dstype_postgres.png")
    ImageResource dsType_postgres_Image();

    @Source("images/dstype_salesforce.png")
    ImageResource dsType_salesforce_Image();

    @Source("images/dstype_teiid.png")
    ImageResource dsType_teiid_Image();
    
    @Source("images/dstype_ldap.png")
    ImageResource dsType_ldap_Image();
    
    @Source("images/dstype_file.png")
    ImageResource dsType_file_Image();
    
    @Source("images/dstype_webservice.png")
    ImageResource dsType_webservice_Image();

}
