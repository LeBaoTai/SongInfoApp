package main.java.client.connect;

import org.json.JSONArray;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Connect extends Thread {
    private int port;
    private Socket socket;
    private BufferedWriter ouput;
    private BufferedReader input;
    private ObjectOutputStream outputOb;

    public Connect(Socket socket) throws Exception{
        this.socket = socket;
        System.out.println("Accept Client: " + socket.toString());
        outputOb = new ObjectOutputStream(socket.getOutputStream());
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // gửi dữ liệu qua client
    private void send(LinkedHashMap data) {
        try {
            outputOb.writeObject(data);
//            ouput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//            ouput.write(data + "\n");
//            ouput.flush();
//            hehe.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // nhận từ client
    private String receive() {
        try {
            String data = input.readLine();
            System.out.println(data);
            return data;
        } catch (Exception e) {
            return "";
        }
    }

    // đóng cái stream
    private void closeAll() throws IOException {
        socket.close();
        ouput.close();
        input.close();
    }

    // hàm xử lính dữ liệu chính
    private LinkedHashMap<String, String> processData(String data) {
        try {
            LinkedHashMap<String, String> googleResult = getResponseFromGoogle(data);

            String title = googleResult.get("title");
            String subTitle = googleResult.get("subTitle");
            if (subTitle.contains("Bài hát")) {
                LinkedHashMap<String, String> songInfo = getLyricFromGG(googleResult);
                if (songInfo == null) {
                    String link = getLinkLyricFromBHH(  googleResult);
                    songInfo = getLyricFromBHH(link, googleResult);
                    songInfo.putIfAbsent("find", "true");
                }
                return songInfo;
            } else {
                System.out.println("Ca si");
                System.out.println(title);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // dùng gg search để tìm tên bài hát và ca sĩ
    private LinkedHashMap<String, String> getResponseFromGoogle(String data) {
        try {
            String ggSearchUrl = "https://www.google.com/search?q=";

            String fullUrl = ggSearchUrl + data;

            Document document = Jsoup.connect(fullUrl)
                    .method(Connection.Method.GET)
                    .execute().parse();

            // tach html tu goole search
            Element rnct = document.getElementById("rcnt");
            Elements yKMVIe = rnct.getElementsByClass("yKMVIe");

            LinkedHashMap<String, String> songInfo = new LinkedHashMap<>();


//            System.out.println(yKMVIe.get(0).text());
            String title = yKMVIe.get(0).text();

            title = title.split("\\(")[0].strip();
            if (title.contains("|")) {
                String[] sub = title.split("\\|");
                title = sub[1].strip();
            }
            songInfo.putIfAbsent("title", title);

            Elements wx62f_pzpZlf_x7XAkb = rnct.getElementsByClass("wx62f PZPZlf x7XAkb");
            String[] splitString = wx62f_pzpZlf_x7XAkb.get(0).text().split("\\s");
            String subTitle = wx62f_pzpZlf_x7XAkb.get(0).text();

            String singerName = "";
            for (int i = 3; i < splitString.length; i++) {
                singerName += splitString[i] + " ";
            }
//        System.out.println(singerName);
            songInfo.putIfAbsent("subTitle", subTitle);

            return songInfo;
        } catch (Exception e) {
            LinkedHashMap<String, String> tmp = new LinkedHashMap<>();

            tmp.putIfAbsent("title", null);
            tmp.putIfAbsent("subTitle", null);

            return tmp;
        }
    }

    // lấy lời bài hát từ gg search
    private LinkedHashMap<String, String> getLyricFromGG(LinkedHashMap<String, String> songInfo) {
        try {
            LinkedHashMap<String, String> returnHashMap = new LinkedHashMap<>();

            String songName = songInfo.get("title");
            String singerName = songInfo.get("subTitle");

            returnHashMap.putIfAbsent("songName", songName);
            returnHashMap.putIfAbsent("singerName", singerName);

            String ggSearchUrl = "https://www.google.com/search?q=";
            String qLBH = "Lời bài hát";

            String fullUrl = ggSearchUrl + qLBH + " " + songName + " của " + singerName;

            Document doc = Jsoup.connect(fullUrl)
                    .execute().parse();

            Element search = doc.getElementById("search");
            Element WbKHeb = search.getElementsByAttributeValue("jsname", "WbKHeb").get(0);
            Elements ujudUb = WbKHeb.getElementsByClass("ujudUb");

            String lyric = "";

            for (Element spans: ujudUb) {
                for (Element span: spans.getElementsByTag("span")) {
//                System.out.println(span.text());
                    lyric += span.text() + "\n";
                }
            }

            returnHashMap.putIfAbsent("songLyric", lyric);

            return returnHashMap;
        } catch (Exception e) {
            return null;
        }
    }

    // lấy link lời bài hát từ baihathay.net
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

    // lấy lời bài hát từ baihathay.net
    private LinkedHashMap<String, String> getLyricFromBHH(String linkLyric, LinkedHashMap<String, String> songInfo) {
        try {
            String fullUrl = "https://baihathay.net" + linkLyric;
            Document doc = Jsoup.connect(fullUrl)
                    .execute().parse();

            Element tabLyric = doc.getElementsByClass("tab-lyrics").last();
            String lyric = tabLyric.toString().split("\n")[1]
                    .replaceAll("<br>", "\n")
                    .replaceAll("<p>", "")
                    .replaceAll("</p>", "");


            LinkedHashMap<String, String> returnHashMap = new LinkedHashMap<>();

            returnHashMap.putIfAbsent("singerName", songInfo.get("subTitle"));
            returnHashMap.putIfAbsent("songName", songInfo.get("title"));
            returnHashMap.putIfAbsent("songLyric", lyric);

            return returnHashMap;
        } catch (Exception e) {
            return null;
        }
    }

    private String getLinkFormWiki(String name) {
        try {
            String apiLink = "https://vi.wikipedia.org/w/api.php?action=opensearch&search=";
            String url = apiLink + name;

            Document doc = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute().parse();

            JSONArray json = new JSONArray(doc.text());

            JSONArray links = (JSONArray) json.get(3);

            String link = links.get(0).toString();

            String[] subString = link.split("/");
            String singerName = subString[subString.length - 1];
            String decodeSingerName = URLDecoder.decode(singerName, StandardCharsets.UTF_8);
            System.out.println(decodeSingerName);


            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // hàm run từ thread
    @Override
    public void run() {
        while (true) {
            String data = receive();
            LinkedHashMap<String, String> processedData = processData(data);
            send(processedData);
//        try {
//            closeAll();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        }
    }
}
