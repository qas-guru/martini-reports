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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

import guru.qas.martini.report.column.TraceabilityColumn;

import static org.apache.poi.hssf.usermodel.HSSFPicture.PICTURE_TYPE_PNG;

@SuppressWarnings("WeakerAccess")
@Configurable
public class DefaultTraceabilityMatrix implements TraceabilityMatrix {

	protected static final String IMAGE_RESOURCE = "/images/martini-80.png";
	protected final ImmutableList<TraceabilityColumn> columns;

	@Autowired
	protected DefaultTraceabilityMatrix(Iterable<TraceabilityColumn> columns) {
		this.columns = ImmutableList.copyOf(columns);
	}

	@Override
	public void createReport(Reader reader, OutputStream outputStream) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Results");
		addBanner(sheet);

		workbook.write(outputStream);
		outputStream.flush();
	}

	protected void addBanner(HSSFSheet sheet) throws IOException {

		HSSFWorkbook workbook = sheet.getWorkbook();
		byte[] imageBytes = getImageBytes();
		int imageIndex = workbook.addPicture(imageBytes, PICTURE_TYPE_PNG);

		ClientAnchor clientAnchor = getClientAnchor(workbook);
		addBanner(sheet, imageIndex, clientAnchor);
	}

	private byte[] getImageBytes() throws IOException {
		try (InputStream resourceAsStream = getClass().getResourceAsStream(IMAGE_RESOURCE)) {
			return ByteStreams.toByteArray(resourceAsStream);
		}
	}

	protected ClientAnchor getClientAnchor(HSSFWorkbook workbook) throws IOException {
		CreationHelper creationHelper = workbook.getCreationHelper();
		ClientAnchor clientAnchor = creationHelper.createClientAnchor();
		clientAnchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
		clientAnchor.setCol1(0);
		clientAnchor.setRow1(0);
		clientAnchor.setRow2(0);
		clientAnchor.setCol2(1);
		return clientAnchor;
	}

	protected void addBanner(HSSFSheet sheet, int imageIndex, ClientAnchor clientAnchor) {
		Drawing drawingPatriarch = sheet.createDrawingPatriarch();
		Picture picture = drawingPatriarch.createPicture(clientAnchor, imageIndex);
		picture.resize();
	}
}
