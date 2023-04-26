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
        LinkedHashMap<String, String> songInfo = new LinkedHashMap<>();
        LinkedHashMap<String, String> googleResult = getResponseFromGoogle(data);
        String songName = googleResult.get("songName").strip();
        String singerName = googleResult.get("singerName").strip();

        if (singerName != null && songName != null) {
            String linkLyric = getLyricLink(songName, singerName);

            if (linkLyric != null) {
                songInfo.putIfAbsent("songName", songName);
                songInfo.putIfAbsent("singerName", singerName);
                String lyric = getLyric(linkLyric);
                songInfo.putIfAbsent("songLyric", lyric);
            }
        }

        return songInfo != null? songInfo : null;
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


//        System.out.println(yKMVIe.get(0).text());
            String songName = yKMVIe.get(0).text();
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

    // lấy link của bài hát
    private String getLyricLink(String songName, String singerName) {
//        System.out.println(songName);
//        System.out.println(singerName);
        try {
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
            } else if (songs.size() > 2) {
                Element tmp = new Element("temp");
                for (Element song : songs) {
                    String ten = song.getElementsByClass("ten").text();
                    String nhacsy = song.getElementsByClass("nhacsy").text();
                    if (ten.compareToIgnoreCase(songName) == 0 && nhacsy.compareToIgnoreCase(singerName) == 0) {
                        tmp = song;
                        break;
//                    System.out.println(ten + " " + nhacsy);
                    }
                    if (ten.compareToIgnoreCase(songName) == 0) {
                        tmp = song;
                    }
                }

                String ten = tmp.getElementsByClass("ten").text();
                String nhacsy = tmp.getElementsByClass("nhacsy").text();
                linkLyric = tmp.getElementsByTag("a").attr("href");
//                System.out.println(ten + " " + nhacsy);
//                System.out.println(linkLyric);
            }
            return linkLyric;
        } catch (Exception e) {
            return null;
        }
    }

    // lấy lời bài hát
    private String getLyric(String linkLyric) {
        try {
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
            String[] subStringLyric = lyricSong.toString().split("<br>");
            int lengthLyric = lyricSong.toString().split("<br>").length;
            String fullLyric = "";
            for (int i = 1; i < lengthLyric - 1; i++) {
                fullLyric += subStringLyric[i].strip() + "\n";
            }

//        System.out.println(hehe);
            return fullLyric;
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
