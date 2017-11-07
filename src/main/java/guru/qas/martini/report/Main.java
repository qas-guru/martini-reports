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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("WeakerAccess")
public class Main {

	protected Main() {
	}

	protected void createReport(CommandLine commandLine) throws IOException, URISyntaxException {
		try (Reader reader = getReader(commandLine);
			 OutputStream out = getOutputStream(commandLine)) {

			ApplicationContext context = getApplicationContext(commandLine);
			TraceabilityMatrix bean = context.getBean(TraceabilityMatrix.class);
			bean.createReport(reader, out);
		}
	}

	protected Reader getReader(CommandLine commandLine) throws IOException, URISyntaxException {
		String fileOption = commandLine.getOptionValue('f');
		String dirOption = null == fileOption ? commandLine.getOptionValue('d') : null;
		return null == fileOption ? getDirectoryReader(dirOption) : getFileReader(fileOption);
	}

	protected Reader getDirectoryReader(String location) throws IOException, URISyntaxException {
		List<String> filenames = Lists.newArrayList();
		try (InputStream inputStream = new URL(location).openStream();
			 InputStreamReader isr = new InputStreamReader(inputStream);
			 BufferedReader reader = new BufferedReader(isr)) {
			for (String line = reader.readLine(); null != line; line = reader.readLine()) {
				if (line.endsWith(".json")) {
					filenames.add(line);
				}
			}
		}
		checkState(!filenames.isEmpty(), "URL %s contains no .json files", location);
		return getDirectoryReader(location, filenames);
	}

	protected Reader getDirectoryReader(
		String location,
		Collection<String> filenames
	) throws IOException, URISyntaxException {

		List<InputStream> streams = Lists.newArrayListWithExpectedSize(filenames.size());
		for (String filename : filenames) {
			String fileLocation = String.format("%s/%s", location, filename);
			URI uri = new URI(fileLocation);
			URI normalized = uri.normalize();
			URL url = normalized.toURL();
			InputStream inputStream = url.openStream();
			streams.add(inputStream);
		}

		Enumeration<InputStream> elements = new Vector<>(streams).elements();
		SequenceInputStream inputStream = new SequenceInputStream(elements);
		return new InputStreamReader(inputStream);
	}

	protected Reader getFileReader(String location) throws IOException {
		InputStream inputStream = new URL(location).openStream();
		return new InputStreamReader(inputStream);
	}

	protected OutputStream getOutputStream(CommandLine commandLine) throws IOException {
		String location = commandLine.getOptionValue('o');
		File file = new File(location);
		checkState(file.createNewFile() || file.canWrite(), "unable to write to file %s", location);
		return new FileOutputStream(file, false);
	}

	protected ApplicationContext getApplicationContext(CommandLine commandLine) {
		String configuration = commandLine.getOptionValue("s", "martiniContext.xml");
		return new ClassPathXmlApplicationContext(configuration);
	}

	public static void main(String[] args) throws IOException, ParseException, URISyntaxException {
		Options options = getOptions();
		CommandLine commandLine = getCommandLine(args, options);

		if (null == commandLine || commandLine.hasOption('h')) {
			printUsageSynopsis(options);
		}
		else {
			new Main().createReport(commandLine);
		}
	}

	protected static Options getOptions() {
		Options options = new Options();

		Option option = getHelpOption();
		options.addOption(option);

		OptionGroup urlGroup = getURLOptions();
		options.addOptionGroup(urlGroup);

		option = getOutputFileOption();
		options.addOption(option);

		option = getOverwriteOption();
		options.addOption(option);

		option = getApplicationContextOption();
		options.addOption(option);

		return options;
	}

	protected static Option getHelpOption() {
		return Option.builder("h")
			.required(false).hasArg(false).longOpt("help").numberOfArgs(0)
			.desc("print this message")
			.build();
	}

	protected static OptionGroup getURLOptions() {
		OptionGroup group = new OptionGroup();
		group.setRequired(true);

		Option option = Option.builder("f")
			.hasArg(true).longOpt("fileURL").numberOfArgs(1)
			.desc("URL to Martini .json file").build();
		group.addOption(option);

		option = Option.builder("d").hasArg(true).longOpt("directoryURL").numberOfArgs(1)
			.desc("URL to resource containing multiple .json files").build();
		group.addOption(option);

		return group;
	}

	protected static Option getOutputFileOption() {
		return Option.builder("o")
			.required(true).hasArg(true).longOpt("outputFile").numberOfArgs(1)
			.desc("output file")
			.build();
	}

	protected static Option getOverwriteOption() {
		return Option.builder("n")
			.required(false).hasArg(false).longOpt("noOverwrite").
				desc("supress overwrite of output file").build();
	}

	protected static Option getApplicationContextOption() {
		return Option.builder("s")
			.required(false).hasArg(true).longOpt("springConfiguration")
			.desc("Spring application file").build();
	}

	protected static CommandLine getCommandLine(String[] args, Options options) {
		CommandLineParser parser = new DefaultParser();

		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args, false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return commandLine;
	}

	protected static void printUsageSynopsis(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(Main.class.getName(), options);
	}
}