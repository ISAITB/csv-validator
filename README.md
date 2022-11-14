![BuildStatus](https://github.com/ISAITB/csv-validator/actions/workflows/main.yml/badge.svg)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ISAITB_csv-validator&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ISAITB_csv-validator)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ISAITB_csv-validator&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=ISAITB_csv-validator)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ISAITB_csv-validator&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=ISAITB_csv-validator)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ISAITB_csv-validator&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=ISAITB_csv-validator)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ISAITB_csv-validator&metric=bugs)](https://sonarcloud.io/summary/new_code?id=ISAITB_csv-validator)
[![licence](https://img.shields.io/github/license/ISAITB/csv-validator.svg?color=blue)](https://github.com/ISAITB/csv-validator/blob/master/LICENCE.txt)
[![docs](https://img.shields.io/static/v1?label=docs&message=Test%20Bed%20guides&color=blue)](https://www.itb.ec.europa.eu/docs/guides/latest/validatingCSV/)
[![docker](https://img.shields.io/docker/pulls/isaitb/csv-validator?color=blue&logo=docker&logoColor=white)](https://hub.docker.com/r/isaitb/csv-validator)

# CSV validator

The **CSV validator** is a web application to validate CSV data against [Table Schema](https://specs.frictionlessdata.io/table-schema/).
The application provides a fully reusable core that requires only configuration to determine the supported specifications,
configured validation types and other validator customisations. The web application allows validation via:

* A SOAP web service API for contract-based machine-machine integrations.
* A REST web service API for machine-machine integrations.
* A web form for validation via user interface.

The SOAP web service API conforms to the [GITB validation service API](https://www.itb.ec.europa.eu/docs/services/latest/validation/)
which makes it usable as a building block in [GITB Test Description Language (TDL)](https://www.itb.ec.europa.eu/docs/tdl/latest/)
conformance test cases for the verification of content (as a [verify step](https://www.itb.ec.europa.eu/docs/tdl/latest/constructs/index.html#verify)
handler). Note additionally that the validator can also be used to build a command-line tool as an executable JAR with
pre-packaged or provided configuration.

This validator is maintained by the **European Commission's DIGIT** and specifically the **Interoperability Test Bed**,
a conformance testing service for projects involved in the delivery of cross-border public services. Find out more
[here](https://joinup.ec.europa.eu/collection/interoperability-test-bed-repository/solution/interoperability-test-bed).

# Usage

Usage and configuration of this validator is documented as a step-by-step tutorial in the Test Bed's
[CSV validation guide](https://www.itb.ec.europa.eu/docs/guides/latest/validatingCSV/).

The validator's key principle is that the software is built as a generic core that can be configured to validate any
supported specifications. Configuration is organised in **domains** which represent logically separate setups supported
by the same application instance. Each such domain defines the offered **validation types** and their **options** along
with the validation artefacts needed to carry out validation (local, remote or user-provided). A domain's configuration
is grouped in a folder that contains a [configuration property file](https://www.itb.ec.europa.eu/docs/guides/latest/validatingCSV/index.html#step-3-prepare-validator-configuration)
along with any other necessary resources.

When built from source, the simplest way to get started using the validator is to use the **all-in-one executable JAR**
built from the `csvvalidator-war` module. You can use and configure this as described in the
[validator installation guide](https://www.itb.ec.europa.eu/docs/guides/latest/installingValidatorProduction/index.html#approach-1-using-jar-file).

If you do not plan on modifying the validator's source you can reuse the Test Bed's provided packages. Specifically:

* Via **Docker**, using the [isaitb/csv-validator](https://hub.docker.com/r/isaitb/csv-validator) image from the Docker Hub.
* Via **JAR file**, using the [generic web application package](https://www.itb.ec.europa.eu/csv-jar/any/validator.zip).
* Via **command line tool**, using the [generic command line tool package](https://www.itb.ec.europa.eu/csv-offline/any/validator.zip).

It is interesting to note that the second option (executable web application JAR) matches what you would build from this
repository. The command line package is produced from the `csvvalidator-jar` although this requires an additional step
of JAR post-processing to configure the validator's domain(s).

Once the validator's web application is up you can use it as follows:

* SOAP API: http://localhost:8080/csv/soap/DOMAIN/validation?wsdl
* REST API: http://localhost:8080/csv/DOMAIN/api/validate (Swagger docs at http://localhost:8080/csv/swagger-ui/index.html)
* Web form: http://localhost:8080/csv/DOMAIN/upload

Note that the `DOMAIN` placeholder in the above URLs is the name of a domain configuration folder beneath your configured `validator.resourceRoot`.
This can be adapted by providing `validator.domainName.DOMAIN` mapping(s) for your domain(s). These are [application-level configuration properties](https://www.itb.ec.europa.eu/docs/guides/latest/validatingCSV/index.html#application-level-configuration)
that can be set in the default [application.properties](csvvalidator-common/src/main/resources/application.properties)
or via environment variables and system properties.

# Building

The validator is a multi-module Maven project from which the artefact to use is the web application package, produced
from module `csvvalidator-war`. This is an all-in-one Spring Boot web application. To build issue `mvn clean install`.

## Prerequisites

To build this project's libraries you require:
* A JDK installation (11+).
* Maven (3+)
* Locally installed [itb-commons](https://github.com/ISAITB/itb-commons) dependencies (see below).

Building this validator from source depends on libraries that are available on public repositories. The exception is
currently the set of [itb-commons](https://github.com/ISAITB/itb-commons) dependencies, common libraries that are shared
by all Test Bed validators. To be able to build you need to first clone [itb-commons](https://github.com/ISAITB/itb-commons)
and install its artefacts in your local Maven repository.

## Configuration for development

When building for development you will need to provide the validator's basic configuration to allow it to bootstrap.
The simplest approach to do this is to use environment variables that set the validator's
[configuration properties](https://www.itb.ec.europa.eu/docs/guides/latest/validatingCSV/index.html#validator-configuration-properties).

The minimum properties you should define this way are:

* `validator.resourceRoot`: The root folder from which all domain configurations will be loaded.
* `logging.file.path`: The validator’s log output folder.
* `validator.tmpFolder`: The validator’s temporary work folder.

In addition, you should include within the `validator.resourceRoot` folder additional folder(s) for your configuration domains,
each with its configuration property file and any other needed resources. A simple example of such configuration that you
can also download and reuse, is provided in the CSV validation guide's [configuration step](https://www.itb.ec.europa.eu/docs/guides/latest/validatingCSV/index.html#step-3-prepare-validator-configuration).

## Using Docker

If you choose Docker to run your validator you can use the [sample Dockerfile](etc/docker/csv-validator/Dockerfile)
as a starting point. To use this:
1. Create a folder and copy within it the Dockerfile and JAR produced from the `csvvalidator-war` module.
2. Create a sub-folder (e.g. `resources`) as your resource root within which you place your domain configuration folder(s).
3. Adapt the Dockerfile to also copy the `resources` folder and set its path within the image as the `validator.resourceRoot`:

```
...
COPY resources /validator/resources/
ENV validator.resourceRoot /validator/resources/
...
```  

# Plugin development

The CSV validator supports custom plugins to extend the validation report. Plugins are implementations of the
[GITB validation service API](https://www.itb.ec.europa.eu/docs/services/latest/validation/) for which the following
applies. Note that plugin JAR files need to be built as "all-in-one" JARs.

## Input to plugins

The JSON validator calls plugins in sequence passing in the following input:

| Input name          | Type     | Description                                                                                                                             |
|---------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `contentToValidate` | `String` | The absolute and full path to the input provided to the validator.                                                                      |
| `domain`            | `String` | The validation domain relevant to the specific validation call.                                                                         |
| `validationType`    | `String` | The validation type of the domain that is selected for the specific validation call.                                                    |
| `tempFolder`        | `String` | The absolute and full path to a temporary folder for plugins. This will be automatically deleted after all plugins complete validation. |
| `hasHeaders`        | `String` | `true` or `false` depending on whether the input should be considered as having a header row.                                           |
| `delimiter`         | `String` | The character to use as the field delimiter.                                                                                            |
| `quote`             | `String` | The character to use for the quote character.                                                                                           |
| `locale`            | `String` | The locale (language code) to use for reporting of results (e.g. "fr", "fr_FR").                                                        |

## Output from plugins

The output of plugins is essentially a GITB `ValidationResponse` that wraps a `TAR` instance. The report items within this `TAR` instance are merged
with any reports produced by Table Schema validation.

## Plugin configuration

Plugin configuration for a validator instance is part of its [domain configuration](https://www.itb.ec.europa.eu/docs/guides/latest/validatingCSV/index.html#domain-level-configuration).
Once you have your plugins implemented you can configure them using the `validator.defaultPlugins`
and `validator.plugins` properties where you list each plugin by providing:

* The path to its JAR file.
* The fully qualified class name of the plugin entry point.

# Licence

This software is shared using the [European Union Public Licence (EUPL) version 1.2](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12).

# Legal notice

The authors of this library waive any and all liability linked to its usage or the interpretation of results produced
by its downstream validators.

# Contact

For feedback or questions regarding this library you are invited to post issues in the current repository. In addition,
feel free to contact the Test Bed team via email at [DIGIT-ITB@ec.europa.eu](mailto:DIGIT-ITB@ec.europa.eu).

# See also

The Test Bed provides similar validators for other content types. Check these out for more information:
* The **RDF validator**: see [overview](https://joinup.ec.europa.eu/collection/interoperability-test-bed-repository/solution/rdf-validator), [source](https://github.com/ISAITB/shacl-validator), [detailed guide](https://www.itb.ec.europa.eu/docs/guides/latest/validatingRDF/) and [Docker Hub image](https://hub.docker.com/r/isaitb/shacl-validator).
* The **XML validator**: see [overview](https://joinup.ec.europa.eu/collection/interoperability-test-bed-repository/solution/xml-validator), [source](https://github.com/ISAITB/xml-validator), [detailed guide](https://www.itb.ec.europa.eu/docs/guides/latest/validatingXML/) and [Docker Hub image](https://hub.docker.com/r/isaitb/xml-validator).
* The **JSON validator**: see [overview](https://joinup.ec.europa.eu/collection/interoperability-test-bed-repository/solution/json-validator), [source](https://github.com/ISAITB/json-validator), [detailed guide](https://www.itb.ec.europa.eu/docs/guides/latest/validatingJSON/) and [Docker Hub image](https://hub.docker.com/r/isaitb/json-validator).