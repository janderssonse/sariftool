# SarifComvert

`sarifconvert` is a CLI tool for converting the standard SARIF format to other formats (currently only Sonar Custom Format). 


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

To start the interactive program, simply run `$ sarifconvert`.

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

## Want to Contribute?
---

Check out `docs/CONTRIBUTING.md`.

## License

This project is released under the

[Apache License 2.0](LICENSE)

Most of the parser implementation is lifted from:


