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
import org.teiid.authoring.share.beans.DataSourcePageRow;
import org.teiid.authoring.share.beans.DataSourcePropertyBean;
import org.teiid.authoring.share.beans.DataSourceWithVdbDetailsBean;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.teiid.authoring.share.exceptions.DataVirtUiException;
import org.teiid.authoring.share.services.ITeiidService;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

/**
 * Client-side service for making RPC calls to the remote DataSource service.
 *
 * @author mdrillin@redhat.com
 */
@ApplicationScoped
public class TeiidRpcService {

    @Inject
    private Caller<ITeiidService> remoteTeiidService;

    /**
     * Constructor.
     */
    public TeiidRpcService() {
    }

    public void getDataSourceWithVdbDetails(String dsName, final IRpcServiceInvocationHandler<DataSourceWithVdbDetailsBean> handler) {
        RemoteCallback<DataSourceWithVdbDetailsBean> successCallback = new DelegatingRemoteCallback<DataSourceWithVdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getDataSourceWithVdbDetails(dsName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void getDataSourceTypes(final IRpcServiceInvocationHandler<List<String>> handler) {
        RemoteCallback<List<String>> successCallback = new DelegatingRemoteCallback<List<String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getDataSourceTypes();
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getDataSourceTypeProperties(String dsType, final IRpcServiceInvocationHandler<List<DataSourcePropertyBean>> handler) {
        RemoteCallback<List<DataSourcePropertyBean>> successCallback = new DelegatingRemoteCallback<List<DataSourcePropertyBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getDataSourceTypeProperties(dsType);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    
    public void getDataSources(final String filter, final String srcVdbPrefix, final IRpcServiceInvocationHandler<List<DataSourcePageRow>> handler) {
        RemoteCallback<List<DataSourcePageRow>> successCallback = new DelegatingRemoteCallback<List<DataSourcePageRow>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getDataSources(filter,srcVdbPrefix);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getDataSources(final PageRequest request, final String filter, final IRpcServiceInvocationHandler<PageResponse<DataSourcePageRow>> handler) {
        RemoteCallback<PageResponse<DataSourcePageRow>> successCallback = new DelegatingRemoteCallback<PageResponse<DataSourcePageRow>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getDataSources(request,filter);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getDataSourceNames(final IRpcServiceInvocationHandler<List<String>> handler) {
        RemoteCallback<List<String>> successCallback = new DelegatingRemoteCallback<List<String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getDataSourceNames();
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
        
    public void getTranslators(final IRpcServiceInvocationHandler<List<String>> handler) {
        RemoteCallback<List<String>> successCallback = new DelegatingRemoteCallback<List<String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getTranslators();
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
   
    public void getQueryableDataSourceMap(final IRpcServiceInvocationHandler<Map<String,String>> handler) {
        RemoteCallback<Map<String,String>> successCallback = new DelegatingRemoteCallback<Map<String,String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getQueryableDataSourceMap();
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void getDefaultTranslatorMap(final IRpcServiceInvocationHandler<Map<String,String>> handler) {
        RemoteCallback<Map<String,String>> successCallback = new DelegatingRemoteCallback<Map<String,String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getDefaultTranslatorMap();
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void createDataSourceWithVdb(DataSourceWithVdbDetailsBean dataSourceWithVdb, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).createDataSourceWithVdb(dataSourceWithVdb);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void createVdbAndVdbSource(DataSourceWithVdbDetailsBean dataSourceWithVdb, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).createVdbAndVdbSource(dataSourceWithVdb);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void deleteDataSourceAndVdb(String dsName, String vdbName, final IRpcServiceInvocationHandler<List<VdbDetailsBean>> handler) {
        RemoteCallback<List<VdbDetailsBean>> successCallback = new DelegatingRemoteCallback<List<VdbDetailsBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).deleteDataSourceAndVdb(dsName,vdbName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void deleteDataSources(Collection<String> dsNames, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).deleteDataSources(dsNames);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void deployNewVDB(final String vdbName, final int vdbVersion, final ViewModelRequestBean viewModelRequest, final IRpcServiceInvocationHandler<VdbDetailsBean> handler) {
        RemoteCallback<VdbDetailsBean> successCallback = new DelegatingRemoteCallback<VdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).deployNewVDB(vdbName, vdbVersion,viewModelRequest);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getDynamicVdbsWithPrefix(String vdbPrefix, final IRpcServiceInvocationHandler<List<VdbDetailsBean>> handler) {
        RemoteCallback<List<VdbDetailsBean>> successCallback = new DelegatingRemoteCallback<List<VdbDetailsBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getDynamicVdbsWithPrefix(vdbPrefix);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getVdbDetails(String vdbName, final IRpcServiceInvocationHandler<VdbDetailsBean> handler) {
        RemoteCallback<VdbDetailsBean> successCallback = new DelegatingRemoteCallback<VdbDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).getVdbDetails(vdbName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void deleteDynamicVdbsWithPrefix(String vdbPrefix, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).deleteDynamicVdbsWithPrefix(vdbPrefix);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }   

    public void cloneDynamicVdbAddSource(String vdbName, int vdbVersion, final IRpcServiceInvocationHandler<List<VdbDetailsBean>> handler) {
        RemoteCallback<List<VdbDetailsBean>> successCallback = new DelegatingRemoteCallback<List<VdbDetailsBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteTeiidService.call(successCallback, errorCallback).cloneDynamicVdbAddSource(vdbName,vdbVersion);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
}