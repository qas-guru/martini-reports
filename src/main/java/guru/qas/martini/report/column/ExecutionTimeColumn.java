/*
Copyright 2017 Penny Rohr Curich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package guru.qas.martini.report.column;

import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import guru.qas.martini.report.State;

@SuppressWarnings("WeakerAccess")
@Component
public class ExecutionTimeColumn implements TraceabilityColumn {

	protected static final String LABEL = "Execution (ms)";
	protected static final String KEY_START = "startTimestamp";
	protected static final String KEY_END = "endTimestamp";

	protected ExecutionTimeColumn() {
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public void addResult(State state, Cell cell, JsonObject o) {
		Long start = getTimestamp(o, KEY_START);
		Long end = null == start ? null : getTimestamp(o, KEY_END);
		Long executionTime = null == end ? null : end - start;
		doSomething(state, cell, executionTime);

	}

	protected Long getTimestamp(JsonObject o, String key) {
		JsonElement element = o.get(key);
		return null == element ? null : element.getAsLong();
	}

	protected void doSomething(State state, Cell cell, Long executionTime) {
		if (null != executionTime) {
			cell.setCellValue(executionTime);
			state.setExecutionTime(cell, executionTime);
		}
	}
}
