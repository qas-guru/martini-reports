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

import java.util.LinkedHashSet;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import guru.qas.martini.report.State;

@SuppressWarnings("WeakerAccess")
@Component
public class ThemeColumn implements TraceabilityColumn {

	protected static final String LABEL = "Themes";
	protected static final String KEY = "categories";

	protected ThemeColumn() {
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public void addResult(State state, HSSFCell cell, JsonObject o) {
		JsonArray categories = o.getAsJsonArray(KEY);
		if (null != categories) {
			doSomething(state, cell, categories);
		}
	}

	protected void doSomething(State state, HSSFCell cell, JsonArray categories) {
		int size = categories.size();

		LinkedHashSet<String> ordered = new LinkedHashSet<>();

		for (int i = 0; i < size; i++) {
			JsonElement element = categories.get(i);
			String category = element.getAsString();
			ordered.add(category);
		}

		String value = Joiner.on("\n").join(ordered);
		HSSFRichTextString richTextString = new HSSFRichTextString(value);
		cell.setCellValue(richTextString);

		state.setThemes(cell, ordered);
	}
}
