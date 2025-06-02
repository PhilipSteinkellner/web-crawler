# Web-Crawler

This program analyses the contents of the input website and lists its headings and links.
It also does this for the linked websites within the specified domains to the given depth.
Broken links are marked as such.

The result is recorded in `report.md`

## Build

Run `./gradlew build`

## Execute

Run `./gradlew run --args='https://news.orf.at orf.at --depth=1'`

The first argument must be a URL that specifies the website to start crawling.
The following arguments define the domain restrictions.
The --depth option limits how deep the program follows the links (default: 0, i.e., only the given website is crawled)

## Test

Run `./gradlew test`

## Test Coverage

Run `./gradlew test jacocoTestReport`