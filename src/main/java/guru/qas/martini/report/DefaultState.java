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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class DefaultState implements State {
	protected final static String KEY_FEATURE = "feature";

	private final Multimap<String, Cell> statii;
	private final Multimap<String, Cell> themes;
	private final Map<String, JsonObject> suites;
	private final Map<String, JsonObject> features;

	private List<Cell> longestExecutionCells;
	private long longestExecution;

	protected DefaultState() {
		statii = ArrayListMultimap.create();
		themes = ArrayListMultimap.create();
		suites = new LinkedHashMap<>();
		features = new LinkedHashMap<>();
		longestExecutionCells = new ArrayList<>();
		longestExecution = 0;
	}

	@Override
	public void setStatus(Cell cell, String status) {
		statii.put(status, cell);
	}

	@Override
	public void setThemes(Cell cell, Iterable<String> i) {
		for (String theme : i) {
			themes.put(theme, cell);
		}
	}

	@Override
	public void setExecutionTime(Cell cell, long executionTime) {
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
			for (Cell cell : longestExecutionCells) {
				CellStyle original = cell.getCellStyle();
				Sheet sheet = cell.getSheet();
				Workbook workbook = sheet.getWorkbook();
				CellStyle newStyle = workbook.createCellStyle();
				newStyle.cloneStyleFrom(original);
				int originalFontIndex = original.getFontIndexAsInt();
				Font originalFont = workbook.getFontAt(originalFontIndex);


				Font font = workbook.createFont();
				font.setBold(true);
				font.setColor(IndexedColors.DARK_RED.getIndex());
				font.setFontHeight((short) Math.round(originalFont.getFontHeight() * 1.5));
				newStyle.setFont(font);
				cell.setCellStyle(newStyle);

				Row row = cell.getRow();
				short firstCellNum = row.getFirstCellNum();
				short lastCellNum = row.getLastCellNum();

				for (int i = firstCellNum; i < lastCellNum; i++) {
					Cell rowCell = row.getCell(i);
					original = rowCell.getCellStyle();
					CellStyle borderStyle = workbook.createCellStyle();
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
		Map<String, Collection<Cell>> statusMap = statii.asMap();
		for (Map.Entry<String, Collection<Cell>> mapEntry : statusMap.entrySet()) {
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
				Collection<Cell> statusCells = mapEntry.getValue();
				colorRows(color, statusCells);
			}
		}
	}

	protected void colorRows(short color, Iterable<Cell> cells) {
		for (Cell cell : cells) {
			Row row = cell.getRow();
			colorRow(color, row);

		}
	}

	protected void colorRow(short color, Row row) {
		short firstCellNum = row.getFirstCellNum();
		short lastCellNum = row.getLastCellNum();
		for (int i = firstCellNum; i <= lastCellNum; i++) {
			Cell cell = row.getCell(i);
			if (null != cell) {
				CellStyle cellStyle = cell.getCellStyle();
				Workbook workbook = cell.getSheet().getWorkbook();
				CellStyle clone = workbook.createCellStyle();

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
		Collection<Cell> failed = statii.get("FAILED");

		if (!failed.isEmpty()) {
			List<Row> rows = Lists.newArrayListWithExpectedSize(failed.size());
			for (Cell cell : failed) {
				Row row = cell.getRow();
				rows.add(row);
			}

			Set<Cell> compromisedThemeCells = Sets.newHashSet();

			Map<String, Collection<Cell>> themeMap = themes.asMap();
			for (Map.Entry<String, Collection<Cell>> mapEntry : themeMap.entrySet()) {
				Collection<Cell> themeCells = mapEntry.getValue();

				boolean compromised = false;
				for (Iterator<Cell> iterator = themeCells.iterator(); !compromised && iterator.hasNext(); ) {
					Cell themeCell = iterator.next();
					Row row = themeCell.getRow();
					compromised = rows.contains(row);
				}

				if (compromised) {
					compromisedThemeCells.addAll(themeCells);
				}
			}

			Set<String> compromisedThemes = Sets.newHashSet();
			for (Cell themeCell : compromisedThemeCells) {
				String contents = themeCell.getStringCellValue();
				if (null != contents) {
					Iterable<String> themes = Splitter.onPattern("\\s+").omitEmptyStrings().split(contents);
					Iterables.addAll(compromisedThemes, themes);
				}
			}

			for (String theme : compromisedThemes) {
				Collection<Cell> cells = themes.get(theme);
				for (Cell cell : cells) {
					CellStyle cellStyle = cell.getCellStyle();
					Sheet sheet = cell.getSheet();
					Workbook workbook = sheet.getWorkbook();

					int originalFontIndex = cellStyle.getFontIndexAsInt();
					Font originalFont = workbook.getFontAt(originalFontIndex);

					CellStyle clone = workbook.createCellStyle();
					clone.cloneStyleFrom(cellStyle);

					Font font = workbook.findFont(
						true,
						IndexedColors.DARK_RED.getIndex(),
						originalFont.getFontHeight(),
						originalFont.getFontName(),
						originalFont.getItalic(),
						originalFont.getStrikeout(),
						originalFont.getTypeOffset(),
						originalFont.getUnderline());

					if (null == font) {
						font = workbook.createFont();
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
		checkNotNull(suite, "null JsonObject");
		String id = getId(suite);
		suites.put(id, suite);
	}

	protected String getId(JsonObject object) {
		JsonPrimitive primitive = object.getAsJsonPrimitive("id");
		return primitive.getAsString();
	}

	@Override
	public void addFeature(JsonObject feature) {
		checkNotNull(feature, "null JsonObject");
		String id = getId(feature);
		features.put(id, feature);
	}

	@Override
	public JsonObject getFeature(JsonObject result) {
		checkNotNull(result, "null JsonObject");
		JsonElement element = result.get(KEY_FEATURE);
		String featureId = element.getAsString();
		return features.get(featureId);
	}


	@Override
	public void updateSuites(Sheet sheet) {
		int lastRowNum = sheet.getLastRowNum();
		Row row = sheet.createRow(0 == lastRowNum ? 0 : lastRowNum + 1);
		row.createCell(0, CellType.STRING).setCellValue("ID");
		row.createCell(1, CellType.STRING).setCellValue("Date");
		row.createCell(2, CellType.STRING).setCellValue("Name");
		row.createCell(3, CellType.STRING).setCellValue("Hostname");
		row.createCell(4, CellType.STRING).setCellValue("IP");
		row.createCell(5, CellType.STRING).setCellValue("Username");
		row.createCell(6, CellType.STRING).setCellValue("Profiles");
		row.createCell(7, CellType.STRING).setCellValue("Environment Variables");

		for (Map.Entry<String, JsonObject> mapEntry : suites.entrySet()) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);

			String id = mapEntry.getKey();
			row.createCell(0, CellType.STRING).setCellValue(id);

			JsonObject suite = mapEntry.getValue();
			JsonPrimitive primitive = suite.getAsJsonPrimitive("startTimestamp");
			Long timestamp = null == primitive ? null : primitive.getAsLong();

			Cell cell = row.createCell(1);
			if (null != timestamp) {
				Workbook workbook = sheet.getWorkbook();
				CellStyle cellStyle = workbook.createCellStyle();
				CreationHelper creationHelper = workbook.getCreationHelper();
				cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("m/d/yy h:mm"));
				cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
				cell.setCellValue(new Date(timestamp));
				cell.setCellStyle(cellStyle);
			}

			cell = row.createCell(2);
			primitive = suite.getAsJsonPrimitive("name");
			cell.setCellValue(null == primitive ? "" : primitive.getAsString());

			cell = row.createCell(3);
			JsonObject host = suite.getAsJsonObject("host");
			primitive = null == host ? null : host.getAsJsonPrimitive("name");
			cell.setCellValue(null == primitive ? "" : primitive.getAsString());

			cell = row.createCell(4);
			primitive = null == host ? null : host.getAsJsonPrimitive("ip");
			cell.setCellValue(null == primitive ? "" : primitive.getAsString());

			cell = row.createCell(5);
			primitive = null == host ? null : host.getAsJsonPrimitive("username");
			cell.setCellValue(null == primitive ? "" : primitive.getAsString());

			cell = row.createCell(6);
			JsonArray array = suite.getAsJsonArray("profiles");
			List<String> profiles = Lists.newArrayList();
			if (null != array) {
				int size = array.size();
				for (int i = 0; i < size; i++) {
					JsonElement element = array.get(i);
					String profile = null == element ? null : element.getAsString();
					profiles.add(profile);
				}
				String profilesValue = Joiner.on('\n').skipNulls().join(profiles);
				cell.setCellValue(profilesValue);
			}

			cell = row.createCell(7);
			JsonObject environmentVariables = suite.getAsJsonObject("environment");
			Map<String, String> index = new TreeMap<>();
			if (null != environmentVariables) {
				Set<Map.Entry<String, JsonElement>> entries = environmentVariables.entrySet();
				for (Map.Entry<String, JsonElement> environmentEntry : entries) {
					String key = environmentEntry.getKey();
					JsonElement element = environmentEntry.getValue();
					String value = null == element ? "" : element.getAsString();
					index.put(key, value);
				}

				String variablesValue = Joiner.on('\n').withKeyValueSeparator('=').useForNull("").join(index);
				cell.setCellValue(variablesValue);
			}
		}

		for (int i = 0; i < 8; i++) {
			sheet.autoSizeColumn(i, false);
		}
	}
}
