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
import java.util.Iterator;
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

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
		colorCompromisedThemes();
	}

	public void updateLongestExecutions() {
		if (!longestExecutionCells.isEmpty()) {
			for (HSSFCell cell : longestExecutionCells) {
				HSSFCellStyle original = cell.getCellStyle();
				HSSFSheet sheet = cell.getSheet();
				HSSFWorkbook workbook = sheet.getWorkbook();
				HSSFCellStyle newStyle = workbook.createCellStyle();
				newStyle.cloneStyleFrom(original);
				HSSFFont originalFont = original.getFont(workbook);

				HSSFFont font = workbook.createFont();
				font.setBold(true);
				font.setColor(IndexedColors.ORANGE.getIndex());
				font.setFontHeight((short) Math.round(originalFont.getFontHeight() * 1.5));
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
					color = IndexedColors.TAN.getIndex();
					break;
				case "PASSED":
					color = IndexedColors.LIME.getIndex();
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

	protected void colorCompromisedThemes() {
		Collection<HSSFCell> failed = statii.get("FAILED");

		if (!failed.isEmpty()) {
			List<HSSFRow> rows = Lists.newArrayListWithExpectedSize(failed.size());
			for (HSSFCell cell : failed) {
				HSSFRow row = cell.getRow();
				rows.add(row);
			}

			Set<HSSFCell> compromisedThemeCells = Sets.newHashSet();

			Map<String, Collection<HSSFCell>> themeMap = themes.asMap();
			for (Map.Entry<String, Collection<HSSFCell>> mapEntry : themeMap.entrySet()) {
				Collection<HSSFCell> themeCells = mapEntry.getValue();

				boolean compromised = false;
				for (Iterator<HSSFCell> iterator = themeCells.iterator(); !compromised && iterator.hasNext();) {
					HSSFCell themeCell = iterator.next();
					HSSFRow row = themeCell.getRow();
					compromised = rows.contains(row);
				}

				if (compromised) {
					compromisedThemeCells.addAll(themeCells);
				}
			}

			Set<String> compromisedThemes = Sets.newHashSet();
			for (HSSFCell themeCell : compromisedThemeCells) {
				String contents = themeCell.getStringCellValue();
				if (null != contents) {
					Iterable<String> themes = Splitter.onPattern("\\s+").omitEmptyStrings().split(contents);
					Iterables.addAll(compromisedThemes, themes);
				}
			}

			for (String theme : compromisedThemes) {
				Collection<HSSFCell> cells = themes.get(theme);
				for (HSSFCell cell : cells) {
					HSSFCellStyle cellStyle = cell.getCellStyle();
					HSSFSheet sheet = cell.getSheet();
					HSSFWorkbook workbook = sheet.getWorkbook();

					HSSFFont originalFont = cellStyle.getFont(workbook);

					HSSFCellStyle clone = workbook.createCellStyle();
					clone.cloneStyleFrom(cellStyle);

					HSSFFont font = workbook.findFont(
						true,
						IndexedColors.DARK_RED.getIndex(),
						originalFont.getFontHeight(),
						originalFont.getFontName(),
						originalFont.getItalic(),
						originalFont.getStrikeout(),
						originalFont.getTypeOffset(),
						originalFont.getUnderline());

					if (null == font) {
						font =  workbook.createFont();
						font.setBold(true);
						font.setColor(IndexedColors.DARK_RED.getIndex());
						font.setFontHeight(originalFont.getFontHeight());
						font.setFontName(originalFont.getFontName());
						font.setItalic(originalFont.getItalic());
						font.setStrikeout(originalFont.getStrikeout());
						font.setTypeOffset(originalFont.getTypeOffset());
						font.setUnderline(originalFont.getUnderline());
					}
					clone.setFont(font);
					cell.setCellStyle(clone);
				}
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
