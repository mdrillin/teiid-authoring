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
package org.teiid.authoring.share;



/**
 * Application constants
 * @author mdrillin@redhat.com
 */
public class Constants {

	public static final String WEBUI_USER = "webui";
	public static final String WEBUI_PASS = "webui";
	
	public static final String CREATE_DATA_SERVICE_SCREEN = "CreateDataServiceScreen";
	public static final String EDIT_DATA_SERVICE_SCREEN = "EditDataServiceScreen";
	public static final String DATA_SERVICE_DETAILS_SCREEN = "DataServiceDetailsScreen";
	public static final String DATA_SERVICES_LIBRARY_SCREEN = "DataServicesLibraryScreen";
	public static final String DATA_SERVICES_EMPTY_LIBRARY_SCREEN = "DataServicesEmptyLibraryScreen";
	public static final String MANAGE_SOURCES_SCREEN = "ManageSourcesScreen";
	public static final String UNKNOWN = "[unknown]";
	
	public static final int DATASOURCES_TABLE_PAGE_SIZE = 15;
	public static final int DATASOURCE_TYPES_TABLE_PAGE_SIZE = 15;
	public static final int VDBS_TABLE_PAGE_SIZE = 15;
	public static final int VDB_MODELS_TABLE_PAGE_SIZE = 15;
	public static final int QUERY_RESULTS_TABLE_PAGE_SIZE = 15;
	public static final int QUERY_COLUMNS_TABLE_PAGE_SIZE = 6;
	
	public static final String FROM_SCREEN = "from-screen";
	
	public static final String SERVICE_TEST_VDB_PREFIX = "SvcTestVdb_";
	public static final String SERVICE_SOURCE_VDB_PREFIX = "SvcSourceVdb_";
	public static final String PREVIEW_VDB_PREFIX = "PREVIEW_";
	
	public static final String JNDI_PREFIX = "java:/";
	public static final String JBOSS_JNDI_PREFIX = "java:jboss/datasources/";
	public static final String MODESHAPE_JNDI_PREFIX = "java:/datasources/";
	public static final String SERVICE_NAME_KEY = "service-name";
	public static final String SERVICE_VIEW_NAME = "SvcView";
	public static final String CLONE_SERVICE_KEY = "clone-service";
	public static final String DELETE_SERVICE_KEY = "delete-service";
	
	public static final String DATA_SOURCE_NEW_NAME = "MyNewSource";
	public static final String TEST_CONNECTION_FAILED = "Could not connect - please check source properties and server log, then enter desired properties";
    public static final String TEST_CONNECTION_ERROR_LOADING_SOURCES = "Error - could not load the available sources";

	public static final String SERVICE_NAME_CANNOT_BE_EMPTY_MSG = "The service name cannot be empty";
	public static final String SERVICE_NAME_FIRST_CHAR_MUST_BE_ALPHA_MSG = "The first character of the service name must be an alphabetic character";

	public static final String REST_PROCNAME = "RestProc";
    public static final String REST_XML_GROUPTAG = "Elems";
    public static final String REST_XML_ELEMENTTAG = "Elem";
    public static final String REST_URI_PROPERTY = "rest";

    public static final String VDB_PROP_KEY_REST_AUTOGEN = "{http://teiid.org/rest}auto-generate";
    public static final String VDB_PROP_KEY_DATASERVICE_VIEWNAME = "data-service-view";
    
    public static final String DS_TYPE_ACCUMULO = "accumulo";
    public static final String DS_TYPE_FILE = "file";
	public static final String DS_TYPE_GOOGLE = "google";
	public static final String DS_TYPE_H2 = "h2";
	public static final String DS_TYPE_INFINISPAN = "infinispan";
	public static final String DS_TYPE_LDAP = "ldap";
	public static final String DS_TYPE_MODESHAPE = "modeshape";
	public static final String DS_TYPE_MONGODB = "mongodb";
	public static final String DS_TYPE_SALESFORCE = "salesforce";
	public static final String DS_TYPE_TEIID = "teiid";
	public static final String DS_TYPE_TEIID_LOCAL = "teiid-local";
	public static final String DS_TYPE_WEBSERVICE = "webservice";
	
	public static final String MYSQL_FRAGMENT = "mysql";
	public static final String POSTGRES_FRAGMENT = "postgres";
	
	public static final String OPENSHIFT_HOST_PREFIX = "[OPENSHIFT]";
	
	public static final String OK = "OK";
	public static final String QUESTION_MARK = "?";
	public static final String DOT = ".";
	public static final String SELECT_STAR_FROM = "SELECT * FROM";
	public static final String LIMIT_10 = "LIMIT 10";
	public static final String SPACE = " ";
	public static final String BLANK = "";
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
}
