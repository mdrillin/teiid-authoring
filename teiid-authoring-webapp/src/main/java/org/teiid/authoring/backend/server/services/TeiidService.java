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
package org.teiid.authoring.backend.server.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.errai.bus.server.annotations.Service;
import org.teiid.adminapi.PropertyDefinition;
import org.teiid.adminapi.VDB;
import org.teiid.adminapi.VDB.Status;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.authoring.backend.server.api.AdminApiClientAccessor;
import org.teiid.authoring.backend.server.services.util.JdbcSourceHelper;
import org.teiid.authoring.backend.server.services.util.VdbHelper;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.TranslatorHelper;
import org.teiid.authoring.share.beans.DataSourceDetailsBean;
import org.teiid.authoring.share.beans.DataSourcePageRow;
import org.teiid.authoring.share.beans.DataSourcePropertyBean;
import org.teiid.authoring.share.beans.DataSourceWithVdbDetailsBean;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.VdbModelBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.teiid.authoring.share.exceptions.DataVirtUiException;
import org.teiid.authoring.share.services.ITeiidService;
import org.teiid.authoring.share.services.StringUtils;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

/**
 * Concrete implementation of the Teiid service.  This service is used to interact with the teiid server
 * through the teiid admin api
 *
 * @author mdrillin@redhat.com
 */
@Service
public class TeiidService implements ITeiidService {

    private static final String DRIVER_KEY = "driver-name";
    private static final String CLASSNAME_KEY = "class-name";
    private static final String CONN_FACTORY_CLASS_KEY = "managedconnectionfactory-class";
    private static final String CONNECTION_URL_DISPLAYNAME = "connection-url";

    private static final String LOCALHOST = "127.0.0.1";

    private VdbHelper vdbHelper = VdbHelper.getInstance();
    
    @Inject
    private AdminApiClientAccessor clientAccessor;
 
    @Inject
    private QueryService queryService;

    /**
     * Constructor.
     */
    public TeiidService() {
    }

