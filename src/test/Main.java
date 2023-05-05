package test;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.client.connect.Connect;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Scanner;


public class Main extends Application {
    private static String getLink() throws IOException {
        String apiLink = "https://vi.wikipedia.org/w/api.php?action=opensearch&search=";

        String name = "ca sĩ nguyễn việt hoàng";

        String url = apiLink + name;

        Document doc = Jsoup.connect(url)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute().parse();

        JSONArray json = new JSONArray(doc.text());

        JSONArray links = (JSONArray) json.get(3);

        String link = links.get(0).toString();

        String[] subString = link.split("/");
        String singerName = subString[subString.length-1];
        String decodeSingerName = URLDecoder.decode(singerName, StandardCharsets.UTF_8);
        System.out.println(decodeSingerName);


        return null;
    }

    private static void info() throws IOException {
        String url = "https://vi.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=Phan_M%E1%BA%A1nh_Qu%E1%BB%B3nh";
        String url1 = "https://vi.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=S%C6%A1n_T%C3%B9ng_M-TP";
        Document doc = Jsoup.connect(url)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute().parse();

        JSONObject json = new JSONObject(doc.text());
        JSONObject query = json.getJSONObject("query");
        JSONObject pages = query.getJSONObject("pages");

        String pageId = pages.keySet().toString();
//        pageId = pageId.replaceAll("\\[", "");
        pageId = pageId.replaceAll("]|\\[", "");

        JSONObject page = pages.getJSONObject(pageId);
        JSONArray revisions = page.getJSONArray("revisions");
        JSONObject revision1 = (JSONObject) revisions.get(0);
        ArrayList<String> info = getInfoSinger(revision1.get("*").toString());
    }

    private static void processInput(String data) throws IOException {
        String url = "https://www.google.com/search?q=";
        String name = "manhQuynhphan";
        String fullLink = url + URLEncoder.encode(name, StandardCharsets.UTF_8);
        System.out.println(fullLink);
        Document doc = Jsoup.connect(fullLink)
                .ignoreContentType(true)
                .execute().parse();

        Element element = doc.getElementById("rcnt");
        System.out.println(element.getElementsByClass("yKMVIe").text());
    }

    private static String getLyricVNLink(String songName, String singerName) throws IOException {
        String url = "https://loibaihat.biz/timkiem/?keyword=";
//        String songName = "vợ người ta";
        String fullLink = url + URLEncoder.encode(songName, StandardCharsets.UTF_8);

        Document doc = Jsoup.connect(fullLink)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute().parse();
        Element listSong = doc.getElementById("left-content");
        Elements songs = listSong.getElementsByClass("list-lyric-song");

        String linkLyric = "";

        if (songs.size() == 2) {
            Element song = songs.get(1);
            String ten = song.getElementsByClass("ten").text();
            String nhacsy = song.getElementsByClass("nhacsy").text();

//            System.out.println(ten + " " + nhacsy);
            linkLyric = song.getElementsByTag("a").attr("href");
//            System.out.println(linkLyric);
        }
        else if (songs.size() > 2) {
            Element tmp = new Element("temp");
            for (Element song : songs) {
                String ten = song.getElementsByClass("ten").text();
                String nhacsy = song.getElementsByClass("nhacsy").text();
                if (ten.compareToIgnoreCase(songName) == 0 && nhacsy.compareToIgnoreCase(singerName) == 0) {
                    tmp = song;
//                    System.out.println(ten + " " + nhacsy);
                    break;
                } else if (ten.compareToIgnoreCase(songName) == 0) {
//                    System.out.println(ten + " " + nhacsy);
                    tmp = song;
                }
            }

            String ten = tmp.getElementsByClass("ten").text();
            String nhacsy = tmp.getElementsByClass("nhacsy").text();
            linkLyric = tmp.getElementsByTag("a").attr("href");
//            System.out.println(ten + " " + nhacsy);
//            System.out.println(linkLyric);
        }
        return linkLyric;
    }

    private static String getLyric(String linkLyric) throws IOException {
        String domain = "https://loibaihat.biz";
        String fullLink = domain + linkLyric;

        Document doc = Jsoup.connect(fullLink)
                .method(Connection.Method.GET)
                .execute().parse();

        Element playLyric = doc.getElementById("play-lyric");

        Element lyricSong = playLyric.getElementsByClass("lyric-song").get(0);

//        System.out.println(lyricSong.text());
//        String lyricText = lyricSong;

//        System.out.println(lyricSong.toString().split("<br>")[1].strip());
        String[] subString = lyricSong.toString().split("<br>");
        int lengthLyric = lyricSong.toString().split("<br>").length;
        String hehe = "";
        for (int i = 1; i < lengthLyric - 1; i++) {
            hehe += subString[i].strip() + "\n";
        }

//        System.out.println(hehe);
        return hehe;
    }

