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
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import guru.qas.martini.report.State;

@SuppressWarnings("WeakerAccess")
@Component
public class LocationColumn implements TraceabilityColumn {

	protected static final String LABEL = "Location";
	protected static final String KEY_LINE = "line";
	protected static final String KEY_LOCATION = "location";

	protected LocationColumn() {
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public void addResult(State state, Cell cell, JsonObject o) {
		String line = getLine(o);
		String relative = getResource(state, o);

		StringBuilder builder = new StringBuilder(null == relative ? "" : relative);
		if (null != line) {
			int length = builder.length();
			builder.append(length > 0 ? " line " : "line ").append(line);
		}

		String value = builder.toString();
		RichTextString richTextString = new XSSFRichTextString(value);
		cell.setCellValue(richTextString);
		state.setStatus(cell, value);
	}

	protected String getLine(JsonObject o) {
		JsonElement element = o.get(KEY_LINE);
		return null == element ? null : element.getAsString();
	}

	protected String getResource(State state, JsonObject result) {
		JsonObject feature = state.getFeature(result);
		JsonElement element = feature.get(KEY_LOCATION);
		return null == element ? null : element.getAsString();
	}
}
