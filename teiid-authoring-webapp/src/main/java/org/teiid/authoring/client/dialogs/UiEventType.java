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
package org.teiid.authoring.client.dialogs;

public enum UiEventType {
    CREATE_SOURCE_OK, 
    CREATE_SOURCE_CANCEL,
    DELETE_SOURCE_OK, 
    DELETE_SOURCE_CANCEL,
    DELETE_SERVICE_OK, 
    DELETE_SERVICE_CANCEL,
    SAVE_SERVICE,
    EDIT_SERVICE_ABORT_OK,
    EDIT_SERVICE_ABORT_CANCEL,
    ADD_VIEW_SOURCE_OK,
    ADD_VIEW_SOURCE_CANCEL,
    VIEW_SOURCES_CHANGED,
    SOURCE_RENAME_OK, 
    SOURCE_RENAME_CANCEL,
    SOURCE_REDEPLOY_OK, 
    SOURCE_REDEPLOY_CANCEL,
    SOURCE_CHANGETYPE_OK,
    SOURCE_CHANGETYPE_CANCEL,
    VIEW_DEFN_REPLACE_OK,
    VIEW_DEFN_REPLACE_CANCEL,
    VIEW_EDITOR_CHANGED,
    VIEW_EDITOR_GOTO_MANAGE_SOURCES,
    DATA_SOURCE_ADD,
    DATA_SOURCE_DELETE,
    DATA_SOURCE_DEPLOY_STARTING,
    DATA_SOURCE_DEPLOY_SUCCESS,
    DATA_SOURCE_DEPLOY_FAIL,
    QUERY_RESULT_DISPLAYER_REFRESHED_OK, 
    QUERY_RESULT_DISPLAYER_REFRESHED_NOROWS,
    QUERY_RESULT_DISPLAYER_REFRESHED_ERROR;
}
