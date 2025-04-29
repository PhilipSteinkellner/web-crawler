# Web-Crawler

This program analyses the contents of the input website and lists its headings and links.
It also does this for the linked websites within the specified domains to the given depth.

The result is recorded in `report.md`

## Build

Run `./gradlew build`

## Run

Run `./gradlew run --args='https://news.orf.at orf.at --depth=1`

## Test

Run `./gradlew test`

## Test Coverge

Run `./gradlew test jacocoTestReport`