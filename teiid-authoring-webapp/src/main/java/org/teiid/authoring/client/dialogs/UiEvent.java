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

public class UiEvent {
	
	private UiEventType type;
	private String dataSourceName;
	private String dataServiceName;
	private String eventSource;
	
	public UiEvent(UiEventType type) {
		this.type = type;
	}
	
	public UiEventType getType() {
		return type;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	
	public String getDataServiceName() {
		return dataServiceName;
	}

	public void setDataServiceName(String dataServiceName) {
		this.dataServiceName = dataServiceName;
	}

	public String getEventSource() {
		return eventSource;
	}

	public void setEventSource(String eventSource) {
		this.eventSource = eventSource;
	}
}
