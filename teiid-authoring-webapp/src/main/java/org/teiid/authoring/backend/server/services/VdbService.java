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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.errai.bus.server.annotations.Service;
import org.teiid.adminapi.VDB;
import org.teiid.adminapi.VDB.Status;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.adminapi.impl.VDBMetadataParser;
import org.teiid.authoring.backend.server.api.AdminApiClientAccessor;
import org.teiid.authoring.backend.server.services.util.FilterUtil;
import org.teiid.authoring.backend.server.services.util.JdbcSourceHelper;
import org.teiid.authoring.backend.server.services.util.VdbHelper;
import org.teiid.authoring.share.beans.Constants;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.VdbModelBean;
import org.teiid.authoring.share.beans.VdbResultSetBean;
import org.teiid.authoring.share.beans.VdbSummaryBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.teiid.authoring.share.exceptions.DataVirtUiException;
import org.teiid.authoring.share.services.IVdbService;
import org.teiid.authoring.share.services.StringUtils;

/**
 * Concrete implementation of the VDB service.
 *
 * @author mdrillin@redhat.com
 */
@Service
public class VdbService implements IVdbService {

    private static final String LOCALHOST = "127.0.0.1";
    
    @Inject
    private AdminApiClientAccessor clientAccessor;

    private VdbHelper vdbHelper = VdbHelper.getInstance();
    
    /**
     * Constructor.
     */
    public VdbService() {
    }

