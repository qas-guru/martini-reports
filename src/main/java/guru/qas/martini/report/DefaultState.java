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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

@SuppressWarnings("WeakerAccess")
public class DefaultState implements State {

	private final Multimap<String, HSSFCell> statii;
	private final Multimap<String, HSSFCell> themes;
	private final Set<JsonObject> suites;

	private List<HSSFCell> longestExecutionCells;
	private long longestExecution;

	protected DefaultState() {
		statii = ArrayListMultimap.create();
		themes = ArrayListMultimap.create();
		suites = Sets.newHashSet();
		longestExecutionCells = new ArrayList<>();
		longestExecution = 0;
	}

	@Override
	public void setStatus(HSSFCell cell, String status) {
		statii.put(status, cell);
	}

	@Override
	public void setThemes(HSSFCell cell, Iterable<String> i) {
		for (String theme : i) {
			themes.put(theme, cell);
		}
	}

	@Override
	public void setExecutionTime(HSSFCell cell, long executionTime) {
		if (executionTime == longestExecution) {
			longestExecutionCells.add(cell);
		}
		else if (executionTime > longestExecution) {
			longestExecution = executionTime;
			longestExecutionCells.clear();
			longestExecutionCells.add(cell);
		}
	}

	@Override
	public void updateResults() {
		updateLongestExecutions();
		colorRowsByStatus();
	}

	public void updateLongestExecutions() {
		if (!longestExecutionCells.isEmpty()) {
			for (HSSFCell cell : longestExecutionCells) {
				HSSFCellStyle original = cell.getCellStyle();
				HSSFSheet sheet = cell.getSheet();
				HSSFWorkbook workbook = sheet.getWorkbook();
				HSSFCellStyle newStyle = workbook.createCellStyle();
				newStyle.cloneStyleFrom(original);

				HSSFFont font = workbook.createFont();
				font.setBold(true);
				font.setColor(IndexedColors.ORANGE.getIndex());
				newStyle.setFont(font);
				cell.setCellStyle(newStyle);

				HSSFRow row = cell.getRow();
				short firstCellNum = row.getFirstCellNum();
				short lastCellNum = row.getLastCellNum();

				for (int i = firstCellNum; i < lastCellNum; i++) {
					HSSFCell rowCell = row.getCell(i);
					original = rowCell.getCellStyle();
					HSSFCellStyle borderStyle = workbook.createCellStyle();
					borderStyle.cloneStyleFrom(original);
					borderStyle.setBorderTop(BorderStyle.MEDIUM);
					borderStyle.setBorderBottom(BorderStyle.MEDIUM);

					if (i == cell.getColumnIndex()) {
						borderStyle.setBorderLeft(BorderStyle.MEDIUM);
						borderStyle.setBorderRight(BorderStyle.MEDIUM);
					}
					else if (i == firstCellNum) {
						borderStyle.setBorderLeft(BorderStyle.MEDIUM);
					}
					else if (i == lastCellNum - 1) {
						borderStyle.setBorderRight(BorderStyle.MEDIUM);
					}
					rowCell.setCellStyle(borderStyle);
				}
			}
		}
	}

	public void colorRowsByStatus() {
		Map<String, Collection<HSSFCell>> statusMap = statii.asMap();
		for (Map.Entry<String, Collection<HSSFCell>> mapEntry : statusMap.entrySet()) {
			String status = mapEntry.getKey();

			Short color = null;
			switch (status) {
				case "SKIPPED":
					color = IndexedColors.LEMON_CHIFFON.getIndex();
					break;
				case "PASSED":
					color = IndexedColors.LIGHT_TURQUOISE.getIndex();
					break;
				case "FAILED":
					color = IndexedColors.ROSE.getIndex();
					break;
			}

			if (null != color) {
				Collection<HSSFCell> statusCells = mapEntry.getValue();
				colorRows(color, statusCells);
			}
		}
	}

	protected void colorRows(short color, Iterable<HSSFCell> cells) {
		for (HSSFCell cell : cells) {
			HSSFRow row = cell.getRow();
			colorRow(color, row);

		}
	}

	protected void colorRow(short color, HSSFRow row) {
		short firstCellNum = row.getFirstCellNum();
		short lastCellNum = row.getLastCellNum();
		for (int i = firstCellNum; i <= lastCellNum; i++) {
			HSSFCell cell = row.getCell(i - 1);
			if (null != cell) {
				HSSFCellStyle cellStyle = cell.getCellStyle();
				HSSFWorkbook workbook = cell.getSheet().getWorkbook();
				HSSFCellStyle clone = workbook.createCellStyle();
				clone.cloneStyleFrom(cellStyle);
				clone.setFillForegroundColor(color);
				clone.setFillPattern(FillPatternType.SOLID_FOREGROUND);

				BorderStyle borderStyle = cellStyle.getBorderLeftEnum();
				clone.setBorderLeft(BorderStyle.NONE == borderStyle ? BorderStyle.THIN : borderStyle);
				short borderColor = cellStyle.getLeftBorderColor();
				clone.setLeftBorderColor(0 == borderColor ? IndexedColors.BLACK.getIndex() : borderColor);


				borderStyle = cellStyle.getBorderRightEnum();
				clone.setBorderRight(BorderStyle.NONE == borderStyle ? BorderStyle.THIN : borderStyle);
				borderColor = cellStyle.getRightBorderColor();
				clone.setRightBorderColor(0 == borderColor ? IndexedColors.BLACK.getIndex() : borderColor);


				borderStyle = cellStyle.getBorderTopEnum();
				clone.setBorderTop(BorderStyle.NONE == borderStyle ? BorderStyle.THIN : borderStyle);
				borderColor = cellStyle.getTopBorderColor();
				clone.setTopBorderColor(0 == borderColor ? IndexedColors.BLACK.getIndex() : borderColor);


				borderStyle = cellStyle.getBorderBottomEnum();
				clone.setBorderBottom(BorderStyle.NONE == borderStyle ? BorderStyle.THIN : borderStyle);
				borderColor = cellStyle.getBottomBorderColor();
				clone.setBottomBorderColor(borderColor);
				cell.setCellStyle(clone);
			}
		}
	}

	@Override
	public void addSuite(JsonObject suite) {
		suites.add(suite);
	}

	@Override
	public void updateSuites(HSSFSheet sheet) {
	}
}
