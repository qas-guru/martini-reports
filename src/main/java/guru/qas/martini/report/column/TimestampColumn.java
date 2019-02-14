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

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import guru.qas.martini.report.State;

@SuppressWarnings("WeakerAccess")
@Component
public class TimestampColumn implements TraceabilityColumn {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TimestampColumn.class);

	protected static final String LABEL = "Timestamp";
	protected static final String KEY = "startTimestamp";

	protected TimestampColumn() {
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public void addResult(State state, Cell cell, JsonObject o) {
		JsonPrimitive primitive = o.getAsJsonPrimitive(KEY);
		if (null != primitive) {
			addResult(cell, primitive);
		}
	}

	protected void addResult(Cell cell, JsonPrimitive primitive) {
		String timestamp = primitive.getAsString();

		String value;
		try {
			Date date = new Date(Long.parseLong(timestamp));
			value = String.format("%s\n(%s)", timestamp, date);
		}
		catch (NumberFormatException e) {
			value = timestamp;
			LOGGER.warn("unable to parse '{}' to a long", timestamp, e);
		}

		RichTextString richTextString = new XSSFRichTextString(value);
		cell.setCellValue(richTextString);
	}
}
