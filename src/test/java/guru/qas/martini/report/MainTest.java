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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import static com.google.common.base.Preconditions.checkState;

public class MainTest {

	private AtomicInteger counter;
	private URL jsonResource;

	@BeforeClass
	public void setUpClass() {
		counter = new AtomicInteger(0);
		jsonResource = MainTest.class.getClassLoader().getResource("sample.json");
	}

	@Test
	public void testSingleFileInput() throws Exception {
		int fingerprint = counter.incrementAndGet();
		String prefix = String.format("sample%s.", fingerprint);

		File input = getInputFile(prefix);
		File output = getOutputFile(prefix);

		String inputResource = input.toURI().toURL().toExternalForm();
		String outputResource = output.toURI().toURL().toExternalForm();
		String[] args = new String[]{"-i", inputResource, "-o", outputResource};
		Main.main(args);
		checkState(output.exists(), "output file does not exist");
		assertReportContents(output, 3, 5);
	}

	private void assertReportContents(File output, int rowIndex, int cellIndex) throws IOException {
		Workbook workbook;
		try (FileInputStream inputStream = new FileInputStream(output)) {
			workbook = new HSSFWorkbook(inputStream);
		}

		Sheet sheet = workbook.getSheetAt(0);
		Row row = sheet.getRow(rowIndex);
		Cell cell = row.getCell(cellIndex);
		String value = cell.getStringCellValue();
		String expected = "1512752459298\n(Fri Dec 08 11:00:59 CST 2017)";
		Assert.assertEquals(value, expected, "report contains incorrect rendering");
	}

	private File getInputFile(String prefix) throws IOException {
		File input = File.createTempFile(prefix, ".json");
		input.deleteOnExit();
		try (InputStream inputStream = jsonResource.openStream()) {
			Files.copyFile(inputStream, input);
		}
		return input;
	}

	private File getOutputFile(String prefix) throws IOException {
		File output = File.createTempFile(prefix, ".xls", new File("/Users/pennycurich/tmp/wut"));
		//output.deleteOnExit();
		return output;
	}

//	@Test
//	public void testSingleFileWildcard() throws Exception {
//
//	}

	@AfterClass
	public void tearDownClass() {
		counter = null;
		jsonResource = null;
	}
}
