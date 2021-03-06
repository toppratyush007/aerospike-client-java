/*
 * Copyright 2012-2016 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements WHICH ARE COMPATIBLE WITH THE APACHE LICENSE, VERSION 2.0.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aerospike.client.async;

import java.util.ArrayList;
import java.util.Map;

import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.policy.WritePolicy;

public final class AsyncOperate extends AsyncRead {
	private final WritePolicy writePolicy;
	private final Operation[] operations;
	
	public AsyncOperate(AsyncCluster cluster, WritePolicy policy, RecordListener listener, Key key, Operation[] operations) {
		super(cluster, policy, listener, key, null);
		this.writePolicy = policy;
		this.operations = operations;
	}

	@Override
	protected void writeBuffer() {
		setOperate(writePolicy, key, operations);
	}
	
	@Override
	protected AsyncNode getNode() {	
		return (AsyncNode)cluster.getMasterNode(partition);
	}
	
	@Override
	protected void addBin(Map<String,Object> bins, String name, Object value) {
		if (bins.containsKey(name)) {
			// Multiple values returned for the same bin. 
			Object prev = bins.get(name);
			
			if (prev instanceof OpResults) {
				// List already exists.  Add to it.
				OpResults list = (OpResults)prev;
				list.add(value);					
			}
			else {
				// Make a list to store all values.
				OpResults list = new OpResults();
				list.add(prev);
				list.add(value);
				bins.put(name, list);
			}
		}
		else {
			bins.put(name, value);
		}
	}
	
	private static class OpResults extends ArrayList<Object> {
		private static final long serialVersionUID = 1L;
	}
}
