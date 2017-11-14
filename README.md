# Martini Reporting 

1. Execute a Martini test suite using the standalone harness with runtime argument -jsonOutput to produce .json files

	example: `-jsonOutput file:///path/to/martini.json`

2. Optionally run additional suites, collecting output files in a single directory.

3. Execute class guru.qas.martini.report.Main wiht file or directory input argument and output argument.

	example 1: `java -cp ... guru.qas.martini.report.Main -f file:///path/to/martini.json -o /path/to/martini.xls`

	example 2: `java -cp ... guru.qas.martini.report.Main -d file://path/to -o /path/to/martini.xls`