    private static String getLyricFromGGSearch(String songName, String singerName) throws Exception{
        System.out.println(songName + " " + singerName);

        String ggSearchLink = "https://www.google.com/search?q=";
        String qLBH = "Lời bài hát";

        String fullLink = ggSearchLink + qLBH + " " + songName + " của " + singerName;

        Document doc = Jsoup.connect(fullLink)
                .execute().parse();

        Element search = doc.getElementById("search");

        Element WbKHeb = search.getElementsByAttributeValue("jsname", "WbKHeb").get(0);

        String lyric = "";
        Elements ujudUb = WbKHeb.getElementsByClass("ujudUb");

        for (Element spans: ujudUb) {
            for (Element span: spans.getElementsByTag("span")) {
//                System.out.println(span.text());
                lyric += span.text() + "\n";
            }
        }

//        System.out.println(ujudUb.get(0).getElementsByTag("span").get(0).text());

//        System.out.println(c.text());
        return lyric;
    }

    public static void main(String[] args) throws Exception{
//        launch();
//        LinkedHashMap<String, String> songInfo = googleSearch();
//        for (String info: songInfo.values()) {
//            System.out.println(info);
//        }
//        String songName = songInfo.get("songName");
//        String singerName = songInfo.get("singerName");
//
//        System.out.println(getLyricFromGGSearch(songName, singerName));
//        getLyricFromGGSearch(songName, singerName);
//        System.out.println(songName + " " + singerName);

//        String link = getLyricVNLink(songName.strip(), singerName.strip());
//        System.out.println(link);

//        getLyric(link);
//        getLink();

        googleSearch();
    }

    private static ArrayList<String> getInfoSinger(String rawInfo) {

        String[] listInfo = rawInfo.split("'''");

        String[] thongTin = listInfo[0].split("Thông tin nhân vật\\n\\|");

        String[] thongTinCaNhan = thongTin[1].split("\n");

        for (String str: thongTinCaNhan) {
            if (str.replaceAll("\\s+", "").split("=").length < 2) continue;
            str = str.replaceAll("\\[|]|\\{|}", "");
            str = str.replaceFirst("\\|", " ");
            str = str.strip();
            if (str.contains("tên") || str.contains("birth_name")) {
                System.out.println(str);
            } else if (str.contains("ngày sinh") || str.contains("birth_date")) {
                System.out.println(str);
            } else if (str.contains("nguyên quán") || str.contains("birth_place")) {
                System.out.println(str);
            } else if (str.contains("nghề nghiệp")) {
                System.out.println(str);
            } else if (str.contains("dân tộc") ) {
                System.out.println(str);
            }
        }
        return null;
    }

    // bai hat
    private static LinkedHashMap<String, String> googleSearch() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Nhap thong tin: ");
        String input = sc.nextLine();

        String ggSearchLink = "https://www.google.com/search?q=";

        String fullLink = ggSearchLink + input;

        Document document = Jsoup.connect(fullLink)
                .method(Connection.Method.GET)
                .execute().parse();

        // tach html tu goole search
        Element rnct = document.getElementById("rcnt");
        Elements yKMVIe = rnct.getElementsByClass("yKMVIe");

        LinkedHashMap<String, String> songInfo = new LinkedHashMap<>();


        System.out.println(yKMVIe.get(0).text());
        String songName = yKMVIe.get(0).text();
        songInfo.putIfAbsent("songName", songName);

        Elements wx62f_pzpZlf_x7XAkb = rnct.getElementsByClass("wx62f PZPZlf x7XAkb");
        String[] splitString = wx62f_pzpZlf_x7XAkb.get(0).text().split("\\s");

        String singerName = "";
        for (int i = 3; i < splitString.length; i++) {
            singerName += splitString[i] + " ";
        }
        System.out.println(singerName);
        songInfo.putIfAbsent("singerName", singerName);

        return songInfo;
    }

    @Override
    public void start(Stage stage) throws Exception {

        TextArea textArea = new TextArea();

        LinkedHashMap<String, String> songInfo = googleSearch();
//        for (String info: songInfo.values()) {
//            System.out.println(info);
//        }
        String songName = songInfo.get("songName");
        String singerName = songInfo.get("singerName");
        String link = getLyricVNLink(songName.strip(), singerName.strip());
        String lyric = getLyric(link);

        textArea.setText(lyric);

        VBox box = new VBox();
        box.getChildren().add(textArea);

        Scene scene = new Scene(box, 500, 500);
        stage.setScene(scene);
        stage.show();

    }
}
