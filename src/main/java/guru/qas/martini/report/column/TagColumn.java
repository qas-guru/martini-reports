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

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import guru.qas.martini.report.State;

@SuppressWarnings("WeakerAccess")
@Component
public class TagColumn implements TraceabilityColumn {

	protected static final String LABEL = "Tags";
	protected static final String KEY_TAGS = "tags";
	protected static final String KEY_NAME = "name";
	protected static final String KEY_ARGUMENT = "argument";

	protected TagColumn() {
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public void addResult(State state, HSSFCell cell, JsonObject o) {
		JsonArray array = o.getAsJsonArray(KEY_TAGS);
		if (null != array) {
			addResult(cell, array);
		}
	}

	protected void addResult(HSSFCell cell, JsonArray array) {

		int size = array.size();
		List<String> tags = Lists.newArrayListWithExpectedSize(size);
		for (int i = 0; i < size; i++) {
			JsonElement element = array.get(i);
			String tag = getTag(element);
			tags.add(tag);
		}

		String value = Joiner.on('\n').skipNulls().join(tags);
		HSSFRichTextString richTextString = new HSSFRichTextString(value);
		cell.setCellValue(richTextString);
	}

	protected String getTag(JsonElement element) {
		JsonObject entry = element.getAsJsonObject();
		JsonElement nameElement = entry.get(KEY_NAME);
		String name = null == nameElement ? null : nameElement.getAsString();

		String tag = null;
		if (null != name) {
			JsonElement argumentElement = entry.get(KEY_ARGUMENT);
			String argument = null == argumentElement ? "" : String.format("\"%s\"", argumentElement.getAsString());
			tag = String.format("@%s(%s)", name, argument);
		}
		return tag;
	}
}
