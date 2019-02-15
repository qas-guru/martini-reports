# Martini Reports

## Table of Contents
1. [What is Martini Reports?](#what)
1. [How does Martini Reports work?](#how)
	1. [How do I generate a traceability matrix from run my Martini suite output?](#how-execute)
1. [Where can I find more information?](#info)

### What is Martini Reports? <a name="what"></a>

Martini Reporting is a Java command-line tool used to generate a traceability matrix from the JSON
output of a [Martini](https://github.com/qas-guru/martini-core) suite execution.


### How does Martini Reports work? <a name="how"></a>

The library parses Martini JSON output to generate a report in Excel format.

#### How do I generate a traceability matrix from my Martini suite output? <a name="how-execute"></a>

1. Execute a Martini test suite using the standalone harness with runtime argument -jsonOutput to produce .json files

	example: `-jsonOutput file:///path/to/martini.json`

1. Optionally run additional suites, collecting output files in a single directory.

1. Execute class guru.qas.martini.report.Main with file or directory input argument and output argument.

	example 1: `java -cp ... guru.qas.martini.report.Main -f file:///path/to/martini.json -o /path/to/martini.xlsx`

	example 2: `java -cp ... guru.qas.martini.report.Main -d file://path/to -o /path/to/martini.xlsx`

	example 3: `mvn exec:java -Dexec.mainClass="guru.qas.martini.report.Main" -Dexec.args="-i file:///path/to/martini.json -o file:///path/to/martini.xlsx"`


### Where can I find more information? <a name="info"></a>

#### In Progress: [Martini Standalone Wiki](https://github.com/qas-guru/martini-standalone/wiki) 
#### In Progress: [__Martini - swank software testing in Java__](https://leanpub.com/martini) 
