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
import java.net.URI;
import java.net.URL;
import java.util.UUID;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import static com.google.common.base.Preconditions.checkState;

public class MainTest {

	private Logger logger;
	private URL jsonResource;
	private File tmpDir;

	@BeforeClass
	public void setUpClass() {
		logger = LoggerFactory.getLogger(this.getClass());
		jsonResource = MainTest.class.getClassLoader().getResource("sample.json");
	}

	@BeforeTest
	public void setUp() throws IOException {
		tmpDir = java.nio.file.Files.createTempDirectory(null).toFile();
	}

	@DataProvider(name = "singleFileProvider")
	public Object[][] getSingleFileArguments() throws IOException {

		return new Object[][]{
			{
				getInputFile().toURI().toURL().toExternalForm(),
				getOutputFile().toURI().toURL().toExternalForm()}
		};
	}

	@Test(dataProvider = "singleFileProvider")
	public void testSingleFileInput(String inputResource, String outputResource) throws Exception {
		logger.info(inputResource);
		logger.info(outputResource);
		String[] args = new String[]{"-i", inputResource, "-o", outputResource};
		Main.main(args);
		File file = new File(new URI(outputResource).toURL().getFile());
		checkState(file.exists(), "output file does not exist");
		assertReportContents(file);
	}

	private File getInputFile() throws IOException {
		UUID uuid = UUID.randomUUID();
		File input = new File(tmpDir, uuid + ".json");
		try (InputStream inputStream = jsonResource.openStream()) {
			Files.copyFile(inputStream, input);
		}
		return input;
	}

	private File getOutputFile() throws IOException {
		UUID uuid = UUID.randomUUID();
		return  new File(tmpDir, uuid + ".xls");
	}

	private void assertReportContents(File output) throws IOException {
		Workbook workbook;
		try (FileInputStream inputStream = new FileInputStream(output)) {
			workbook = new HSSFWorkbook(inputStream);
		}

		Sheet sheet = workbook.getSheetAt(0);
		Row row = sheet.getRow(3);
		Cell cell = row.getCell(5);
		String value = cell.getStringCellValue();
		String expected = "1512752459298\n(Fri Dec 08 11:00:59 CST 2017)";
		Assert.assertEquals(value, expected, "report contains incorrect rendering");
	}

//	@Test
//	public void testSingleFileWildcard() throws Exception {
//
//	}

	@AfterTest
	public void tearDown() {
		try {
			FileSystemUtils.deleteRecursively(tmpDir);
		}
		catch (Exception e) {
			logger.warn("unable to delete tmp dir {}", tmpDir, e);
		}
	}

	@AfterClass
	public void tearDownClass() {
		logger = null;
		jsonResource = null;
	}
}
