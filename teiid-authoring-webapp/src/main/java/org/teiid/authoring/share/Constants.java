/*
 * Copyright 2013 JBoss Inc
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
package org.teiid.authoring.share;

/**
 * Application constants
 * @author mdrillin@redhat.com
 */
public class Constants {

	public static final int DATASOURCES_TABLE_PAGE_SIZE = 15;
	public static final int DATASOURCE_TYPES_TABLE_PAGE_SIZE = 15;
	public static final int VDBS_TABLE_PAGE_SIZE = 15;
	public static final int VDB_MODELS_TABLE_PAGE_SIZE = 15;
	public static final int QUERY_RESULTS_TABLE_PAGE_SIZE = 15;
	public static final int QUERY_COLUMNS_TABLE_PAGE_SIZE = 6;
	
	public static final String SOURCE_VDB_PREFIX = "DataVirtUI";

	public static final String SERVICES_VDB = "ServicesVDB";
	public static final String SERVICES_VDB_JNDI = "java:/ServicesVDB";
	public static final String SERVICE_NAME_KEY = "service-name";
	public static final String SERVICE_VIEW_NAME = "SvcView";
	public static final String SERVICE_SOURCE_VDB_PREFIX = "SvcSrcVdb_";
	public static final String CLONE_SERVICE_KEY = "clone-service";
	public static final String DELETE_SERVICE_KEY = "delete-service";
	
	public static final String DATA_SOURCE_NEW_NAME = "MyNewSource";
	
	public static final String OK = "OK";
	public static final String QUESTION_MARK = "?";
	public static final String SUCCESS = "success";
	public static final String PHYSICAL = "PHYSICAL";
	public static final String VIRTUAL = "VIRTUAL";

	public static final String DYNAMIC_VDB_SUFFIX = "-vdb.xml";
	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_INACTIVE = "INACTIVE";
	public static final String STATUS_LOADING = "LOADING";
	public static final String STATUS_UNKNOWN = "Unknown";
	
	public static final int VDB_LOADING_TIMEOUT_SECS = 300;
	public static final String NO_TYPE_SELECTION = "[Select a Type]";
	public static final String NO_TRANSLATOR_SELECTION = "[Select a Translator]";

	public static final String SORT_COLID_NAME = "name"; //$NON-NLS-1$
    public static final String SORT_COLID_MODIFIED_ON = "lastModifiedTimestamp"; //$NON-NLS-1$

    public static final String VDB_STATUS_URL_ACTIVE_16PX = "images/StatusIcon_ok_16x16.png";
    public static final String VDB_STATUS_URL_INACTIVE_16PX = "images/StatusIcon_inactive_16x16.png";
    public static final String VDB_STATUS_URL_LOADING_16PX = "images/StatusIcon_loading_16x16.png";

    public static final String VDB_STATUS_URL_ACTIVE_32PX = "images/StatusIcon_ok_32x32.png";
    public static final String VDB_STATUS_URL_INACTIVE_32PX = "images/StatusIcon_inactive_32x32.png";
    public static final String VDB_STATUS_URL_LOADING_32PX = "images/StatusIcon_loading_32x32.png";
    
    public static final String VDBMODEL_STATUS_URL_INACTIVE_16PX = "images/StatusIcon_inactive_16x16.png";
    public static final String VDBMODEL_STATUS_URL_ACTIVE_16PX = "images/StatusIcon_ok_16x16.png";
    public static final String VDBMODEL_STATUS_URL_UNKNOWN_16PX = "images/StatusIcon_loading_16x16.png";

}
