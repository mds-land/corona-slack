package com.github.mdsina.corona.wotd;

import javax.inject.Singleton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

//@Singleton
public class UDWotdParser implements WotdParser {

    private static final String URL = "https://www.urbandictionary.com";

    @Override
    public Wotd getAndParse() {
        try {
            Document document = Jsoup.connect(URL)
                .timeout(10000)
                .followRedirects(true)
                .get();

            Element element = document.select("a.word").first();
            String wotd = element.text();
            String wotdLink = URL + element.attr("href").trim();
            String meaning = document.select("div.meaning").first().text().trim();
            String example = document.select("div.example").first().text().trim();

            return Wotd.builder()
                .dictionary("Urban Dictionary")
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
