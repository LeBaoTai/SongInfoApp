package main.java.client.connect;

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

    public Connect(Socket socket) throws Exception{
        this.socket = socket;
        System.out.println("Accept Client: " + socket.toString());
    }

    // gửi dữ liệu qua client
    private void send(LinkedHashMap data) {
        try {
            ObjectOutputStream outputOb = new ObjectOutputStream(socket.getOutputStream());
            outputOb.writeObject(data);
            outputOb.close();

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
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
//        ouput.close();
        input.close();
    }

    // hàm xử lính dữ liệu chính
    private LinkedHashMap<String, String> processData(String data) {
        try {
            LinkedHashMap<String, String> googleResult = getResponseFromGoogle(data);
            LinkedHashMap<String, String> songInfo = getLyricFromGG(googleResult);
            if (songInfo == null) {
                String link = getLinkLyricFromBHH(  googleResult);
                songInfo = getLyricFromBHH(link, googleResult);
            }
            return songInfo;
        } catch (Exception e) {
            return null;
        }
    }

    // dùng gg search để tìm tên bài hát và ca sĩ
    private LinkedHashMap<String, String> getResponseFromGoogle(String data) {
        try {
            String ggSearchLink = "https://www.google.com/search?q=";

            String fullLink = ggSearchLink + data;

            Document document = Jsoup.connect(fullLink)
                    .method(Connection.Method.GET)
                    .execute().parse();

            // tach html tu goole search
            Element rnct = document.getElementById("rcnt");
            Elements yKMVIe = rnct.getElementsByClass("yKMVIe");

            LinkedHashMap<String, String> songInfo = new LinkedHashMap<>();


//            System.out.println(yKMVIe.get(0).text());
            String songName = yKMVIe.get(0).text();

            songName = songName.split("\\(")[0].strip();

            songInfo.putIfAbsent("songName", songName);

            Elements wx62f_pzpZlf_x7XAkb = rnct.getElementsByClass("wx62f PZPZlf x7XAkb");
            String[] splitString = wx62f_pzpZlf_x7XAkb.get(0).text().split("\\s");

            String singerName = "";
            for (int i = 3; i < splitString.length; i++) {
                singerName += splitString[i] + " ";
            }
//        System.out.println(singerName);
            songInfo.putIfAbsent("singerName", singerName);

            return songInfo;
        } catch (Exception e) {
            LinkedHashMap<String, String> tmp = new LinkedHashMap<>();

            tmp.putIfAbsent("songName", null);
            tmp.putIfAbsent("singerName", null);

            return tmp;
        }
    }

    // lấy lời bài hát từ gg search
    private LinkedHashMap<String, String> getLyricFromGG(LinkedHashMap<String, String> songInfo) {
        try {
            LinkedHashMap<String, String> returnHashMap = new LinkedHashMap<>();

            String songName = songInfo.get("songName");
            String singerName = songInfo.get("singerName");

            returnHashMap.putIfAbsent("songName", songName);
            returnHashMap.putIfAbsent("singerName", singerName);

            String ggSearchLink = "https://www.google.com/search?q=";
            String qLBH = "Lời bài hát";

            String fullLink = ggSearchLink + qLBH + " " + songName + " của " + singerName;

            Document doc = Jsoup.connect(fullLink)
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
            String bhhLink = "https://baihathay.net/music/tim-kiem/";
            String fullLink = bhhLink + songInfo.get("songName") + "/trang-1.html";

            Document doc = Jsoup.connect(fullLink)
                    .execute().parse();

            String link = "";
            String singerName = songInfo.get("singerName").strip();
            String songName = songInfo.get("songName").strip();

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

            System.out.println(link);
            return link;
        } catch (Exception e) {
            return null;
        }
    }

    // lấy lời bài hát từ baihathay.net
    private LinkedHashMap<String, String> getLyricFromBHH(String linkLyric, LinkedHashMap<String, String> songInfo) {
        try {
            linkLyric = "https://baihathay.net" + linkLyric;
            Document doc = Jsoup.connect(linkLyric)
                    .execute().parse();

            Element tabLyric = doc.getElementsByClass("tab-lyrics").last();
            String lyric = tabLyric.toString().split("\n")[1].replaceAll("<br>", "\n");

            LinkedHashMap<String, String> returnHashMap = new LinkedHashMap<>();

            returnHashMap.putIfAbsent("singerName", songInfo.get("singerName"));
            returnHashMap.putIfAbsent("songName", songInfo.get("songName"));
            returnHashMap.putIfAbsent("songLyric", lyric);

            return returnHashMap;
        } catch (Exception e) {
            return null;
        }
    }

    // hàm run từ thread
    @Override
    public void run() {
        String data = receive();
        LinkedHashMap<String, String> processedData = processData(data);
        send(processedData);
        try {
            closeAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