    public PageResponse<DataSourcePageRow> getDataSources( final PageRequest pageRequest, final String filters ) {

    	List<String> filteredDsList = new ArrayList<String>();
		try {
			List<String> allDSList = getDataSourceNames();
			for(String sourceName : allDSList) {
				if(sourceName!=null && !sourceName.isEmpty() && !sourceName.startsWith(Constants.PREVIEW_VDB_PREFIX)) {
					filteredDsList.add(sourceName);
				}
			}
			// Sort the list
			Collections.sort(filteredDsList);
		} catch (DataVirtUiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> typeList = new ArrayList<String>(filteredDsList.size());
		for(String sourceName : filteredDsList) {
			// Get Data Source properties
			Properties dsProps = null;
			try {
				dsProps = clientAccessor.getClient().getDataSourceProperties(sourceName);
			} catch (AdminApiClientException e) {
			}

			// Determine type/driver from properties
			String dsType = getDataSourceType(dsProps);
			typeList.add(dsType);
		}

    	
    	final PageResponse<DataSourcePageRow> response = new PageResponse<DataSourcePageRow>();
    	final List<DataSourcePageRow> resultDSPageRowList = new ArrayList<DataSourcePageRow>();

    	int i = 0;
    	for ( String dsName : filteredDsList ) {
    		if ( i >= pageRequest.getStartRowIndex() + pageRequest.getPageSize() ) {
    			break;
    		}
    		if ( i >= pageRequest.getStartRowIndex() ) {
    			DataSourcePageRow dataSourcePageRow = new DataSourcePageRow();
    			dataSourcePageRow.setName( dsName );
    			dataSourcePageRow.setType(typeList.get(i));
    			resultDSPageRowList.add( dataSourcePageRow );
    		}
    		i++;
    	}

    	response.setPageRowList( resultDSPageRowList );
    	response.setStartRowIndex( pageRequest.getStartRowIndex() );
    	response.setTotalRowSize( filteredDsList.size() );
    	response.setTotalRowSizeExact( true );
    	//response.setLastPage(true);

    	return response;
    }
    
    /**
     * Find all of the 'raw' server sources (not preview sources).  For each source, get the type.
     * Also, check for a corresponding VDB with the supplied prefix.  If found, set the VDB flag and translator
     * @param filters filter string
     * @param srcVdbPrefix source VDB prefix for the corresponding src vdb
     */
    public List<DataSourcePageRow> getDataSources( final String filters, final String srcVdbPrefix ) {

    	// Get list of all Server Sources (except preview vdb and test VDB sources)
    	List<String> filteredDsList = new ArrayList<String>();
		try {
			List<String> allDSList = getDataSourceNames();
			for(String sourceName : allDSList) {
				if(!StringUtils.isEmpty(sourceName) && 
				   !sourceName.startsWith(Constants.PREVIEW_VDB_PREFIX) &&
				   !sourceName.startsWith(Constants.SERVICE_TEST_VDB_PREFIX) ) {
					filteredDsList.add(sourceName);
				}
			}
			// Sort the list
			Collections.sort(filteredDsList);
		} catch (DataVirtUiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Get types corresponding to the sources
		List<String> typeList = new ArrayList<String>(filteredDsList.size());
		List<Boolean> hasSrcVdbList = new ArrayList<Boolean>(filteredDsList.size());
		for(String sourceName : filteredDsList) {
			// Get Data Source properties
			Properties dsProps = null;
			try {
				dsProps = clientAccessor.getClient().getDataSourceProperties(sourceName);
			} catch (AdminApiClientException e) {
			}

			// Determine type/driver from properties
			String dsType = getDataSourceType(dsProps);
			typeList.add(dsType);
			
			// Determine type/driver from properties
			boolean hasSrcVdb = hasSourceVdb(sourceName,srcVdbPrefix,filteredDsList);
			hasSrcVdbList.add(Boolean.valueOf(hasSrcVdb));
		}

		// Create the result list
    	List<DataSourcePageRow> resultDSPageRowList = new ArrayList<DataSourcePageRow>();
    	int i = 0;
    	for ( String dsName : filteredDsList ) {
    		DataSourcePageRow dataSourcePageRow = new DataSourcePageRow();
    		dataSourcePageRow.setName( dsName );
    		dataSourcePageRow.setType(typeList.get(i));
    		// If DataSource has a corresponding VDB Source, check connection to the VDB Source
    		if(hasSrcVdbList.get(i)) {
    			String vdbSource = srcVdbPrefix+dsName;
    			String connectionStatus = testConnection(vdbSource);
    			if(!connectionStatus.equals(Constants.OK)) {
    				dataSourcePageRow.setState(DataSourcePageRow.State.ERROR);
    				dataSourcePageRow.setErrorMessage(connectionStatus);
    			} else {
    				dataSourcePageRow.setState(DataSourcePageRow.State.OK);
    			}
    		// If DataSource has no corresponding source, the propsPanel will show 'no translator' error
    		} else {
    			dataSourcePageRow.setState(DataSourcePageRow.State.ERROR);
    		}
    		resultDSPageRowList.add( dataSourcePageRow );
    		i++;
    	}
    	
    	return resultDSPageRowList;
    }
    
    private String testConnection(String sourceName) {
    	String sourceJndiName = Constants.JNDI_PREFIX+sourceName;
    	return queryService.testConnection(sourceJndiName,sourceName);
    }
    
    private boolean hasSourceVdb(String dsName, String vdbPrefix, List<String> allDsNames) {
    	String srcVdbName = vdbPrefix + dsName;
    	return allDsNames.contains(srcVdbName);
    }
        
    @Override
    public DataSourceWithVdbDetailsBean getDataSourceWithVdbDetails(String dsName) throws DataVirtUiException {
    	String srcVdbName = Constants.SERVICE_SOURCE_VDB_PREFIX+dsName;
    	
    	// Create DataSource Details Bean - set name
    	DataSourceWithVdbDetailsBean dsWithVdbDetailsBean = new DataSourceWithVdbDetailsBean();
    	dsWithVdbDetailsBean.setName(dsName);

    	// Get Data Source properties
    	Properties dsProps = null;
    	try {
			dsProps = clientAccessor.getClient().getDataSourceProperties(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}

    	// Set jndi name
    	dsWithVdbDetailsBean.setJndiName(dsProps.getProperty("jndi-name"));

    	// Determine type/driver from properties
    	String dsType = getDataSourceType(dsProps);
    	dsWithVdbDetailsBean.setType(dsType);
    	
    	// Get the Default Properties for the DS type
    	List<DataSourcePropertyBean> dataSourcePropertyBeans = getDataSourceTypeProperties(dsType);
    	    	
        // Set DS type default property to data source specific value
        for(DataSourcePropertyBean propBean: dataSourcePropertyBeans) {
            String propName = propBean.getName();
            String propValue = dsProps.getProperty(propName);
            if(dsProps.containsKey(propName)) {
                propValue = dsProps.getProperty(propName);
                if(propValue!=null) {
                	propBean.setValue(propValue);
                	propBean.setOriginalValue(propValue);
                }
            }
        }
        dsWithVdbDetailsBean.setProperties(dataSourcePropertyBeans);
        
        // Set the translator for the corresponding Src VDB (if it exists)
        dsWithVdbDetailsBean.setTranslator(getTranslatorForSrcVdb(srcVdbName));
        dsWithVdbDetailsBean.setSourceVdbName(srcVdbName);

    	return dsWithVdbDetailsBean;
    }
    
    private String getTranslatorForSrcVdb(String srcVdbName) throws DataVirtUiException {
    	String translator = null;
    	
    	VDBMetaData vdb = null;
    	try {
    		vdb = clientAccessor.getClient().getVDB(srcVdbName,1);
    	} catch (Exception e) {
    		throw new DataVirtUiException(e.getMessage());
    	}

    	// Details for this VDB
    	VdbDetailsBean vdbDetailsBean = VdbHelper.getInstance().getVdbDetails(vdb);
    	// The modelName in VDB is same as VDB, but without the prefix
    	String physModelName = srcVdbName.substring(srcVdbName.indexOf(Constants.SERVICE_SOURCE_VDB_PREFIX)+Constants.SERVICE_SOURCE_VDB_PREFIX.length());

    	// Get source models from VDB, find matching model and get translator
    	Collection<VdbModelBean> vdbModels = vdbDetailsBean.getModels();
    	for(VdbModelBean vdbModel : vdbModels) {
    		String modelName = vdbModel.getName();
    		if(modelName.equals(physModelName)) {
    			translator = vdbModel.getTranslator();
    			break;
    		}
    	}

        return translator;
    }

    /**
     * Gets the current DataSources
     * @throws DataVirtUiException
     */
    public List<String> getDataSourceNames( ) throws DataVirtUiException {
    	List<String> dsList = new ArrayList<String>();
    	
		Collection<String> sourceNames = null;
    	try {
    		sourceNames = clientAccessor.getClient().getDataSourceNames();
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(sourceNames==null || sourceNames.isEmpty()) {
    		return Collections.emptyList();
    	}
    	
    	dsList.addAll(sourceNames);    	
    	// Alphabetically sort the list
    	Collections.sort(dsList);

    	return dsList;    	
   }
    
    /**
     * Gets the 'testable' DataSources - those that are jdbc sources
     * @throws DataVirtUiException
     */
//    public Map<String,String> getQueryableDataSourceMap( ) throws DataVirtUiException {
//        Collection<Properties> dsSummaryPropsCollection = null;
//        try {
//        	dsSummaryPropsCollection = clientAccessor.getClient().getDataSourceSummaryPropsCollection();
//		} catch (AdminApiClientException e) {
//			throw new DataVirtUiException(e.getMessage());
//		}
//        
//        // Create a Map of *all* Datasources and their jndi names
//        Map<String,String> allSourcesToJndiMap = new HashMap<String,String>();
//        for(Properties dsProps : dsSummaryPropsCollection) {
//            String sourceName = dsProps.getProperty("name");
//            String jndiName = dsProps.getProperty("jndi-name");
//            if( !StringUtils.isEmpty(sourceName) && !StringUtils.isEmpty(sourceName) ) {
//            	allSourcesToJndiMap.put(sourceName, jndiName);
//            }
//        }
//        
//        // Gets jdbc Jndi names available on the server
//        List<String> jdbcJndiNames = JdbcSourceHelper.getInstance().getJdbcSourceNames(false);
//        
//        Map<String,String> resultMap = new HashMap<String,String>();
//        for(String allDsName : allSourcesToJndiMap.keySet()) {
//        	if(jdbcJndiNames.contains(allSourcesToJndiMap.get(allDsName))) {
//        		resultMap.put(allDsName,allSourcesToJndiMap.get(allDsName));
//        	}
//        }
//        
//        return resultMap;
//    }
    /**
     * Gets the current Translators
     * @throws DataVirtUiException
     */
    public List<String> getTranslators( ) throws DataVirtUiException {
    	List<String> resultList = new ArrayList<String>();
    	
		Collection<String> translatorNames = null;
    	try {
    		translatorNames = clientAccessor.getClient().getTranslatorNames();
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(translatorNames==null || translatorNames.isEmpty()) {
    		return Collections.emptyList();
    	}
    	
    	resultList.addAll(translatorNames);
    	// Alphabetically sort the list
    	Collections.sort(resultList);

    	return resultList;    	
    }
    
    public Map<String,String> getDefaultTranslatorMap() throws DataVirtUiException {
		Map<String,String> mappings = null;
    	try {
    		mappings = clientAccessor.getClient().getDefaultTranslatorMap();
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(mappings==null || mappings.isEmpty()) {
    		return Collections.emptyMap();
    	}
    	return mappings;
    }

    public List<String> getDataSourceTypes() throws DataVirtUiException {
    	List<String> dsTypeList = new ArrayList<String>();
    	    	
		Collection<String> dsTypes = null;
    	try {
    		dsTypes = clientAccessor.getClient().getDataSourceTypes();
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(dsTypes==null || dsTypes.isEmpty()) {
    		return Collections.emptyList();
    	}
    	
    	// Filter out 'types' ending with .war
    	for(String dsType : dsTypes) {
    	   if(dsType!=null && !dsType.endsWith(".war")) {
    		   dsTypeList.add(dsType);
    	   }
    	}
    	
    	// Alphabetically sort the list
    	Collections.sort(dsTypeList);

    	return dsTypeList;
    }
    
    public List<DataSourcePropertyBean> getDataSourceTypeProperties(String typeName) throws DataVirtUiException {
    	List<DataSourcePropertyBean> propertyDefnList = new ArrayList<DataSourcePropertyBean>();
    	
		Collection<? extends PropertyDefinition> propDefnList = null;
    	try {
    		propDefnList = clientAccessor.getClient().getDataSourceTypePropertyDefns(typeName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(propDefnList==null || propDefnList.isEmpty()) {
    		return Collections.emptyList();
    	}
    	
		// Get the Managed connection factory class for rars
		String rarConnFactoryValue = null;
		if(isRarDriver(typeName)) {
			rarConnFactoryValue = getManagedConnectionFactoryClassDefault(propDefnList);
		}
    	
		for(PropertyDefinition propDefn: propDefnList) {
			DataSourcePropertyBean propBean = new DataSourcePropertyBean();
			
			// ------------------------
			// Set PropertyObj fields
			// ------------------------
			// Name
			String name = propDefn.getName();
			propBean.setName(name);
			// DisplayName
			String displayName = propDefn.getDisplayName();
			propBean.setDisplayName(displayName);
			// isModifiable
			boolean isModifiable = propDefn.isModifiable();
			propBean.setModifiable(isModifiable);
			// isRequired
			boolean isRequired = propDefn.isRequired();
			propBean.setRequired(isRequired);
			// isMasked
			boolean isMasked = propDefn.isMasked();
			propBean.setMasked(isMasked);
			// defaultValue
			Object defaultValue = propDefn.getDefaultValue();
			if(defaultValue!=null) {
				propBean.setDefaultValue(defaultValue.toString());
			}
			// Set the value and original Value
			if(defaultValue!=null) {
				propBean.setValue(defaultValue.toString());
				propBean.setOriginalValue(defaultValue.toString());
				// Set Connection URL to template if available and value was null
			} else if(displayName.equalsIgnoreCase(CONNECTION_URL_DISPLAYNAME)) {
				String urlTemplate = TranslatorHelper.getUrlTemplate(typeName);
				if(!StringUtils.isEmpty(urlTemplate)) {
					propBean.setValue(urlTemplate);
					propBean.setOriginalValue(urlTemplate);
				}
			}

			// Copy the 'managedconnectionfactory-class' default value into the 'class-name' default value
			if(name.equals(CLASSNAME_KEY)) {
				propBean.setDefaultValue(rarConnFactoryValue);
				propBean.setValue(rarConnFactoryValue);
				propBean.setOriginalValue(rarConnFactoryValue);
				propBean.setRequired(true);
			}

			// ------------------------
			// Add PropertyObj to List
			// ------------------------
			propertyDefnList.add(propBean);
		}

    	return propertyDefnList;
    } 
    
    /**
     * Determine if this is a 'rar' type driver that is deployed with Teiid
     * @param driverName the name of the driver
     * @return 'true' if the driver is a rar driver, 'false' if not.
     */
    private boolean isRarDriver(String driverName) {
    	boolean isRarDriver = false;
    	if(!StringUtils.isEmpty(driverName)) {
    		if( driverName.equals(TranslatorHelper.TEIID_FILE_DRIVER) || driverName.equals(TranslatorHelper.TEIID_GOOGLE_DRIVER)
    				|| driverName.equals(TranslatorHelper.TEIID_INFINISPAN_DRIVER) || driverName.equals(TranslatorHelper.TEIID_LDAP_DRIVER)
    				|| driverName.equals(TranslatorHelper.TEIID_MONGODB_DRIVER) || driverName.equals(TranslatorHelper.TEIID_SALESORCE_DRIVER)
    				|| driverName.equals(TranslatorHelper.TEIID_WEBSERVICE_DRIVER)) {
    			isRarDriver = true;
    		}
    	}

    	return isRarDriver;
    }
        
    /*
     * Get the default value for the Managed ConnectionFactory class
     * @param propDefns the collection of property definitions
     * @return default value of the ManagedConnectionFactory, null if not found.
     */
    private String getManagedConnectionFactoryClassDefault (Collection<? extends PropertyDefinition> propDefns) {
    	String resultValue = null;
    	for(PropertyDefinition pDefn : propDefns) {
    		if(pDefn.getName().equalsIgnoreCase(CONN_FACTORY_CLASS_KEY)) {
    			resultValue=(String)pDefn.getDefaultValue();
    			break;
    		}
    	}
    	return resultValue;
    }

    
    /**
     * Get the Driver name for the supplied DataSource name - from the TeiidServer
     * @param dsName the data source name
     * @return the dataSource driver name
     */
    public String getDataSourceType(String dsName) throws DataVirtUiException {
    	Properties dsProps = null;
    	try {
			dsProps = clientAccessor.getClient().getDataSourceProperties(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	return getDataSourceType(dsProps);
    }

    /**
     * Get the Driver name for the supplied DataSource name - from the TeiidServer
     * @param dsProps the data source properties
     * @return the dataSource driver name
     */
    private String getDataSourceType(Properties dsProps) {
    	if(dsProps==null) return Constants.STATUS_UNKNOWN;

    	String driverName = dsProps.getProperty(DRIVER_KEY);
    	// If driver-name not found, look for class name and match up the .rar
    	if(driverName==null || driverName.trim().length()==0) {
    		String className = dsProps.getProperty(CLASSNAME_KEY);
    		if(className!=null && className.trim().length()!=0) {
    			driverName = TranslatorHelper.getDriverNameForClass(className);
    		}
    	}
    	return driverName;
    }
         
    public void deleteDataSource(String dsName) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteDataSource(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }
    
    public void createDataSource(DataSourceDetailsBean bean) throws DataVirtUiException {
    	// First delete the source with this name, if it already exists
    	deleteDataSource(bean.getName());
    	
    	List<DataSourcePropertyBean> dsPropBeans = bean.getProperties();
    	Properties dsProps = new Properties();
    	for(DataSourcePropertyBean dsPropBean : dsPropBeans) {
    		dsProps.put(dsPropBean.getName(),dsPropBean.getValue());
    	}
    	try {
			clientAccessor.getClient().createDataSource(bean.getName(), bean.getType(), dsProps);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}    	
    }
    
    public void createDataSourceWithVdb(DataSourceWithVdbDetailsBean bean) throws DataVirtUiException {
    	// First delete the server source and corresponding vdb source, if they exist
    	deleteDataSource(bean.getName());
    	
    	// Create the 'Raw' Server source with connection properties
    	List<DataSourcePropertyBean> dsPropBeans = bean.getProperties();
    	Properties dsProps = new Properties();
    	for(DataSourcePropertyBean dsPropBean : dsPropBeans) {
    		dsProps.put(dsPropBean.getName(),dsPropBean.getValue());
    	}
    	try {
			clientAccessor.getClient().createDataSource(bean.getName(), bean.getType(), dsProps);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}  
    	
    	// Get JNDI for the specified DataSource name.  if null choose a default
    	String jndiName = getSourceJndiName(bean.getName());
    	if(StringUtils.isEmpty(jndiName)) {
    		jndiName = "java:/"+bean.getName();
    	}
    	
    	// Delete Source VDB if it already exists
    	List<String> vdbsToDelete = new ArrayList<String>(1);
    	vdbsToDelete.add(bean.getSourceVdbName());
    	deleteVdbs(vdbsToDelete);
    	// Create the corresponding SrcVdb for the new source
    	deploySourceVDB(bean.getSourceVdbName(), bean.getName(), bean.getName(), jndiName, bean.getTranslator());
    	
    	// Create the teiid dataSource for the deployed source VDB
    	createVdbDataSource(bean.getSourceVdbName());
    }
    
    /**
     * Create a VDB and its corresponding teiid source
     * @param bean the DataSource and VDB details
     * @throws DataVirtUiException
     */
    public void createVdbAndVdbSource(DataSourceWithVdbDetailsBean bean) throws DataVirtUiException {
    	// Get JNDI for the specified DataSource name.  if null choose a default
    	String jndiName = getSourceJndiName(bean.getName());
    	if(StringUtils.isEmpty(jndiName)) {
    		jndiName = "java:/"+bean.getName();
    	}
    	
    	// Delete VDB if it already exists
    	deleteVdb(bean.getSourceVdbName());
    	
    	// Deploy the VDB
    	deploySourceVDB(bean.getSourceVdbName(), bean.getName(), bean.getName(), jndiName, bean.getTranslator());
    	
    	// Create the teiid dataSource for the deployed source VDB
    	createVdbDataSource(bean.getSourceVdbName());
    }
    
    private void deleteVdbs(Collection<String> vdbNames) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteVDBs(vdbNames);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }
    
    private void deleteVdb(String vdbName) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteVDB(vdbName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }
        
    /**
     * Deploys a SourceVDB for the specified dataSource, if it doesnt already exist
     * @param sourceVDBName the name of the source VDB
     * @param modelName the name of the model
     * @param dataSourceName the name of the datasource
     * @param jndiName the JNDI name of the datasource
     * @param translator the name of the translator
     */
    private String deploySourceVDB(String sourceVDBName, String modelName, String dataSourceName, String jndiName, String translator) throws DataVirtUiException {
    	try {
        	// Get VDB with the supplied name.
        	// -- If it already exists, return its status
        	VDBMetaData sourceVdb = clientAccessor.getClient().getVDB(sourceVDBName,1);

        	if(sourceVdb!=null) {
        		String sourceVdbStatus = getVDBStatusMessage(sourceVDBName);
        		if(!sourceVdbStatus.equals(Constants.SUCCESS)) {
        			return sourceVdbStatus;
        		}
        		return sourceVdbStatus;
        	}
        	
        	// Deployment name for vdb must end in '-vdb.xml'.
        	String deploymentName = sourceVDBName + Constants.DYNAMIC_VDB_SUFFIX;

        	// Create a new Source VDB to deploy
        	sourceVdb = vdbHelper.createVdb(sourceVDBName,1);

        	// Create source model - same name as dataSource.  Use model name for source mapping - will be unique
        	ModelMetaData model = vdbHelper.createSourceModel(modelName, modelName, jndiName, translator);

        	// Adding the SourceModel to the VDB
        	sourceVdb.addModel(model);

        	// If it exists, undeploy it
        	byte[] vdbBytes = vdbHelper.getVdbByteArray(sourceVdb);

        	// Deploy the VDB
        	clientAccessor.getClient().deploy(deploymentName, new ByteArrayInputStream(vdbBytes));

        	// Wait for VDB to finish loading
        	waitForVDBLoad(sourceVDBName, 1, Constants.VDB_LOADING_TIMEOUT_SECS);                        

        	// Get deployed VDB and return status
        	String vdbStatus = getVDBStatusMessage(sourceVDBName);
        	if(!vdbStatus.equals(Constants.SUCCESS)) {
        		return vdbStatus;
        	}
        	return Constants.SUCCESS;
    	} catch (Exception e) {
			throw new DataVirtUiException(e.getMessage());
    	}

    }
    
    /*
     * Get the error messages (if any) for the supplied VDB.
     * @param vdbName the name of the VDB
     * @return the Error Message string, or 'success' if none
     */
    private String getVDBStatusMessage(String vdbName) throws DataVirtUiException {
    	// Get deployed VDB and check status
    	VDBMetaData theVDB;
		try {
			theVDB = clientAccessor.getClient().getVDB(vdbName,1);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	if(theVDB!=null) {
    		Status vdbStatus = theVDB.getStatus();
    		if(vdbStatus!=Status.ACTIVE) {
    			List<String> allErrors = theVDB.getValidityErrors();
    			if(allErrors!=null && !allErrors.isEmpty()) {
    				StringBuffer sb = new StringBuffer();
    				for(String errorMsg : allErrors) {
    					sb.append("ERROR: " +errorMsg);
    				}
    				return sb.toString();
    			}
    		}
    	}
    	return Constants.SUCCESS;
    }
    
    private String getSourceJndiName(String dataSourceName) throws DataVirtUiException {
    	// Get Data Source properties
    	Properties dsProps = null;
    	try {
			dsProps = clientAccessor.getClient().getDataSourceProperties(dataSourceName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}

    	return dsProps.getProperty("jndi-name");
    }
    
    /*
     * Create the specified VDB "teiid-local" source on the server. If it already exists, delete it first.
     * @param vdbName the name of the VDB for the connection
     */
    private void createVdbDataSource(String vdbName) throws DataVirtUiException {
    	Properties vdbProps = new Properties();
    	vdbProps.put("connection-url","jdbc:teiid:"+vdbName+";useJDBC4ColumnNameAndLabelSemantics=false");
    	vdbProps.put("user-name","user");
    	vdbProps.put("password","user");

    	// Create the datasource (deletes first, if it already exists)
    	addDataSource(vdbName, "teiid-local", vdbProps );
    }
    
    /*
     * Create the specified source on the server. If it already exists, delete it first - then redeploy
     * @param sourceName the name of the source to add
     * @param templateName the name of the template for the source
     * @param sourcePropMap the map of property values for the specified source
     */
    private void addDataSource(String sourceName, String templateName, Properties sourceProps) throws DataVirtUiException {
    	try {
			// If 'sourceName' already exists - delete it first...
			clientAccessor.getClient().deleteDataSource(sourceName);

			// Create the specified datasource
			clientAccessor.getClient().createDataSource(sourceName,templateName,sourceProps);
		} catch (Exception e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }
    
    /**
     * Delete a dataSource and a VDB.  Used to delete a VDB which is exposed as a source in one operation
     * @param dsName the source name
     * @param vdbName the vdb name
     * @throws DataVirtUiException
     */
    @Override
    public List<VdbDetailsBean> deleteDataSourceAndVdb(String dsName, String vdbName) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteDataSource(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	Collection<String> vdbNames = new ArrayList<String>(1);
    	vdbNames.add(vdbName);
    	deleteVdbs(vdbNames);
    	
    	return getDynamicVdbsWithPrefix(Constants.SERVICE_VDB_PREFIX);
    }
    
    @Override
    public void deleteDataSources(Collection<String> dsNames) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteDataSources(dsNames);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }
    
    @Override
    public void deleteTypes(Collection<String> dsTypes) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteDataSourceTypes(dsTypes);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }
 
    /*
     * Deploy a VDB with the requested View Model.
     * @param vdbName name of the VDB
     * @param viewModelName the name of the viewModel to add
     * @param ddlString the DDL string to use for the view model
     * @return the VdbDetails
     */
    public VdbDetailsBean deployNewVDB(final String vdbName, final int vdbVersion, final ViewModelRequestBean viewModelRequest) throws DataVirtUiException {
    	// Create a new VDB
    	VDBMetaData theVDB = vdbHelper.createVdb(vdbName, vdbVersion);
  	
    	// Add the requested viewModel to the VDB
    	VDBMetaData newVdb = vdbHelper.addViewModel(theVDB, viewModelRequest.getName(), viewModelRequest.getDescription(), viewModelRequest.getDdl(), viewModelRequest.isVisible());

    	// Add the required source import VDBs to the VDB
    	List<String> rqdImportVdbNames = viewModelRequest.getRequiredImportVdbNames();
    	List<Integer> rqdImportVdbVersions = viewModelRequest.getRequiredImportVdbVersions();
    	if(rqdImportVdbNames!=null && !rqdImportVdbNames.isEmpty()) { 
    		newVdb = vdbHelper.addImports(newVdb, rqdImportVdbNames, rqdImportVdbVersions);
    	}

		String deployString;
		try {
			deployString = vdbHelper.getVdbString(newVdb);
			
			if(deployString!=null) {
				// Deploys the VDB, waits for it to load and deploys corresponding source
				deployVdbWithSource(vdbName, new ByteArrayInputStream(deployString.getBytes("UTF-8")));
			}
		} catch (Exception e) {
			throw new DataVirtUiException(e);
		}

    	// Return details
    	return getVdbDetails(vdbName, 1);
    }
    
    private void deployVdbWithSource(String vdbName, InputStream vdbContent) throws DataVirtUiException {
		// Deployment name for vdb must end in '-vdb.xml'
		String deploymentName = vdbName + Constants.DYNAMIC_VDB_SUFFIX;

    	// Deploy the VDB
		InputStream contentStream = null;
		try {
			clientAccessor.getClient().deploy(deploymentName, vdbContent);
		} catch(AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
		
        // This wait method takes deploymentName
        waitForVDBDeploymentToLoad(deploymentName, Constants.VDB_LOADING_TIMEOUT_SECS);

        // Create the Vdb data source
        createVdbDataSource(vdbName);
    }
    
    /*
     * Helper method - waits for the VDB to finish loading
     * @param deploymentName the deployment name for the VDB
     * @param timeoutInSecs time to wait before timeout
     * @return 'true' if vdb found and is out of 'Loading' status, 'false' otherwise.
     */
    private boolean waitForVDBDeploymentToLoad(String deploymentName, int timeoutInSecs) {
    	long waitUntil = System.currentTimeMillis() + timeoutInSecs*1000;
    	if (timeoutInSecs < 0) {
    		waitUntil = Long.MAX_VALUE;
    	}

    	String vdbName = null;
    	int vdbVersion = 1;
    	// Get VDB name and version for the specified deploymentName
    	Collection<? extends VDB> allVdbs = null;
    	try {
    		allVdbs = clientAccessor.getClient().getVdbs();
    		for(VDB vdbMeta : allVdbs) {
    			String deployName = vdbMeta.getPropertyValue("deployment-name");
    			if(deployName!=null && deployName.equals(deploymentName)) {
    				vdbName=vdbMeta.getName();
    				vdbVersion=vdbMeta.getVersion();
    				break;
    			}
    		}
    	} catch (AdminApiClientException e) {
    	}

    	if(vdbName==null) return false;

    	boolean first = true;
    	do {
    		// Pause 5 sec before subsequent attempts
    		if (!first) {
    			try {
    				Thread.sleep(5000);
    			} catch (InterruptedException e) {
    				break;
    			}
    		} else {
    			first = false;
    		}
    		// Get the VDB using admin API
    		VDBMetaData vdbMetaData = null;
    		try {
    			vdbMetaData = (VDBMetaData)clientAccessor.getClient().getVDB(vdbName, vdbVersion);
    		} catch (AdminApiClientException e) {
    		}
    		// Determine if VDB is loading, or whether to wait
    		if(vdbMetaData!=null) {
    			Status vdbStatus = vdbMetaData.getStatus();
    			// return if no models in VDB, or VDB has errors (done loading)
    			if(vdbMetaData.getModels().isEmpty() || vdbStatus==Status.FAILED || vdbStatus==Status.REMOVED || vdbStatus==Status.ACTIVE) {
    				return true;
    			}
    			// If the VDB Status is LOADING, but a validity error was found - return
    			if(vdbStatus==Status.LOADING && !vdbMetaData.getValidityErrors().isEmpty()) {
    				return true;
    			}
    		}
    	} while (System.currentTimeMillis() < waitUntil);
    	return false;
    }
    
    /*
     * Helper method - waits for the VDB to finish loading
     * @param vdbName the name of the VDB
     * @param vdbVersion the VDB version
     * @param timeoutInSecs time to wait before timeout
     * @return 'true' if vdb found and is out of 'Loading' status, 'false' otherwise.
     */
    private boolean waitForVDBLoad(String vdbName, int vdbVersion, int timeoutInSecs) {
    	long waitUntil = System.currentTimeMillis() + timeoutInSecs*1000;
    	if (timeoutInSecs < 0) {
    		waitUntil = Long.MAX_VALUE;
    	}

    	boolean first = true;
    	do {
    		// Pause 5 sec before subsequent attempts
    		if (!first) {
    			try {
    				Thread.sleep(5000);
    			} catch (InterruptedException e) {
    				break;
    			}
    		} else {
    			first = false;
    		}
    		// Get the VDB using admin API
    		VDBMetaData vdbMetaData = null;
    		try {
    			vdbMetaData = (VDBMetaData)clientAccessor.getClient().getVDB(vdbName, vdbVersion);
    		} catch (AdminApiClientException e) {
    		}
    		// Determine if VDB is loading, or whether to wait
    		if(vdbMetaData!=null) {
    			Status vdbStatus = vdbMetaData.getStatus();
    			// return if no models in VDB, or VDB has errors (done loading)
    			if(vdbMetaData.getModels().isEmpty() || vdbStatus==Status.FAILED || vdbStatus==Status.REMOVED || vdbStatus==Status.ACTIVE) {
    				return true;
    			}
    			// If the VDB Status is LOADING, but a validity error was found - return
    			if(vdbStatus==Status.LOADING && !vdbMetaData.getValidityErrors().isEmpty()) {
    				return true;
    			}
    		}
    	} while (System.currentTimeMillis() < waitUntil);
    	return false;
    }

    @Override
    public void deleteDynamicVdbsWithPrefix(String vdbPrefix) throws DataVirtUiException {
    	// Get the list of vdbNames that start with the prefix
    	List<String> vdbsToDelete = new ArrayList<String>();
    	Collection<String> allNames;
		try {
			allNames = clientAccessor.getClient().getVdbNames(true,false,false);
		} catch (AdminApiClientException e1) {
    		throw new DataVirtUiException(e1);
		}
		if(allNames==null) return;
		
    	for(String vdbName : allNames) {
    		if(vdbName.startsWith(vdbPrefix)) {
    			vdbsToDelete.add(vdbName);
    		}
    	}
    	 
    	// Undeploys the VDBS
    	try {
			clientAccessor.getClient().deleteVDBs(vdbsToDelete);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	// Deletes the corresponding DataSources
    	try {
			clientAccessor.getClient().deleteDataSources(vdbsToDelete);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }
    
    public List<VdbDetailsBean> getDynamicVdbsWithPrefix(String vdbPrefix) throws DataVirtUiException {
    	// Collect all of the Service VDB names
    	List<String> svcVdbNames = new ArrayList<String>();
    	Collection<String> allVdbNames = null;
    	try {
    		allVdbNames = clientAccessor.getClient().getVdbNames(true,false,false);
    		for(String vdbName : allVdbNames) {
    			if(vdbName!=null && vdbName.startsWith(vdbPrefix)) {
    				svcVdbNames.add(vdbName);
    			}
    		}
    	} catch (AdminApiClientException e) {
    		throw new DataVirtUiException(e);
    	}
    	
    	// Alphabetic sort
    	Collections.sort(svcVdbNames);
    	
    	List<VdbDetailsBean> svcVdbs = new ArrayList<VdbDetailsBean>(svcVdbNames.size());
    	for(String svcVdbName : svcVdbNames) {
    		svcVdbs.add(getVdbDetails(svcVdbName));
    	}
    	
    	return svcVdbs;
    }
    
    private List<String> getDynamicVdbNamesWithPrefix(String vdbPrefix) throws DataVirtUiException {
    	// Collect all of the Service VDB names
    	List<String> svcVdbNames = new ArrayList<String>();
    	Collection<String> allVdbNames = null;
    	try {
    		allVdbNames = clientAccessor.getClient().getVdbNames(true,false,false);
    		for(String vdbName : allVdbNames) {
    			if(vdbName!=null && vdbName.startsWith(vdbPrefix)) {
    				svcVdbNames.add(vdbName);
    			}
    		}
    	} catch (AdminApiClientException e) {
    		throw new DataVirtUiException(e);
    	}
    	
    	return svcVdbNames;
    }
    
    public List<VdbDetailsBean> cloneDynamicVdbAddSource(String vdbName, int vdbVersion) throws DataVirtUiException {                
    	// Get deployed VDB and check status
    	VDBMetaData theVDB;
    	try {
    		theVDB = clientAccessor.getClient().getVDB(vdbName,vdbVersion);
    	} catch (AdminApiClientException e) {
    		throw new DataVirtUiException(e.getMessage());
    	}
    	
    	// Get current service vdb names
    	List<String> currentVdbNames = getDynamicVdbNamesWithPrefix(Constants.SERVICE_VDB_PREFIX);
    	
    	// Get a unique name and suffix for the copy
    	String baseSuffix = "_copy";
    	String clonedVdbName = vdbName+baseSuffix;
    	String clonedVdbSuffix = generateUniqueSuffix(vdbName, currentVdbNames, baseSuffix);
    	if(!clonedVdbSuffix.equals(baseSuffix)) {
    		clonedVdbName = vdbName+clonedVdbSuffix;
    	}

    	// Clone the View Models and get the new VDB
    	VDBMetaData newVdb = vdbHelper.cloneVdbRenamingViewModels(theVDB, clonedVdbSuffix);
    	newVdb.setName(clonedVdbName);
    	
		String deployString;
		try {
			deployString = vdbHelper.getVdbString(newVdb);
			
			if(deployString!=null) {
				// Deploys the VDB, waits for it to load and deploys corresponding source
				deployVdbWithSource(clonedVdbName, new ByteArrayInputStream(deployString.getBytes("UTF-8")));
			}
		} catch (Exception e) {
			throw new DataVirtUiException(e);
		}
		
    	return getDynamicVdbsWithPrefix(Constants.SERVICE_VDB_PREFIX);
    }
    
    @Override
    public VdbDetailsBean getVdbDetails(String vdbName) throws DataVirtUiException {
    	VDBMetaData vdb = null;

    	try {
        	vdb = clientAccessor.getClient().getVDB(vdbName,1);
    	} catch (Exception e) {
			throw new DataVirtUiException(e.getMessage());
    	}
    	
    	VdbDetailsBean vdbDetailsBean = vdbHelper.getVdbDetails(vdb);
    	
    	String serverHost = getServerHost();
    	vdbDetailsBean.setServerHost(serverHost);

        return vdbDetailsBean;
    }
    
    public VdbDetailsBean getVdbDetails(String vdbName, int page) throws DataVirtUiException {
        int pageSize = Constants.VDB_MODELS_TABLE_PAGE_SIZE;
    	
    	VDBMetaData vdb = null;

    	try {
        	vdb = clientAccessor.getClient().getVDB(vdbName,1);
    	} catch (Exception e) {
			throw new DataVirtUiException(e.getMessage());
    	}
    	
    	VdbDetailsBean vdbDetailsBean = vdbHelper.getVdbDetails(vdb);
    	int totalModels = vdbDetailsBean.getTotalModels();
    	
        // Start and End Index for this page
        int page_startIndex = (page - 1) * pageSize;
        int page_endIndex = page_startIndex + (pageSize-1);
        // If page endIndex greater than total rows, reset to end
        if(page_endIndex > (totalModels-1)) {
        	page_endIndex = totalModels-1;
        }
        
        vdbDetailsBean.setModelsPerPage(pageSize);
        vdbDetailsBean.setStartIndex(page_startIndex);
        vdbDetailsBean.setEndIndex(page_endIndex);

        return vdbDetailsBean;
    }
    
	private String generateUniqueSuffix(String baseName, List<String> existingNames, String baseSuffix) {
		// If the name is not contained in existing names, base suffix is ok
		if(!existingNames.contains(baseName+baseSuffix)) {
			return baseSuffix;
		}
		// Iterate generating new names until a good one is found
		String newName = null;
		String suffix = baseSuffix;
		boolean success = false;
		int i = 1;
		while(!success) {
			if(i==1) {
			    newName = baseName + suffix;
			} else {
				suffix = baseSuffix+i;
				newName = baseName + suffix;
			}
			if(!existingNames.contains(newName)) {
				success=true;
			}
			i++;
		}
		return suffix;
	}
	
    private String getServerHost() {
    	String serverHost = LOCALHOST;
    	
   		String serverIP = System.getProperty("jboss.bind.address");
    	// If the server bind address is set, override the default 'localhost'
    	if(!StringUtils.isEmpty(serverIP)) {
    		serverHost = serverIP;
    	}
    	return serverHost;
    }

}
