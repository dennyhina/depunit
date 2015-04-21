# Using the command line #

DepUnit has the following command line usage
```
Usage: java -jar depunit.jar [-e][-v] [-r <report file>] [-s <stylesheet>]
      ([-x <xml file> [-x ...] -t <tag> [-t ...]]|([-c <test class> [-c ...]]
      [<target method> ...]))
  -e: Runs DepUnit in regression mode.
  -r: Name of the xml report file to generate.
  -s: Stylesheet to use to style the report.
  -x: XML input file that defines a suite of test runs.
  -c: Test class to include in the run.
  -t: Only test runs marked with this tag will run.
  target methods: Specific test methods to run.

```

The -e option runs only the tests that failed the last time around.

The -r option is the name of the report file.  The default format of the report file is XML, this can be changed by specifying a XSL style sheet using the -s option.

The -s option is for specifying an XSL style sheet that will be applied to the report XML that is generated at the end of the test.  This lets you change the XML into HTML or some other suitable format for publishing.

The -x option lets you specify an XML test file that can contain one or more test runs.  Multiple -x options can be specified.

The -c option lets you specify a class to include in the test run.  Multiple -c options can be specified on the command line.  This option has no effect if the -x is used.

The -t is used to specify which tagged test runs are to be ran.  Multiple -t flags can be used to specify multiple tags.

The "target methods" is used if you wish to only run a specific test out of the classes specified from the -c options.  This option has no effect if the -x is used.