package org.teiid.authoring.backend.server.services.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.teiid.adminapi.Model;
import org.teiid.adminapi.VDB;
import org.teiid.adminapi.VDB.Status;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBImportMetadata;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.adminapi.impl.VDBMetadataParser;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.VdbModelBean;
import org.teiid.authoring.share.exceptions.DataVirtUiException;

/**
 * Contains methods for working with VDBs
 */
public class VdbHelper {

	// ============================================
	// Static Variables

	private static VdbHelper instance = new VdbHelper();

	// ============================================
	// Static Methods
	/**
	 * Get the singleton instance
	 *
	 * @return instance
	 */
	public static VdbHelper getInstance() {
		return instance;
	}

	/*
	 * Create a VdbHelper
	 */
	private VdbHelper() {
	}

	/**
	 * Create a VDB object
	 * @param vdbName the name of the VDB
	 * @param vdbVersion the vdb version
	 * @return the VDBMetadata
	 */
	public VDBMetaData createVdb(String vdbName, int vdbVersion) {
		VDBMetaData vdb = new VDBMetaData();
		vdb.setName(vdbName);
		vdb.setDescription("VDB for: "+vdbName+", Version: "+vdbVersion);
		vdb.setVersion(vdbVersion);
		return vdb;
	}

	/**
	 * Get the bytearray version of the VDBMetaData object
	 * @param vdb the VDB
	 * @return the vdb in bytearray form
	 */
	public byte[] getVdbByteArray(VDBMetaData vdb) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		VDBMetadataParser.marshell(vdb, out);

