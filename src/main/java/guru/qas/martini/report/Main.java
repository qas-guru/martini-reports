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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("WeakerAccess")
public class Main {

	protected Main() {
	}

	protected void doSomething(CommandLine commandLine) throws IOException, URISyntaxException {
		try (Reader reader = getReader(commandLine)) {
			String s = commandLine.getOptionValue("s", "applicationContext.xml");
			System.out.println("s is " + s);

			System.out.println("done");
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

	protected Reader getDirectoryReader(String location, Collection<String> filenames) throws IOException, URISyntaxException {

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

	public static void main(String[] args) throws IOException, ParseException, URISyntaxException {

		CommandLineParser parser = new DefaultParser();
		Options options = getOptions();
		CommandLine commandLine = parser.parse(options, args);

		Main application = new Main();
		application.doSomething(commandLine);
	}

	protected static Options getOptions() {
		Options options = new Options();

		OptionGroup urlGroup = getURLOptions();
		options.addOptionGroup(urlGroup);

		Option option = getOutputFileOption();
		options.addOption(option);

		option = getOverwriteOption();
		options.addOption(option);

		option = getApplicationContextOption();
		options.addOption(option);

		return options;
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
}