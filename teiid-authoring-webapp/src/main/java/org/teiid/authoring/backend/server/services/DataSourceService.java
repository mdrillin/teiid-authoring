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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.teiid.adminapi.PropertyDefinition;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.authoring.backend.server.api.AdminApiClientAccessor;
import org.teiid.authoring.backend.server.services.util.FilterUtil;
import org.teiid.authoring.backend.server.services.util.JdbcSourceHelper;
import org.teiid.authoring.backend.server.services.util.VdbHelper;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.TranslatorHelper;
import org.teiid.authoring.share.beans.DataSourceDetailsBean;
import org.teiid.authoring.share.beans.DataSourcePageRow;
import org.teiid.authoring.share.beans.DataSourcePropertyBean;
import org.teiid.authoring.share.beans.DataSourceResultSetBean;
import org.teiid.authoring.share.beans.DataSourceSummaryBean;
import org.teiid.authoring.share.beans.DataSourceTypeBean;
import org.teiid.authoring.share.beans.DataSourceTypeResultSetBean;
import org.teiid.authoring.share.beans.DataSourceWithVdbDetailsBean;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.VdbModelBean;
import org.teiid.authoring.share.exceptions.DataVirtUiException;
import org.teiid.authoring.share.services.IDataSourceService;
import org.teiid.authoring.share.services.StringUtils;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

/**
 * Concrete implementation of the DataSource service.
 *
 * @author mdrillin@redhat.com
 */
@Service
public class DataSourceService implements IDataSourceService {

    private static final String DRIVER_KEY = "driver-name";
    private static final String CLASSNAME_KEY = "class-name";
    private static final String CONN_FACTORY_CLASS_KEY = "managedconnectionfactory-class";
    private static final String CONNECTION_URL_DISPLAYNAME = "connection-url";

    @Inject
    private AdminApiClientAccessor clientAccessor;
    
    @Inject
    private VdbService vdbService;

    /**
     * Constructor.
     */
    public DataSourceService() {
    }

