# SarifTool

NOTE: THIS project will most likely not be continued. It was started when sonarqube didn't allow for import of the SARIF format. That has now been fixed
by SonarQuebe, and so the initial motivation is gone. And there are other SARIF-parsers out there to use.
However it was a fun practice in building a CLI and generatiing a native exe from that with graalvm, so I might continue it in the future. The SarifParser
and the cli graalvm worked quite ok when having a look at it.
So for the pipelines and practice of building a PicoCLI generated executable, i'm leaving this as is. Hmm, make I could make a DEV-article, there are some insights
that would be usable in general.

Anyway, don't expect me to fix anything regarding this project, is here for reference only.



`sariftool` is a CLI tool for converting the standard SARIF format to other formats (currently only Sonar Custom Format). Having a generalized name means it might do more in the future.;)

Contents
========

 * [Why?](#why)
 * [Installation](#installation)
 * [Usage](#usage)
 * [Git Integration](#git-integration)
 * [What can I back up?](#what-can-i-back-up)
 * [Configuration](#configuration)
 * [Output Structure](#output-structure)
 * [Reinstalling Dotfiles](#reinstalling-dotfiles)
 * [Want to contribute?](#want-to-contribute)

## Why?

I needed a tool that would import SARIF output from Mega-linter to SonarQuebe.

## Installation
---

TO-DO

## Usage
---

To start the interactive program, simply run `$ sariftool`.

```shell

Usage: sarifconvert [-hV] [-i=<inputDir>] [-o=<outputDir>] [-t=<targetFormat>]
Convert SARIF format to other formats
  -h, --help      Show this help message and exit.
  -i, --inputdir=<inputDir>
                  /path/to/dir/with/sarif/file(s)/
  -o, --outputdir=<outputDir>
                  /path/to/dir/for/output/
  -t, --targetformat=<targetFormat>
                  Only sonar atm (default)
  -V, --version   Print version information and exit.
```

## Format References

- [SARIF Specification](https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html)
- [SARIF JSON chema](https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json)
- [SonarQube Generic Issue Format](https://docs.sonarqube.org/latest/analysis/generic-issue/)
- [SonarQube Issue](https://docs.sonarqube.org/latest/user-guide/issues/)

## Want to Contribute?
---

Check out `docs/CONTRIBUTING.md`.

## License

This project is released under the

[Apache License 2.0](LICENSE)

Most of the parser implementation is lifted from:


