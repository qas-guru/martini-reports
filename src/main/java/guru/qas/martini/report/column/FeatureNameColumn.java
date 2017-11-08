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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import guru.qas.martini.report.State;

@SuppressWarnings("WeakerAccess")
@Component
public class FeatureNameColumn implements TraceabilityColumn {

	protected static final String LABEL = "Feature";
	protected static final String KEY_FEATURE = "feature";
	protected static final String KEY_NAME = "name";

	protected FeatureNameColumn() {
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public void addResult(State state, HSSFCell cell, JsonObject result) {
		JsonObject feature = state.getFeature(result);
		JsonElement element = null == feature ? null : feature.get(KEY_NAME);
		String name = null == element ? null : element.getAsString();
		HSSFRichTextString richTextString = new HSSFRichTextString(name);
		cell.setCellValue(richTextString);
	}
}