    public PageResponse<DataSourcePageRow> getDataSources( final PageRequest pageRequest, final String filters ) {

    	List<String> filteredDsList = new ArrayList<String>();
		try {
			List<String> allDSList = getDataSourceNames();
			for(String sourceName : allDSList) {
				if(sourceName!=null && !sourceName.isEmpty() && !sourceName.startsWith("PREVIEW_")) {
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

    	// Get list of all Server Sources (except preview vdb sources)
    	List<String> filteredDsList = new ArrayList<String>();
		try {
			List<String> allDSList = getDataSourceNames();
			for(String sourceName : allDSList) {
				if(sourceName!=null && !sourceName.isEmpty() && !sourceName.startsWith("PREVIEW_")) {
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
    		dataSourcePageRow.setHasVdb(hasSrcVdbList.get(i));
    		resultDSPageRowList.add( dataSourcePageRow );
    		i++;
    	}
    	
    	return resultDSPageRowList;
    }
    
    private boolean hasSourceVdb(String dsName, String vdbPrefix, List<String> allDsNames) {
    	String srcVdbName = vdbPrefix + dsName;
    	return allDsNames.contains(srcVdbName);
    }
        
    @Override
    public DataSourceResultSetBean search(String searchText, int page, String sortColumnId, boolean sortAscending) throws DataVirtUiException {
        int pageSize = Constants.DATASOURCES_TABLE_PAGE_SIZE; 
        
        DataSourceResultSetBean data = new DataSourceResultSetBean();
        
        Collection<Properties> dsSummaryPropsCollection = null;
        try {
        	dsSummaryPropsCollection = clientAccessor.getClient().getDataSourceSummaryPropsCollection();
		} catch (AdminApiClientException e) {
		}
        
        // List of all the names
        List<Properties> propertiesList = new ArrayList<Properties>(dsSummaryPropsCollection);
        // Save complete list
        List<String> allDsNames = new ArrayList<String>(dsSummaryPropsCollection.size());
        List<String> allDsNamesSort = new ArrayList<String>(dsSummaryPropsCollection.size());
        for(Properties dsProps : propertiesList) {
            String sourceName = dsProps.getProperty("name");
            if( sourceName!=null && !sourceName.isEmpty() ) {
            	allDsNames.add(sourceName);
            	if ( FilterUtil.matchFilter(sourceName, searchText) ) {
            		allDsNamesSort.add(sourceName.toLowerCase());
            	}
            }
        }
        // Sort alpha by name
        Collections.sort(allDsNamesSort);
        // If reverse alpha, reverse the sorted list
        if(!sortAscending) {
        	Collections.reverse(allDsNamesSort);
        }
        
        int totalSources = allDsNamesSort.size();
        
        // Start and End Index for this page
        int page_startIndex = (page - 1) * pageSize;
        int page_endIndex = page_startIndex + (pageSize-1);
        // If page endIndex greater than total rows, reset to end
        if(page_endIndex > (totalSources-1)) {
        	page_endIndex = totalSources-1;
        }
        
        // Gets jdbc Jndi names available on the server
        List<String> jdbcJndiNames = JdbcSourceHelper.getInstance().getJdbcSourceNames(false);
        
        List<DataSourceSummaryBean> rows = new ArrayList<DataSourceSummaryBean>();
        if(!allDsNamesSort.isEmpty()) {
        	for(int i=page_startIndex; i<=page_endIndex; i++) {
        		DataSourceSummaryBean summaryBean = new DataSourceSummaryBean();
        		// Name of source were looking for
        		String dsName = allDsNamesSort.get(i);
        		// Iterate the properties List, find the matching source properties
        		for(Properties dsProps : propertiesList) {
        			String thisDsName = dsProps.getProperty("name");
        			if(thisDsName.equalsIgnoreCase(dsName)) {
        				summaryBean.setName(thisDsName);
        				String jndiName = dsProps.getProperty("jndi-name");
        				summaryBean.setJndiName(jndiName);
        				if(jdbcJndiNames.contains(jndiName)) {
        					summaryBean.setTestable(true);
        				}
        				summaryBean.setType(dsProps.getProperty("type"));
        				rows.add(summaryBean);
        				break;
        			}
        		}
        	}
        }
        data.setAllDsNames(allDsNames);
        data.setDataSources(rows);
        data.setItemsPerPage(pageSize);
        data.setStartIndex(page_startIndex);
        data.setTotalResults(totalSources);
        
        return data;
    }
    
    @Override
    public DataSourceTypeResultSetBean getDataSourceTypeResultSet(int page, String sortColumnId, boolean sortAscending) throws DataVirtUiException {
        int pageSize = Constants.DATASOURCE_TYPES_TABLE_PAGE_SIZE; 
        
        DataSourceTypeResultSetBean data = new DataSourceTypeResultSetBean();
        
        Collection<String> dsTypesCollection = null;
        try {
        	dsTypesCollection = clientAccessor.getClient().getDataSourceTypes();
		} catch (AdminApiClientException e) {
		}
        
    	// Filter out 'types' ending with .war
        List<String> dsTypesList = new ArrayList<String>(dsTypesCollection.size());
        List<String> dsTypesListSort = new ArrayList<String>(dsTypesCollection.size());
    	for(String dsType : dsTypesCollection) {
    	   if(dsType!=null && !dsType.endsWith(".war")) {
    		   dsTypesList.add(dsType);
    		   dsTypesListSort.add(dsType.toLowerCase());
    	   }
    	}
    	
        // Sort alpha by name
        Collections.sort(dsTypesListSort);
        // If reverse alpha, reverse the sorted list
        if(!sortAscending) {
        	Collections.reverse(dsTypesListSort);
        }
    	
        int totalTypes = dsTypesList.size();
        
        // Start and End Index for this page
        int page_startIndex = (page - 1) * pageSize;
        int page_endIndex = page_startIndex + (pageSize-1);
        // If page endIndex greater than total rows, reset to end
        if(page_endIndex > (totalTypes-1)) {
        	page_endIndex = totalTypes-1;
        }
        
        List<DataSourceTypeBean> rows = new ArrayList<DataSourceTypeBean>();
        for(int i=page_startIndex; i<=page_endIndex; i++) {
        	DataSourceTypeBean typeBean = new DataSourceTypeBean();
            String typeName = dsTypesListSort.get(i);
            // Gets the name from original list (with correct case)
            for(String thisTypeName : dsTypesList) {
            	if(thisTypeName.equalsIgnoreCase(typeName)) {
            		typeBean.setName(thisTypeName);
            		rows.add(typeBean);
            		break;
            	}
            }
        }
        data.setDataSourceTypes(rows);
        data.setItemsPerPage(pageSize);
        data.setStartIndex(page_startIndex);
        data.setTotalResults(totalTypes);
        
        return data;
    }
    
    @Override
    public DataSourceDetailsBean getDataSourceDetails(String dsName) throws DataVirtUiException {
    	// Create DataSource Details Bean - set name
    	DataSourceDetailsBean dsDetailsBean = new DataSourceDetailsBean();
    	dsDetailsBean.setName(dsName);

    	// Get Data Source properties
    	Properties dsProps = null;
    	try {
			dsProps = clientAccessor.getClient().getDataSourceProperties(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}

    	// Set jndi name
    	dsDetailsBean.setJndiName(dsProps.getProperty("jndi-name"));

    	// Determine type/driver from properties
    	String dsType = getDataSourceType(dsProps);
    	dsDetailsBean.setType(dsType);
    	
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
    	
    	dsDetailsBean.setProperties(dataSourcePropertyBeans);

    	return dsDetailsBean;
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
    public Map<String,String> getQueryableDataSourceMap( ) throws DataVirtUiException {
        Collection<Properties> dsSummaryPropsCollection = null;
        try {
        	dsSummaryPropsCollection = clientAccessor.getClient().getDataSourceSummaryPropsCollection();
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
        
        // Create a Map of *all* Datasources and their jndi names
        Map<String,String> allSourcesToJndiMap = new HashMap<String,String>();
        for(Properties dsProps : dsSummaryPropsCollection) {
            String sourceName = dsProps.getProperty("name");
            String jndiName = dsProps.getProperty("jndi-name");
            if( !StringUtils.isEmpty(sourceName) && !StringUtils.isEmpty(sourceName) ) {
            	allSourcesToJndiMap.put(sourceName, jndiName);
            }
        }
        
        // Gets jdbc Jndi names available on the server
        List<String> jdbcJndiNames = JdbcSourceHelper.getInstance().getJdbcSourceNames(false);
        
        Map<String,String> resultMap = new HashMap<String,String>();
        for(String allDsName : allSourcesToJndiMap.keySet()) {
        	if(jdbcJndiNames.contains(allSourcesToJndiMap.get(allDsName))) {
        		resultMap.put(allDsName,allSourcesToJndiMap.get(allDsName));
        	}
        }
        
        return resultMap;
    }

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
    	vdbService.delete(vdbsToDelete);
    	// Create the corresponding SrcVdb for the new source
    	vdbService.deploySourceVDB(bean.getSourceVdbName(), bean.getName(), bean.getName(), jndiName, bean.getTranslator());
    	
    	// Create the teiid dataSource for the deployed source VDB
    	createVdbDataSource(bean.getSourceVdbName());
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
    public void deleteDataSourceAndVdb(String dsName, String vdbName) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteDataSource(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	Collection<String> vdbNames = new ArrayList<String>(1);
    	vdbNames.add(vdbName);
    	vdbService.delete(vdbNames);
    }
    
    @Override
    public void deleteDataSource(String dsName) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteDataSource(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
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
    
}
