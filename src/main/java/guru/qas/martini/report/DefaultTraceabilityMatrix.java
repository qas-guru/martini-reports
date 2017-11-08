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

package guru.qas.martini.report;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import guru.qas.martini.report.column.TraceabilityColumn;

import static com.google.common.base.Preconditions.checkNotNull;
import static guru.qas.martini.report.JsonObjectType.*;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultTraceabilityMatrix implements TraceabilityMatrix {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTraceabilityMatrix.class);

	protected final Gson gson;
	protected final ImmutableList<TraceabilityColumn> columns;

	@Autowired
	protected DefaultTraceabilityMatrix(Gson gson, Iterable<TraceabilityColumn> columns) {
		this.gson = gson;
		this.columns = ImmutableList.copyOf(columns);
	}

	@Override
	public void createReport(JsonReader reader, OutputStream outputStream) throws IOException {
		checkNotNull(reader, "null JsonReader");
		checkNotNull(outputStream, "null OutputStream");

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Results");
		addHeader(sheet);

		State state = new DefaultState();
		while (reader.hasNext() && !isEndOfDocument(reader)) {
			JsonElement next = gson.fromJson(reader, JsonElement.class);
			if (null != next && next.isJsonObject()) {
				JsonObject object = next.getAsJsonObject();

				switch(JsonObjectType.evaluate(object)) {
					case SUITE:
						JsonObject suite = SUITE.get(object);
						state.addSuite(suite);
						break;
					case FEATURE:
						JsonObject feature = FEATURE.get(object);
						state.addFeature(feature);
						break;
					case RESULT:
						JsonObject result = RESULT.get(object);
						addResult(state, sheet, result);
						break;
					default:
						LOGGER.warn("skipping unrecognized JsonObject: {}", object);
				}
			}
		}

		state.updateResults();
		resizeColumns(sheet);

		HSSFSheet suiteSheet = workbook.createSheet("Suites");
		state.updateSuites(suiteSheet);

		workbook.write(outputStream);
		outputStream.flush();
	}

	protected boolean isEndOfDocument(JsonReader reader) throws IOException {
		JsonToken token = reader.peek();
		return JsonToken.END_DOCUMENT == token;
	}
	protected void addHeader(HSSFSheet sheet) {
		HSSFRow row = sheet.createRow(0);

		HSSFWorkbook workbook = sheet.getWorkbook();
		HSSFCellStyle style = getHeaderStyle(workbook);

		for (int i = 0; i < columns.size(); i++) {
			TraceabilityColumn column = columns.get(i);
			HSSFCell cell = row.createCell(i, CellType.STRING);
			String label = column.getLabel();
			cell.setCellValue(label);
			cell.setCellStyle(style);
		}
		sheet.createFreezePane(0, 1);
	}

	protected HSSFCellStyle getHeaderStyle(HSSFWorkbook workbook) {
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderBottom(BorderStyle.MEDIUM);
		HSSFFont headerFont = getHeaderFont(workbook);
		style.setFont(headerFont);
		return style;
	}

	protected HSSFFont getHeaderFont(HSSFWorkbook workbook) {
		HSSFFont font = workbook.findFont(
			true, // bold
			IndexedColors.BLACK.getIndex(),
			(short) 300,
			HSSFFont.FONT_ARIAL,
			false, // italic
			false, // strikeout
			HSSFFont.SS_NONE,
			HSSFFont.U_NONE);

		if (null == font) {
			font = workbook.createFont();
			font.setBold(true);
			font.setColor(IndexedColors.BLACK.getIndex());
			font.setFontHeight((short) 300);
			font.setFontName(HSSFFont.FONT_ARIAL);
			font.setItalic(false);
			font.setStrikeout(false);
			font.setTypeOffset(HSSFFont.SS_NONE);
			font.setUnderline(HSSFFont.U_NONE);
		}
		return font;
	}

	protected void addResult(State state, HSSFSheet sheet, JsonObject object) {
		int index = sheet.getLastRowNum();
		HSSFRow row = sheet.createRow(index + 1);

		for (int i = 0; i < columns.size(); i++) {
			HSSFCell cell = row.createCell(i);
			HSSFCellStyle cellStyle = cell.getCellStyle();
			cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
			TraceabilityColumn column = columns.get(i);
			column.addResult(state, cell, object);
		}
	}

	protected void resizeColumns(HSSFSheet sheet) {
		for (int i = 0; i < columns.size(); i++) {
			sheet.autoSizeColumn(i, false);
		}
	}
}
