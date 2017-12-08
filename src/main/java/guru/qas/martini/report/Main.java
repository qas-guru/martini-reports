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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.nio.file.OpenOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import static com.google.common.base.Preconditions.*;
import static java.nio.file.StandardOpenOption.*;

@SuppressWarnings("WeakerAccess")
public class Main {

	private final Args args;
	private final JCommander jCommander;

	protected Main(Args args, JCommander jCommander) {
		this.args = args;
		this.jCommander = jCommander;
	}

	protected void execute() throws Exception {
		try {
			String configuration = args.springConfiguration.trim();
			checkArgument(!configuration.isEmpty(), "no Spring configuration specified");
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configuration);
			createReport(context);
		}
		catch (IllegalArgumentException e) {
			printUsageSynopsis(e);
		}
	}

	protected void printUsageSynopsis(Exception e) {
		e.printStackTrace();
		jCommander.usage();
	}

	protected void createReport(ApplicationContext context) throws Exception {
		Gson gson = context.getBean(Gson.class);
		TraceabilityMatrix matrix = context.getBean(TraceabilityMatrix.class);
		createReport(context, gson, matrix);
	}

	protected void createReport(ApplicationContext context, Gson gson, TraceabilityMatrix matrix) throws Exception {
		try (Reader reader = getReader(context);
			 JsonReader jsonReader = gson.newJsonReader(reader);
			 OutputStream outputStream = getOutputStream(context)) {
			jsonReader.setLenient(true);
			matrix.createReport(jsonReader, outputStream);
		}
	}

	protected Reader getReader(ApplicationContext context) throws IOException {
		String trimmed = null == args.jsonResources ? "" : args.jsonResources.trim();
		checkArgument(!trimmed.isEmpty(), "no JSON resources specified");
		Resource[] resources = context.getResources(trimmed);
		return getReader(resources);
	}

	protected static Reader getReader(Resource[] resources) throws IOException {
		ThrowingFunction<Resource, InputStream> inputStreamFunction = Resource::getInputStream;
		Iterator<InputStream> iterator = Arrays.stream(resources).map(inputStreamFunction).iterator();
		Enumeration<InputStream> enumeration = Iterators.asEnumeration(iterator);
		SequenceInputStream sequence = new SequenceInputStream(enumeration);
		return new InputStreamReader(sequence);
	}

	protected OutputStream getOutputStream(ApplicationContext context) throws IOException {
		Resource resource = context.getResource(args.outputFileResource);
		File file = resource.getFile();
		OpenOption[] options = new OpenOption[]{args.clobber ? CREATE : CREATE_NEW, TRUNCATE_EXISTING};
		return new OptionedFileSystemResource(file, options).getOutputStream();
	}

	public static void main(String[] argv) throws Exception {
		Args args = new Args();
		JCommander jCommander = JCommander.newBuilder().addObject(args).acceptUnknownOptions(true).build();

		try {
			jCommander.parse(argv);
			List<String> unknownOptions = jCommander.getUnknownOptions();
			checkArgument(unknownOptions.isEmpty(), "unrecognized options: %s", Joiner.on(", ").join(unknownOptions));
			main(args, jCommander);
		}
		catch (IllegalArgumentException | ParameterException e) {
			e.printStackTrace();
			jCommander.usage();
		}
	}

	private static void main(Args args, JCommander jCommander) throws Exception {
		if (args.help) {
			jCommander.usage();
		}
		else {
			new Main(args, jCommander).execute();
		}
	}
}