		return out.toByteArray();
	}

	/**
	 * Get the stringified version of the VDBMetaData object
	 * @param vdb the VDB
	 * @return the vdb in string form
	 */
	public String getVdbString(VDBMetaData vdb) throws Exception {
		return new String(getVdbByteArray(vdb));
	}

	/**
	 * Create a VDB import object
	 * @param vdbName the name of the VDB to import
	 * @param vdbVersion the vdb version
	 * @return the VDBImportMetadata
	 */
	public VDBImportMetadata createVdbImport(String vdbName, int vdbVersion) {
		VDBImportMetadata vdbImport = new VDBImportMetadata();
		vdbImport.setName(vdbName);
		vdbImport.setVersion(vdbVersion);
		return vdbImport;
	}

	/**
	 * Create a Source Model
	 * @param modelName the name of the Model
	 * @param sourceMappingName the name of the source mapping
	 * @param jndiName the jndi name
	 * @param translator the translator name
	 * @return the ModelMetaData
	 */
	public ModelMetaData createSourceModel(String modelName, String sourceMappingName, String jndiName, String translator) {
		ModelMetaData modelMetaData = new ModelMetaData();
		modelMetaData.addSourceMapping(sourceMappingName, translator, jndiName);
		modelMetaData.setName(modelName);
		return modelMetaData;
	}

	/**
	 * Create a View Model
	 * @param modelName the name of the Model
	 * @param ddl the DDL which defines the view
	 * @param isVisible 'true' if the model is to be visible, 'false' if not.
	 * @return the ModelMetaData
	 */
	public ModelMetaData createViewModel(String modelName, String description, String ddl, boolean isVisible) {
		ModelMetaData modelMetaData = new ModelMetaData();
		modelMetaData.setName(modelName);
		modelMetaData.setDescription(description);
		modelMetaData.setModelType(Model.Type.VIRTUAL);
		modelMetaData.setSchemaSourceType("DDL");
		modelMetaData.setSchemaText(ddl);
		modelMetaData.setVisible(isVisible);
		return modelMetaData;
	}
	
    /**
     * Get ModelInfo for the models in the specified VDB
     * @param vdb the vdb
     * @return the VdbDetailsBean
     */
	public VdbDetailsBean getVdbDetails(VDBMetaData vdb) {
		VdbDetailsBean vdbDetailsBean = new VdbDetailsBean();

		// Add a model to the vdb, then re-deploy it.
		if(vdb!=null) {
			// ------------
			// VDB Name
			// ------------
			vdbDetailsBean.setName(vdb.getName());
			
			// ------------
			// VDB Type
			// ------------
			if(vdb.isXmlDeployment()) {
				vdbDetailsBean.setType("dynamic");
			} else {
				vdbDetailsBean.setType("archive");				
			}

			// ------------
			// VDB Status
			// ------------
			String vdbStatus = getVdbStatus(vdb);

			// ------------
			// VDB Models
			// ------------
			List<Model> models = vdb.getModels();

			for(Model model: models) {
				VdbModelBean modelBean = new VdbModelBean();
				
				ModelMetaData modelMeta = (ModelMetaData)model;
				String modelName = modelMeta.getName();
				String modelDescription = modelMeta.getDescription();
				String modelType = modelMeta.getModelType().toString();
				boolean isVisible = modelMeta.isVisible();
				String jndiName = null;
				String translatorName = null;
				String modelStatus = null;
				String ddl = "";

				// Virtual Model, use placeholders for jndiName and translatorName
				if(modelType.equals(Model.Type.VIRTUAL.toString())) {
					jndiName = "-----";
					translatorName = "teiid";
					ddl = modelMeta.getSchemaText();
			    // Physical Model, get source info
				} else {
					List<String> sourceNames = modelMeta.getSourceNames();
					for(String sourceName: sourceNames) {
						jndiName = modelMeta.getSourceConnectionJndiName(sourceName);
						translatorName = modelMeta.getSourceTranslatorName(sourceName);
					}
				}
				
				// If this is not an XML Deployment, show the Status as Unknown
				if(!vdb.isXmlDeployment()) {
					modelStatus = Constants.STATUS_UNKNOWN;
			    // Is XML Deployment, look at model errors
				} else {
					List<String> errors = modelMeta.getValidityErrors();
					if(errors.size()==0) {
						modelStatus = Constants.STATUS_ACTIVE;
					} else {
						// There may be multiple errors - process the list...
						boolean connectionError = false;
						boolean validationError = false;
						boolean isLoading = false;
						// Iterate Errors and set status flags
						for(String error: errors) {
							if(error.indexOf("TEIID11009")!=-1 || error.indexOf("TEIID60000")!=-1 || error.indexOf("TEIID31097")!=-1) {
								connectionError=true;
							} else if(error.indexOf("TEIID31080")!=-1 || error.indexOf("TEIID31071")!=-1) {
								validationError=true;
							} else if(error.indexOf("TEIID50029")!=-1) {
								isLoading=true;
							}
						}
						// --------------------------------------------------
						// Set model status string according to errors found
						// --------------------------------------------------
						// Connection Error. Reset the VDB overall status, as it may say loading
						if(connectionError) {
							modelStatus = "INACTIVE: Data Source connection failed...";
						// Validation Error with View SQL
						} else if(validationError) {
							modelStatus = "INACTIVE: Validation Error with SQL";
						// Loading in progress
						} else if(isLoading) {
							modelStatus = "INACTIVE: Metadata loading in progress...";
						// Unknown - use generic message
						} else {
							modelStatus = "INACTIVE: unknown source issue";
						}
					}
				}
				modelBean.setName(modelName);
				modelBean.setDescription(modelDescription);
				modelBean.setType(modelType);
				modelBean.setJndiSource(jndiName);
				modelBean.setTranslator(translatorName);
				modelBean.setStatus(modelStatus);
				modelBean.setDdl(ddl);
				modelBean.setVisible(isVisible);
				
				vdbDetailsBean.addModel(modelBean);
			}
			vdbDetailsBean.setStatus(vdbStatus);
		}

		return vdbDetailsBean;
	}
	
    /**
     * Construct a Source VDBName to be used as VDB Import
     * @param vdbName the VDB name
     * @param modelName the Model name
     */
    public String getSourceVDBName(String vdbName, String modelName) {
    	return Constants.SOURCE_VDB_PREFIX + "-" + vdbName + "-" + modelName;
    }
    
	/**
	 * Get the VDB Deployment name
	 * @param vdb the VDBMetaData
	 * @return the deployment name
	 */
	public String getVdbDeploymentName(VDBMetaData vdb) {
		String deploymentName = null;
		if(vdb!=null) {
			deploymentName = vdb.getPropertyValue("deployment-name");
		}
		return deploymentName;
	}
	
	/**
	 * Get the VDB Status that will be displayed in the UI.  This may be different from the VDBMetaData object status to simplify
	 * @param vdb the VDBMetaData
	 * @return the vdb status
	 */
	public String getVdbStatus(VDBMetaData vdb) {
		VDB.Status status = vdb.getStatus();
		String vdbStatus = Constants.STATUS_UNKNOWN;
		
		// Change FAILED, REMOVED, LOADING status to INACTIVE
		if(status!=null) {
			vdbStatus = status.toString();
			if( status==VDB.Status.FAILED || status==VDB.Status.REMOVED || status==VDB.Status.LOADING ) {
				vdbStatus=Constants.STATUS_INACTIVE;
			}
		}
		
		// If no models, change status to INACTIVE
		List<Model> models = vdb.getModels();
		if(models.isEmpty()) vdbStatus = Constants.STATUS_INACTIVE;
		
		return vdbStatus;
	}
	
    /**
     * Get the imports for the specified VDB
     * @param vdb the vdb
     * @return the List of VDBImportMetadata
     */
	public List<VDBImportMetadata> getVdbImports(VDBMetaData vdb) {
		if(vdb!=null) {
			List<VDBImportMetadata> vdbImports = new ArrayList<VDBImportMetadata>(vdb.getVDBImports());
			return vdbImports;
		} 

		return Collections.emptyList();
	}

    /**
     * Get the ViewModels for the specified VDB
     * @param vdb the vdb
     * @return the List of View Models
     */
	public List<ModelMetaData> getVdbViewModels(VDBMetaData vdb) {
		if(vdb!=null) {
			// Get current vdb ViewModels
			List<ModelMetaData> viewModels = new ArrayList<ModelMetaData>();

			List<Model> allModels = vdb.getModels();
			for(Model theModel : allModels) {
				if(theModel.getModelType()==Model.Type.VIRTUAL) {
					viewModels.add((ModelMetaData)theModel);
				}
			}
			return viewModels;
		}

		return Collections.emptyList();
	}
	
    /**
     * Get the Properties for the specified VDB
     * @param vdb the vdb
     * @return the Vdb Properties
     */
	public Properties getVdbProperties(VDBMetaData vdb) {
		if(vdb!=null) {
			return vdb.getProperties();
		}

		return new Properties();
	}
	
	/**
	 * Adds the Import to supplied VDB deployment. The new VDB is returned.
	 * @param vdb the VDB
	 * @param importVdbName the name of the VDB to import
	 * @param importVdbVersion the version of the VDB to import
	 * @return the new VDB
	 */
	public VDBMetaData addImports(VDBMetaData vdb, List<String> importVdbNames, List<Integer> importVdbVersions) {
		String vdbName = vdb.getName();
		int vdbVersion = vdb.getVersion();
		
		// Get current vdb imports
		List<VDBImportMetadata> currentVdbImports = getVdbImports(vdb);
		List<String> currentVdbImportNames = new ArrayList<String>();
		for(VDBImportMetadata vdbMeta : currentVdbImports) {
			currentVdbImportNames.add(vdbMeta.getName());
		}
		List<ModelMetaData> currentViewModels = getVdbViewModels(vdb);
		Properties currentProperties = getVdbProperties(vdb);

		// Clear any prior Model Messages (needed for successful redeploy)
		clearModelMessages(currentViewModels);

		// Create a new vdb
		VDBMetaData newVdb = createVdb(vdbName,vdbVersion);

		// Add the existing ViewModels
		newVdb.setModels(currentViewModels);

		// Transfer the existing properties
		newVdb.setProperties(currentProperties);

		// Add new import to current imports (if not already present)
		for(int i=0; i<importVdbNames.size(); i++) {
			String importToAdd = importVdbNames.get(i);
			if(!currentVdbImportNames.contains(importToAdd)) {
				currentVdbImports.add(createVdbImport(importVdbNames.get(i), importVdbVersions.get(i)));
			}
		}
		newVdb.getVDBImports().addAll(currentVdbImports);

		return newVdb;
	}
	
	/**
	 * Adds the ViewModel to supplied VDB deployment. The new VDB is returned.
	 * @param vdb the VDB
	 * @param viewModelName the name of the View Model
	 * @param ddlString the view DDL
	 * @param isVisible 'true' if the model is to be visible, 'false' if not.
	 * @return the new VDB
	 */
	public VDBMetaData addViewModel(VDBMetaData vdb, String viewModelName, String description, String ddlString, boolean isVisible) {
		String vdbName = vdb.getName();
		int vdbVersion = vdb.getVersion();
		
		// Get current vdb imports
		List<VDBImportMetadata> currentVdbImports = getVdbImports(vdb);
		List<ModelMetaData> currentViewModels = getVdbViewModels(vdb);
		Properties currentProperties = getVdbProperties(vdb);
		
		// If original VDB has view model with supplied name, remove it
		removeViewModel(currentViewModels, viewModelName);

		// Clear any prior Model Messages (needed for successful redeploy)
		clearModelMessages(currentViewModels);

		// Create a new vdb
		VDBMetaData newVdb = createVdb(vdbName,vdbVersion);

	    // Create View Model and add to current view models
	    ModelMetaData modelMetaData = createViewModel(viewModelName,description,ddlString,isVisible);
	    currentViewModels.add(modelMetaData);
	    
	    // Set ViewModels on new VDB
	    newVdb.setModels(currentViewModels);

		// Transfer the existing properties
		newVdb.setProperties(currentProperties);

		// Add new import to current imports
		newVdb.getVDBImports().addAll(currentVdbImports);

		return newVdb;
	}

	/**
	 * Removes the imports from the supplied VDB - if they exist. The new VDB is returned.
	 * @param vdb the VDB
	 * @param removeImportNameList the list of import names to remove
	 * @return the List of ImportInfo data
	 */
	public VDBMetaData removeImports(VDBMetaData vdb, List<String> removeImportNameList) {                
		String vdbName = vdb.getName();
		int vdbVersion = vdb.getVersion();
		
		// Get current vdb imports
		List<VDBImportMetadata> currentVdbImports = getVdbImports(vdb);
		List<ModelMetaData> currentViewModels = getVdbViewModels(vdb);
		Properties currentProperties = getVdbProperties(vdb);

		// Clear any prior Model Messages (needed for successful redeploy)
		clearModelMessages(currentViewModels);

		// Create a new vdb
		VDBMetaData newVdb = createVdb(vdbName,vdbVersion);

		// Add the existing ViewModels
		newVdb.setModels(currentViewModels);

		// Transfer the existing properties
		newVdb.setProperties(currentProperties);

		// Create import list for new model
		List<VDBImportMetadata> newImports = new ArrayList<VDBImportMetadata>();
		for(VDBImportMetadata vdbImport: currentVdbImports) {
			String currentName = vdbImport.getName();
			// Keep the import - unless its in the remove list
			if(!removeImportNameList.contains(currentName)) {
				newImports.add((VDBImportMetadata)vdbImport);
			}
		}
		newVdb.getVDBImports().addAll(newImports);

		return newVdb;
	}
	
	private void removeViewModel(List<ModelMetaData> viewModels, String viewModelName) {
		Iterator<ModelMetaData> modelIterator = viewModels.iterator();
		while(modelIterator.hasNext()) {
			ModelMetaData viewModel = modelIterator.next();
			if(viewModel.getName().equalsIgnoreCase(viewModelName)) {
				modelIterator.remove();
			}
		}
	}
	
	/**
	 * Removes the models from the supplied VDB - if they exist. The new VDB is returned.
	 * @param vdb the VDB
	 * @param removeModelNameAndTypeMap the Map of modelName to type
	 * @return the new VDB
	 */
	public VDBMetaData removeModels(VDBMetaData vdb, Map<String,String> removeModelNameAndTypeMap) {                
		String vdbName = vdb.getName();
		int vdbVersion = vdb.getVersion();
		
		// Sort the list into separate viewModel and sourceModel(Imports) lists
		List<String> removeViewModelNameList = new ArrayList<String>();
		List<String> removeImportNameList = new ArrayList<String>();
		for(String modelName : removeModelNameAndTypeMap.keySet()) {
			String modelType = removeModelNameAndTypeMap.get(modelName);
			if(modelType.equalsIgnoreCase(Constants.VIRTUAL)) {
				removeViewModelNameList.add(modelName);
			} else {
				removeImportNameList.add(modelName);
			}
		}

		// Get current vdb imports
		List<VDBImportMetadata> currentVdbImports = getVdbImports(vdb);
		List<ModelMetaData> currentViewModels = getVdbViewModels(vdb);
		Properties currentProperties = getVdbProperties(vdb);

		// Clear any prior Model Messages (needed for successful redeploy)
		clearModelMessages(currentViewModels);

		// Create a new vdb
		VDBMetaData newVdb = createVdb(vdbName,vdbVersion);

		// Determine list of view models
		List<ModelMetaData> newViewModels = new ArrayList<ModelMetaData>();
		for(Model model: currentViewModels) {
			String currentName = model.getName();
			// Keep the model - unless its in the remove list
			if(!removeViewModelNameList.contains(currentName)) {
				newViewModels.add((ModelMetaData)model);
			}
		}
		newVdb.setModels(newViewModels);

		// Transfer the existing properties
		newVdb.setProperties(currentProperties);

		// Create import list for new model
		List<VDBImportMetadata> newImports = new ArrayList<VDBImportMetadata>();
		for(VDBImportMetadata vdbImport: currentVdbImports) {
			String currentName = vdbImport.getName();
			// Keep the import - unless its in the remove list
			if(!removeImportNameList.contains(currentName)) {
				newImports.add((VDBImportMetadata)vdbImport);
			}
		}
		newVdb.getVDBImports().addAll(newImports);

		return newVdb;
	}
	
	/**
	 * Clone the supplied view models from the supplied VDB - if they exist. The new VDB is returned.
	 * @param vdb the VDB
	 * @param viewModelNames the List of View Model names to clone in the supplied vdb
	 * @return the new VDB
	 */
	public VDBMetaData cloneViewModel(VDBMetaData vdb, String viewModelName) {                
		String vdbName = vdb.getName();
		int vdbVersion = vdb.getVersion();
		
		// Get current vdb imports
		List<VDBImportMetadata> currentVdbImports = getVdbImports(vdb);
		List<ModelMetaData> currentViewModels = getVdbViewModels(vdb);
		List<String> existingViewNames = new ArrayList<String>(currentViewModels.size());
		for(ModelMetaData modelMeta : currentViewModels) {
			existingViewNames.add(modelMeta.getName());
		}
		Properties currentProperties = getVdbProperties(vdb);

		// Clear any prior Model Messages (needed for successful redeploy)
		clearModelMessages(currentViewModels);

		// Create a new vdb
		VDBMetaData newVdb = createVdb(vdbName,vdbVersion);

		// The new View Model list is all current models, plus clones
		List<ModelMetaData> newViewModels = new ArrayList<ModelMetaData>();
		// Iterate the list of names to clone
		for(Model model: currentViewModels) {
			ModelMetaData modelMeta = (ModelMetaData)model;
			// Add existing view model
			newViewModels.add(modelMeta);

			// Find the model and clone it
			String theModelName = model.getName();
			if(theModelName.equals(viewModelName)) {
				// Create View Model and add to current view models
				String newViewName = generateUniqueName(theModelName,existingViewNames);
				String description = model.getDescription();
				boolean isVisible = model.isVisible();
				String ddlString = modelMeta.getSchemaText();
				ModelMetaData modelMetaData = createViewModel(newViewName,description,ddlString,isVisible);

				newViewModels.add(modelMetaData);
			}
		}

		newVdb.setModels(newViewModels);

		// Transfer the existing properties
		newVdb.setProperties(currentProperties);

		// The imports are unchanged
		List<VDBImportMetadata> newImports = new ArrayList<VDBImportMetadata>();
		for(VDBImportMetadata vdbImport: currentVdbImports) {
			newImports.add((VDBImportMetadata)vdbImport);
		}
		newVdb.getVDBImports().addAll(newImports);

		return newVdb;
	}

	private String generateUniqueName(String origName, List<String> existingNames) {
		// If the name is already unique, return it.
		if(!existingNames.contains(origName)) {
			return origName;
		}
		// Iterate generating new names until a good one is found
		String newName = null;
		boolean success = false;
		int i = 1;
		while(!success) {
			if(i==1) {
			    newName = origName + "_copy";
			} else {
				newName = origName + "_copy" + i;
			}
			if(!existingNames.contains(newName)) {
				success=true;
			}
			i++;
		}
		return newName;
	}
	
	public String getVDBStatusMessage(VDBMetaData vdb) throws DataVirtUiException {
    	if(vdb!=null) {
    		Status vdbStatus = vdb.getStatus();
    		if(vdbStatus!=Status.ACTIVE) {
    			List<String> allErrors = vdb.getValidityErrors();
    			if(allErrors!=null && !allErrors.isEmpty()) {
    				StringBuffer sb = new StringBuffer();
    				for(String errorMsg : allErrors) {
    					sb.append("ERROR: " +errorMsg+"<br>");
    				}
    				return sb.toString();
    			}
    		}
    	}
    	return Constants.SUCCESS;
    }
	
	private void clearModelMessages(List<ModelMetaData> models) {
		for(ModelMetaData model: models) {
			model.clearMessages();
		}
	}

}

