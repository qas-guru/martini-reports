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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import guru.qas.martini.report.State;

@SuppressWarnings("WeakerAccess")
@Component
public class ExceptionColumn implements TraceabilityColumn {

	protected static final String LABEL = "Exception";
	protected static final String KEY_STEPS = "steps";
	protected static final String KEY_EXCEPTION = "exception";

	protected ExceptionColumn() {
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public void addResult(State state, HSSFCell cell, JsonObject o) {
		JsonArray array = o.getAsJsonArray(KEY_STEPS);
		int size = array.size();

		String value = null;
		for (int i = 0; null == value && i < size; i++) {
			JsonElement element = array.get(i);
			JsonObject step = element.getAsJsonObject();
			JsonPrimitive primitive = step.getAsJsonPrimitive(KEY_EXCEPTION);
			String stackTrace = null == primitive ? null : primitive.getAsString().trim();
			value = null != stackTrace && !stackTrace.isEmpty() ? stackTrace: null;
		}

		HSSFRichTextString richTextString = new HSSFRichTextString(value);
		cell.setCellValue(richTextString);
	}
}