    /**
     * @see org.jboss.datavirt.ui.client.shared.services.IDataSourceSearchService#search(java.lang.String, int, java.lang.String, boolean)
     */
    @Override
    public VdbResultSetBean search(String searchText, int page, boolean showDataVirtUiVDBs, String sortColumnId, boolean sortAscending) throws DataVirtUiException {
        int pageSize = Constants.VDBS_TABLE_PAGE_SIZE; 
        
        VdbResultSetBean data = new VdbResultSetBean();
        
        Collection<Properties> vdbSummaryProps = null;
        try {
        	vdbSummaryProps = clientAccessor.getClient().getVdbSummaryPropCollection(true, true, true);
		} catch (AdminApiClientException e) {
		}
        
        // List of all the names
        List<Properties> vdbPropsList = new ArrayList<Properties>(vdbSummaryProps);
        // Save complete list
        List<String> allVdbNames = new ArrayList<String>(vdbSummaryProps.size());
        List<String> allVdbNamesSort = new ArrayList<String>(vdbSummaryProps.size());
        for(Properties vdbProps : vdbPropsList) {
            String vdbName = vdbProps.getProperty("name");
            if(!StringUtils.isEmpty(vdbName)) {
            	// If not showing the DataVirtUi created VDBS, then skip them
            	if(!showDataVirtUiVDBs && vdbName.startsWith(Constants.SOURCE_VDB_PREFIX)) {
            		continue;
            	} else {
            		allVdbNames.add(vdbName);
            		if ( FilterUtil.matchFilter(vdbName, searchText) ) {
            			allVdbNamesSort.add(vdbName.toLowerCase());
            		}
            	}
            }
        }
        
        // Sort alpha by name
        Collections.sort(allVdbNamesSort);
        // If reverse alpha, reverse the sorted list
        if(!sortAscending) {
        	Collections.reverse(allVdbNamesSort);
        }    	
        
        int totalVdbs = allVdbNamesSort.size();
        
        // Start and End Index for this page
        int page_startIndex = (page - 1) * pageSize;
        int page_endIndex = page_startIndex + (pageSize-1);
        // If page endIndex greater than total rows, reset to end
        if(page_endIndex > (totalVdbs-1)) {
        	page_endIndex = totalVdbs-1;
        }
        
        // Gets jdbc Jndi names available on the server
        List<String> jdbcJndiNames = JdbcSourceHelper.getInstance().getJdbcSourceNames(false);

        List<VdbSummaryBean> rows = new ArrayList<VdbSummaryBean>();
        if(!allVdbNamesSort.isEmpty()) {
        	for(int i=page_startIndex; i<=page_endIndex; i++) {
        		VdbSummaryBean summaryBean = new VdbSummaryBean();
        		String vdbName = allVdbNamesSort.get(i);
        		for(Properties vdbProps : vdbPropsList) {
        			String thisVdbName = vdbProps.getProperty("name");
        			if(thisVdbName.equalsIgnoreCase(vdbName)) {
        				summaryBean.setName(thisVdbName);
        				summaryBean.setType(vdbProps.getProperty("type"));
        				summaryBean.setStatus(vdbProps.getProperty("status"));
        				if(jdbcJndiNames.contains("java:/"+thisVdbName)) {
        					summaryBean.setTestable(true);
        				}
        				rows.add(summaryBean);
        				break;
        			}
        		}
        	}
        }
        data.setAllVdbNames(allVdbNames);
        data.setVdbs(rows);
        data.setItemsPerPage(pageSize);
        data.setStartIndex(page_startIndex);
        data.setTotalResults(totalVdbs);
        
        return data;
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
    
    private String getServerHost() {
    	String serverHost = LOCALHOST;
    	
   		String serverIP = System.getProperty("jboss.bind.address");
    	// If the server bind address is set, override the default 'localhost'
    	if(!StringUtils.isEmpty(serverIP)) {
    		serverHost = serverIP;
    	}
    	return serverHost;
    }
    
    @Override
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
    
    @Override
    public List<String> getTranslatorsForSrcVdbs(List<String> srcVdbNames) throws DataVirtUiException {
    	VDBMetaData vdb = null;
    	List<String> translators = new ArrayList<String>(srcVdbNames.size());
    	
    	for(String srcVdbName : srcVdbNames) {
    		try {
    			vdb = clientAccessor.getClient().getVDB(srcVdbName,1);
    		} catch (Exception e) {
    			throw new DataVirtUiException(e.getMessage());
    		}
    		
    		// Details for this VDB
    		VdbDetailsBean vdbDetailsBean = vdbHelper.getVdbDetails(vdb);
    		// The modelName in VDB is same as VDB, but without the prefix
    		String physModelName = srcVdbName.substring(srcVdbName.indexOf(Constants.SERVICE_SOURCE_VDB_PREFIX)+Constants.SERVICE_SOURCE_VDB_PREFIX.length());
    		
    		// Get source models from VDB, find matching model and get translator
    		Collection<VdbModelBean> vdbModels = vdbDetailsBean.getModels();
    		for(VdbModelBean vdbModel : vdbModels) {
    			String modelName = vdbModel.getName();
    			if(modelName.equals(physModelName)) {
        			String modelTranslator = vdbModel.getTranslator();
        			translators.add(modelTranslator);
    			}
    		}
    	}

        return translators;
    }
    
    /**
     * @see org.jboss.datavirt.ui.client.shared.services.IArtifactService#get(java.lang.String)
     */
    @Override
    public String getVdbXml(String vdbName) throws DataVirtUiException {
    	String vdbXml = null;

    	try {
    		VDBMetaData vdb = clientAccessor.getClient().getVDB(vdbName,1);

    		vdbXml = vdbHelper.getVdbString(vdb);
    	} catch (Exception e) {
			throw new DataVirtUiException(e.getMessage());
    	}
    	
    	return vdbXml;
    }    
    
    /*
     * Create and deploy a Dynamic VDB - if it is not already deployed
     * @param vdbName name of the VDB to create
     */
    public void createAndDeployDynamicVdb(String vdbName) throws DataVirtUiException {
    	try {
        	VDBMetaData vdb = clientAccessor.getClient().getVDB(vdbName,1);

    		// Only deploy the VDB if it was not found
    		if(vdb==null) {

    			// Deploy the VDB
    			VDBMetaData newVdb = vdbHelper.createVdb(vdbName,1);
    			newVdb.addProperty("{http://teiid.org/rest}auto-generate","true");
    			newVdb.addProperty("{http://teiid.org/rest}security-type","none");
    			String deployString = vdbHelper.getVdbString(newVdb);

    			// Deploys the VDB, waits for it to load and deploys corresponding source
    			deployVdb(vdbName, new ByteArrayInputStream(deployString.getBytes("UTF-8")));
    		}
    	} catch (Exception e) {
			throw new DataVirtUiException(e.getMessage());
    	}
    }
    
    private void deployVdb(String vdbName, InputStream vdbContent) throws DataVirtUiException {
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
    		allVdbs = clientAccessor.getClient().getVDBs();
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
    
    /**
     * @see org.jboss.datavirt.ui.client.shared.services.IArtifactService#delete(org.jboss.datavirt.ui.client.shared.beans.ArtifactBean)
     */
    @Override
    public void delete(Collection<String> vdbNames) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteVDBs(vdbNames);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }
    
    public VdbDetailsBean deploySourceVDBAddImportAndRedeploy(String vdbName, int modelsPageNumber, String sourceVDBName, String modelName, String dataSourceName, String translator) throws DataVirtUiException {
    	// Get JNDI for the specified DataSource name.  if null choose a default
    	String jndiName = getSourceJndiName(dataSourceName);
    	if(StringUtils.isEmpty(jndiName)) {
    		jndiName = "java:/"+dataSourceName;
    	}
    	// Deploy the Source VDB
    	String sourceStatus = deploySourceVDB(sourceVDBName, modelName, dataSourceName, jndiName, translator);
    	if(sourceStatus!=Constants.SUCCESS) {
    		throw new DataVirtUiException("Could not add the source : \n "+sourceStatus);
    	}

    	// Add the source VDB as an import, then redeploy the vdb
    	addImportAndRedeploy(vdbName,sourceVDBName,1);

    	// Return details
    	return getVdbDetails(vdbName, modelsPageNumber);
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
    
    /**
     * Deploys a SourceVDB for the specified dataSource, if it doesnt already exist
     * @param sourceVDBName the name of the source VDB
     * @param modelName the name of the model
     * @param dataSourceName the name of the datasource
     * @param jndiName the JNDI name of the datasource
     * @param translator the name of the translator
     */
    public String deploySourceVDB(String sourceVDBName, String modelName, String dataSourceName, String jndiName, String translator) throws DataVirtUiException {
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
    
    /*
     * Undeploy the current deployed VDB, re-deploy the supplied VDBMetadata, then define
     * the VDB as a Source
     * @param vdbName name of the VDB
     * @param vdb the VDBMetaData object
     */
    private void redeployVDB(String vdbName, VDBMetaData vdb) throws DataVirtUiException {
    	// output using VDBMetadataParser
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	try {
			VDBMetadataParser.marshell(vdb, out);
		} catch (Exception e) {
			throw new DataVirtUiException(e.getMessage());
		}

    	// Deployment name for vdb must end in '-vdb.xml'
    	String vdbDeployName = vdbName + Constants.DYNAMIC_VDB_SUFFIX;
    	try {
        	// Undeploy the working VDB
    		clientAccessor.getClient().undeploy(vdbDeployName);

        	// Deploy the updated VDB
    		clientAccessor.getClient().deploy(vdbDeployName, new ByteArrayInputStream(out.toByteArray()));

        	// Wait for VDB to finish loading
        	waitForVDBLoad(vdbName, 1, Constants.VDB_LOADING_TIMEOUT_SECS);

        	// Add the VDB as a source. If it already exists, it is deleted first then recreated.
        	// Re-create is required to clear the connection pool.
        	createVdbDataSource(vdbName);
    	} catch (AdminApiClientException e) {
    		throw new DataVirtUiException(e.getMessage());
    	}
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

    /*
     * Adds the Import in supplied VDB deployment. Redeploys the VDB after the import is added.
     * @param vdbName name of the VDB
     * @param importVdbName the name of the VDB to import
     * @param importVdbVersion the version of the VDB to import
     * @return the success string
     */
    private String addImportAndRedeploy(String vdbName, String importVdbName, int importVdbVersion) throws DataVirtUiException {
    	// Get deployed VDB and check status
    	VDBMetaData theVDB;
		try {
			theVDB = clientAccessor.getClient().getVDB(vdbName,1);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}

		VDBMetaData newVdb = vdbHelper.addImport(theVDB, importVdbName, importVdbVersion);
				
    	// Re-Deploy the VDB
   		redeployVDB(vdbName, newVdb);

    	// Get deployed VDB and return status
    	String vdbStatus = getVDBStatusMessage(vdbName);
    	if(!vdbStatus.equals(Constants.SUCCESS)) {
    		return "<bold>Error deploying VDB "+vdbName+"</bold><br>"+vdbStatus;
    	}
    	return Constants.SUCCESS;
    }
    
    /*
     * Add a View Model to the VDB for the specified viewName.
     * The VDB is then re-deployed
     * @param vdbName name of the VDB
     * @param viewModelName the name of the viewModel to add
     * @param ddlString the DDL string to use for the view model
     * @return the VdbDetails
     */
    public VdbDetailsBean addOrReplaceViewModelAndRedeploy(final String vdbName, final int modelsPageNumber, final ViewModelRequestBean viewModelRequest) throws DataVirtUiException {
    	// Get deployed VDB and check status
    	VDBMetaData theVDB;
    	try {
    		theVDB = clientAccessor.getClient().getVDB(vdbName,1);
    	} catch (AdminApiClientException e) {
    		throw new DataVirtUiException(e.getMessage());
    	}
  	
    	VDBMetaData newVdb = vdbHelper.addViewModel(theVDB, viewModelRequest.getName(), viewModelRequest.getDescription(), viewModelRequest.getDdl(), viewModelRequest.isVisible());

    	// Re-Deploy the VDB
    	redeployVDB(vdbName, newVdb);

    	// Return details
    	return getVdbDetails(vdbName, modelsPageNumber);
    }
    
    /*
     * Removes the models from the supplied VDB deployment - if they exist. Redeploys the VDB after
     * models are removed.
     * @param vdbName name of the VDB
     * @param removeModelNameList the list of model names to remove
     * @param removeModelTypeList the list of corresponding model types
     * @return the VdbDetails
     */
    public VdbDetailsBean removeModelsAndRedeploy(String vdbName, int modelsPageNumber, Map<String,String> removeModelNameAndTypeMap) throws DataVirtUiException {                
    	// Get deployed VDB and check status
    	VDBMetaData theVDB;
    	try {
    		theVDB = clientAccessor.getClient().getVDB(vdbName,1);
    	} catch (AdminApiClientException e) {
    		throw new DataVirtUiException(e.getMessage());
    	}

    	VDBMetaData newVdb = vdbHelper.removeModels(theVDB, removeModelNameAndTypeMap);
    	
    	List<String> srcModelVdbDeploymentNames = getSrcVdbsToUndeploy(removeModelNameAndTypeMap);

    	// Re-Deploy the VDB
    	redeployVDB(vdbName, newVdb);

    	// Undeploy the Source Model VDBs - if necessary
		for(String srcModelVdbDeploymentName : srcModelVdbDeploymentNames) {
	    	try {
				clientAccessor.getClient().undeploy(srcModelVdbDeploymentName);
	    	} catch (AdminApiClientException e) {
	    		throw new DataVirtUiException(e.getMessage());
	    	}
		}
    	
    	// Return details
    	return getVdbDetails(vdbName, modelsPageNumber);
    }
    
    /*
     * Removes the models from the supplied VDB deployment - if they exist. Redeploys the VDB after
     * models are removed.
     * @param vdbName name of the VDB
     * @param viewModelNames the list of view model names to clone
     * @return the VdbDetails
     */
    public VdbDetailsBean cloneViewModelAndRedeploy(String vdbName, int modelsPageNumber, String viewModelName) throws DataVirtUiException {                
    	// Get deployed VDB and check status
    	VDBMetaData theVDB;
    	try {
    		theVDB = clientAccessor.getClient().getVDB(vdbName,1);
    	} catch (AdminApiClientException e) {
    		throw new DataVirtUiException(e.getMessage());
    	}

    	// Clone the View Models and get the new VDB
    	VDBMetaData newVdb = vdbHelper.cloneViewModel(theVDB, viewModelName);
    	
    	// Re-Deploy the VDB
    	redeployVDB(vdbName, newVdb);
    	
    	// Return details
    	return getVdbDetails(vdbName, modelsPageNumber);
    }

    private List<String> getSrcVdbsToUndeploy(Map<String,String> removeModelNameAndTypeMap) {
    	List<String> srcVdbsToUndeploy = new ArrayList<String>();
    	for(String modelName : removeModelNameAndTypeMap.keySet()) {
    		String modelType = removeModelNameAndTypeMap.get(modelName);
    		if(modelType.equalsIgnoreCase(Constants.PHYSICAL)) {
    			String srcVdbDeploymentName = modelName + Constants.DYNAMIC_VDB_SUFFIX;
    			srcVdbsToUndeploy.add(srcVdbDeploymentName);
    		}
    	}
    	return srcVdbsToUndeploy;
    }
    
}
