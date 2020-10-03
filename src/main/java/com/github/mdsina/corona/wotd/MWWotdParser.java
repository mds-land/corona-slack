package com.github.mdsina.corona.wotd;

import javax.inject.Singleton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Singleton
public class MWWotdParser implements WotdParser {

    private static final String BASE_URL = "https://www.merriam-webster.com/";
    private static final String URL = BASE_URL + "word-of-the-day";

    @Override
    public Wotd getAndParse() {
        try {
            Document document = Jsoup.connect(URL)
                .timeout(10000)
                .followRedirects(true)
                .get();

            Element element = document.select("div.word-and-pronunciation > h1").first();
            String wotd = element.text();
            String wotdLink = BASE_URL + "dictionary/" + wotd.trim();
            String meaning = document.select("div.wod-definition-container > p").first().text().trim();
            String example = document.select("div.wotd-examples > p").first().text().trim();

            return Wotd.builder()
                .dictionary("Merriam-Webster")
                .wotd(wotd)
                .wotdLink(wotdLink)
                .meaning(meaning)
                .example(example)
                .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
