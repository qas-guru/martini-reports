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

import com.beust.jcommander.Parameter;

class Args {

	@Parameter(names = {"-h", "--h", "-help", "--help"}, help = true,
		description = "print this usage synopsis"
	)
	boolean help;

	@Parameter(
		names = {"-s", "--s", "-springConfiguration", "--springConfiguration"},
		description = "Spring application file"
	)
	String springConfiguration = "classpath*:/martiniContext.xml";

	@Parameter(
		names = {"-i", "--i", "-jsonResources", "--jsonResources"},
		description = "JSON resources, e.g. file:///path/to/*.json"
	)
	String jsonResources;

	@Parameter(
		names = {"-c", "--c", "-clobber", "--clobber"},
		description = "boolean, true to clobber output 6r false to prevent clobber"
	)
	boolean clobber = true;

	@SuppressWarnings("unused")
	@Parameter(
		names = {"-o", "--o", "-output", "--output"},
		description = "output resource, e.g. file:///path/to/report.xls"
	)
	String outputFileResource;
}