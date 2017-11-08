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
import java.io.Reader;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.collect.ImmutableList;
import com.google.common.io.LineReader;
import com.google.gson.JsonObject;

import guru.qas.martini.report.column.TraceabilityColumn;


@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultTraceabilityMatrix implements TraceabilityMatrix {

	protected final ImmutableList<TraceabilityColumn> columns;

	@Autowired
	protected DefaultTraceabilityMatrix(Iterable<TraceabilityColumn> columns) {
		this.columns = ImmutableList.copyOf(columns);
	}

	@Override
	public void createReport(Reader reader, OutputStream outputStream) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Results");
		addHeader(sheet);

		LineReader lineReader = new LineReader(reader);
		State state = new DefaultState();
		for (String line = lineReader.readLine(); null != line; ) {
			MartiniProcessor martiniProcessor = new MartiniProcessor();
			while (null != line && martiniProcessor.processLine(line)) {
				line = lineReader.readLine();
			}
			JsonObject result = martiniProcessor.getResult();
			addResult(state, sheet, result);
			line = null == line ? null : lineReader.readLine();
		}


		state.updateResults();
		resizeColumns(sheet);

		HSSFSheet suiteSheet = workbook.createSheet("Suites");
		state.updateSuites(suiteSheet);

		workbook.write(outputStream);
		outputStream.flush();
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
