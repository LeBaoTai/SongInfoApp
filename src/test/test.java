package main.java.server;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;

public class test {
    private String getLinkLyricFromBHH(LinkedHashMap<String, String> songInfo) {
        try {
            String songName = songInfo.get("title").strip();
            String singerName = songInfo.get("subTitle").strip();

            String bhhUrl = "https://baihathay.net/music/tim-kiem/";
            String fullUrl = bhhUrl + songName + "/trang-1.html";

            Document doc = Jsoup.connect(fullUrl)
                    .execute().parse();

            String link = "";
            Element pureMenuList = doc.getElementsByClass("pure-menu-list").last();
            for (Element pureMenuItem: pureMenuList.getElementsByClass("pure-menu-item")) {
                String song = pureMenuItem.text();
//                int len = song.split("-").length;
                String singer  = song.split("-")[1].strip();

                if (singer.compareToIgnoreCase(singerName) == 0) {
                    link = pureMenuItem.getElementsByTag("a").first().attr("href");
                    break;
                }
            }
            return link;
        } catch (Exception e) {
            return null;
        }
    }
}
