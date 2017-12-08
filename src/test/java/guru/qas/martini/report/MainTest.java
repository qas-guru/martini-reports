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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.apache.commons.io.output.TeeOutputStream;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.io.ByteStreams;

import static com.google.common.base.Preconditions.checkState;

public class MainTest {

	private File inputFile1;
	private File inputFile2;
	private File outputFile;

	@BeforeClass
	private void setUpInputFile() throws IOException {
		inputFile1 = File.createTempFile("sample1.", ".json");
		inputFile2 = File.createTempFile("sample2.", ".json");
		try (InputStream inputStream = MainTest.class.getClassLoader().getResourceAsStream("sample.json");
			 OutputStream stream1 = new FileOutputStream(inputFile1);
			 OutputStream stream2 = new FileOutputStream(inputFile2);
			 OutputStream teeOutputStream = new TeeOutputStream(stream1, stream2)
		) {
			ByteStreams.copy(inputStream, teeOutputStream);
		}
		inputFile1.deleteOnExit();
		inputFile2.deleteOnExit();
	}

	@BeforeClass
	private void setUpOutputFile() throws IOException {
		//outputFile = File.createTempFile("sample", ".xls");
		outputFile = new File("/Users/pennycurich/tmp/sample.xls");
		System.out.println("output file is \n" + outputFile);
		//outputFile.deleteOnExit();
	}

	@DataProvider(name = "resourceProvider")
	public Object[][] getResources() throws MalformedURLException {

		String parent = inputFile2.getParentFile().toURI().toURL().toExternalForm();
		String outputFileExternalForm = outputFile.toURI().toURL().toExternalForm();

		return new Object[][]{
			{inputFile1.toURI().toURL().toExternalForm(), outputFileExternalForm},
			{parent + "sample2.*.json", outputFileExternalForm}
		};
	}

	@Test(dataProvider = "resourceProvider")
	public void testInput(String inputResource, String outputResource) throws Exception {
		String[] args = new String[]{"-i", inputResource, "-o", outputResource};
		Main.main(args);
		checkState(outputFile.exists(), "output file does not exist");
		// TODO: open w/poi and examine
	}

	@AfterClass
	public void tearDown() {
	}
}
