self-assessment-biss-api
========================

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The Self Assessment BISS (Business Income Source Summary) API allows a developer to retrieve a summary of income and
expenditure for a specified self-employment, UK property business, or foreign property business for a given tax year.

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.9.x
- [Service Manager 2](https://github.com/hmrc/sm2)

## Development Setup

Run the microservice from the console using: `sbt run` (starts on port 9785 by default)

Start the service manager profile: `sm2 --start MTDFB_BISS`

## Run Tests

Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## Viewing Open API Spec (OAS) docs

To view documentation locally ensure the Self Assessment BISS API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`

Then go to http://localhost:9680/api-documentation/docs/openapi/preview and use this port and version:
`http://localhost:9785/api/conf/3.0/application.yaml`

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/self-assessment-biss-api)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
