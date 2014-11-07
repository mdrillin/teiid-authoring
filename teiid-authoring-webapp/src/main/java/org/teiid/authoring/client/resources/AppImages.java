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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * GWT managed images for Workbench
 */
public interface AppImages extends ClientBundle {

    @Source("images/teiid_user_logo.png")
    ImageResource ufUserLogo();
    
    @Source("images/EmptyLibraryImage.png")
    ImageResource emptyLibraryImage();
    
    @Source("images/spinner_16.gif")
    ImageResource spinnner16x16Image();
    
    @Source("images/spinner_24.gif")
    ImageResource spinnner24x24Image();
    
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
    
    @Source("images/dstype_blankbox_up.png")
    ImageResource dsType_blankbox_UpImage();
    @Source("images/dstype_blankbox_down.png")
    ImageResource dsType_blankbox_DownImage();

    @Source("images/dstype_google_up.png")
    ImageResource dsType_google_UpImage();
    @Source("images/dstype_google_down.png")
    ImageResource dsType_google_DownImage();

    @Source("images/dstype_h2_up.png")
    ImageResource dsType_h2_UpImage();
    @Source("images/dstype_h2_down.png")
    ImageResource dsType_h2_DownImage();

    @Source("images/dstype_infinispan_up.png")
    ImageResource dsType_infinispan_UpImage();
    @Source("images/dstype_infinispan_down.png")
    ImageResource dsType_infinispan_DownImage();

    @Source("images/dstype_modeshape_up.png")
    ImageResource dsType_modeshape_UpImage();
    @Source("images/dstype_modeshape_down.png")
    ImageResource dsType_modeshape_DownImage();

    @Source("images/dstype_mongodb_up.png")
    ImageResource dsType_mongodb_UpImage();
    @Source("images/dstype_mongodb_down.png")
    ImageResource dsType_mongodb_DownImage();

    @Source("images/dstype_mysql_up.png")
    ImageResource dsType_mysql_UpImage();
    @Source("images/dstype_mysql_down.png")
    ImageResource dsType_mysql_DownImage();

    @Source("images/dstype_postgres_up.png")
    ImageResource dsType_postgres_UpImage();
    @Source("images/dstype_postgres_down.png")
    ImageResource dsType_postgres_DownImage();

    @Source("images/dstype_salesforce_up.png")
    ImageResource dsType_salesforce_UpImage();
    @Source("images/dstype_salesforce_down.png")
    ImageResource dsType_salesforce_DownImage();

    @Source("images/dstype_teiid_up.png")
    ImageResource dsType_teiid_UpImage();
    @Source("images/dstype_teiid_down.png")
    ImageResource dsType_teiid_DownImage();

    @Source("images/dstype_teiid_local_up.png")
    ImageResource dsType_teiid_local_UpImage();
    @Source("images/dstype_teiid_local_down.png")
    ImageResource dsType_teiid_local_DownImage();
    
    @Source("images/dstype_ldap_up.png")
    ImageResource dsType_ldap_UpImage();
    @Source("images/dstype_ldap_down.png")
    ImageResource dsType_ldap_DownImage();
    
    @Source("images/dstype_file_up.png")
    ImageResource dsType_file_UpImage();
    @Source("images/dstype_file_down.png")
    ImageResource dsType_file_DownImage();
    
    @Source("images/dstype_webservice_up.png")
    ImageResource dsType_webservice_UpImage();
    @Source("images/dstype_webservice_down.png")
    ImageResource dsType_webservice_DownImage();
    
    @Source("images/dstype_accumulo_up.png")
    ImageResource dsType_accumulo_UpImage();
    @Source("images/dstype_accumulo_down.png")
    ImageResource dsType_accumulo_DownImage();

    @Source("images/dstype_addtype.png")
    ImageResource dsType_addtype_Image();

    @Source("images/dstype_blankbox_small.png")
    ImageResource dsType_blankbox_small_Image();

    @Source("images/dstype_google_small.png")
    ImageResource dsType_google_small_Image();

    @Source("images/dstype_h2_small.png")
    ImageResource dsType_h2_small_Image();

    @Source("images/dstype_infinispan_small.png")
    ImageResource dsType_infinispan_small_Image();

    @Source("images/dstype_modeshape_small.png")
    ImageResource dsType_modeshape_small_Image();

    @Source("images/dstype_mongodb_small.png")
    ImageResource dsType_mongodb_small_Image();

    @Source("images/dstype_mysql_small.png")
    ImageResource dsType_mysql_small_Image();

    @Source("images/dstype_postgres_small.png")
    ImageResource dsType_postgres_small_Image();

    @Source("images/dstype_salesforce_small.png")
    ImageResource dsType_salesforce_small_Image();

    @Source("images/dstype_teiid_small.png")
    ImageResource dsType_teiid_small_Image();
    
    @Source("images/dstype_ldap_small.png")
    ImageResource dsType_ldap_small_Image();
    
    @Source("images/dstype_file_small.png")
    ImageResource dsType_file_small_Image();
    
    @Source("images/dstype_webservice_small.png")
    ImageResource dsType_webservice_small_Image();
}
