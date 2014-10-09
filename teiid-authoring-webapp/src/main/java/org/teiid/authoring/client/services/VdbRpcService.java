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
package org.teiid.authoring.client.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.teiid.authoring.client.services.rpc.DelegatingErrorCallback;
import org.teiid.authoring.client.services.rpc.DelegatingRemoteCallback;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.teiid.authoring.share.exceptions.DataVirtUiException;
import org.teiid.authoring.share.services.IVdbService;

/**
 * Client-side service for making RPC calls to the remote search service.
 *
 * @author mdrillin@redhat.com
 */
@ApplicationScoped
public class VdbRpcService {

    @Inject
    private Caller<IVdbService> remoteVdbService;

    /**
     * Constructor.
     */
    public VdbRpcService() {
    }

    /**
     * Performs the search using the remote service.  Hides the RPC details from
     * the caller.
     * @param searchText
     * @param page
     * @param sortColumnId
     * @param sortAscending
     * @param handler
     */
//    public void search(String searchText, int page, boolean showDataVirtUiVDBs, String sortColumnId, boolean sortAscending,
//            final IRpcServiceInvocationHandler<VdbResultSetBean> handler) {
//        // TODO only allow one search at a time.  If another search comes in before the previous one
//        // finished, cancel the previous one.  In other words, only return the results of the *last*
//        // search performed.
//        RemoteCallback<VdbResultSetBean> successCallback = new DelegatingRemoteCallback<VdbResultSetBean>(handler);
//        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
//        try {
//        	remoteVdbService.call(successCallback, errorCallback).search(searchText, page, showDataVirtUiVDBs, sortColumnId, sortAscending);
//        } catch (DataVirtUiException e) {
//            errorCallback.error(null, e);
//        }
//    }
    
    public void getDynamicVdbsWithPrefix(String vdbPrefix, final IRpcServiceInvocationHandler<List<VdbDetailsBean>> handler) {
        RemoteCallback<List<VdbDetailsBean>> successCallback = new DelegatingRemoteCallback<List<VdbDetailsBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).getDynamicVdbsWithPrefix(vdbPrefix);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getVdbDetails(String vdbName, final IRpcServiceInvocationHandler<VdbDetailsBean> handler) {
        RemoteCallback<VdbDetailsBean> successCallback = new DelegatingRemoteCallback<VdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).getVdbDetails(vdbName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getVdbDetails(String vdbName, int modelsPage, final IRpcServiceInvocationHandler<VdbDetailsBean> handler) {
        RemoteCallback<VdbDetailsBean> successCallback = new DelegatingRemoteCallback<VdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).getVdbDetails(vdbName,modelsPage);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getTranslatorsForSrcVdbs(List<String> vdbNames, final IRpcServiceInvocationHandler<List<String>> handler) {
        RemoteCallback<List<String>> successCallback = new DelegatingRemoteCallback<List<String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).getTranslatorsForSrcVdbs(vdbNames);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void getVdbXml(String vdbName, final IRpcServiceInvocationHandler<String> handler) {
        RemoteCallback<String> successCallback = new DelegatingRemoteCallback<String>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).getVdbXml(vdbName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void createAndDeployDynamicVdb(String deploymentName, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).createAndDeployDynamicVdb(deploymentName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void deploySourceVDBAddImportAndRedeploy(String vdbName, int modelsPageNumber, String sourceVDBName, String modelName, String dataSourceName, String translator, 
    		final IRpcServiceInvocationHandler<VdbDetailsBean> handler) {
        RemoteCallback<VdbDetailsBean> successCallback = new DelegatingRemoteCallback<VdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).deploySourceVDBAddImportAndRedeploy(vdbName, modelsPageNumber, sourceVDBName, modelName, dataSourceName, translator);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void delete(Collection<String> vdbNames, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).delete(vdbNames);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void deleteDynamicVdbsWithPrefix(String vdbPrefix, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).deleteDynamicVdbsWithPrefix(vdbPrefix);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void addOrReplaceViewModelAndRedeploy(final String vdbName, final int modelsPageNumber, final ViewModelRequestBean viewModelRequest, final IRpcServiceInvocationHandler<VdbDetailsBean> handler) {
        RemoteCallback<VdbDetailsBean> successCallback = new DelegatingRemoteCallback<VdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).addOrReplaceViewModelAndRedeploy(vdbName, modelsPageNumber,viewModelRequest);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void deployNewVDB(final String vdbName, final int vdbVersion, final ViewModelRequestBean viewModelRequest, final IRpcServiceInvocationHandler<VdbDetailsBean> handler) {
        RemoteCallback<VdbDetailsBean> successCallback = new DelegatingRemoteCallback<VdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).deployNewVDB(vdbName, vdbVersion,viewModelRequest);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void removeModelsAndRedeploy(String vdbName, int modelsPageNumber, Map<String,String> removeModelNameAndTypeMap, 
    		final IRpcServiceInvocationHandler<VdbDetailsBean> handler) {
        RemoteCallback<VdbDetailsBean> successCallback = new DelegatingRemoteCallback<VdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).removeModelsAndRedeploy(vdbName, modelsPageNumber,removeModelNameAndTypeMap);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void cloneViewModelAndRedeploy(String vdbName, int modelsPageNumber, String viewModelName, 
    		final IRpcServiceInvocationHandler<VdbDetailsBean> handler) {
        RemoteCallback<VdbDetailsBean> successCallback = new DelegatingRemoteCallback<VdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteVdbService.call(successCallback, errorCallback).cloneViewModelAndRedeploy(vdbName, modelsPageNumber,viewModelName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

}
