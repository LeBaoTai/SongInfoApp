package main.java.client.connect;

import com.sun.jdi.connect.spi.TransportService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.sound.midi.Soundbank;
import javax.swing.plaf.IconUIResource;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAOtherPrimeInfo;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Connect implements Runnable {
    private int port;
    private Socket socket;
    private BufferedWriter ouput;
    private BufferedReader input;
    private ObjectOutputStream outputOb;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String SECRET_KEY;

    public Connect(Socket socket) throws Exception{
        System.out.println("Accept Client: " + socket.toString());
        this.socket = socket;
        outputOb = new ObjectOutputStream(socket.getOutputStream());
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ouput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        generateKeyPairAndSend();
    }

    private void generateKeyPairAndSend () {
        try {
            // nhan duoc client gui public key qua client
            SecureRandom sr = new SecureRandom();
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024, sr);
            KeyPair kp = kpg.generateKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();

            LinkedHashMap<String, Key> keyHashMap = new LinkedHashMap<String, Key>();
            keyHashMap.put("publicKey", publicKey);

            outputOb.writeObject(keyHashMap);
            receiveAES();
        } catch (Exception e) {
            System.out.println("Can't send public key!!!");
        }
    }

    private void receiveAES() {
        try {
            String encryptStr = input.readLine();
            PKCS8EncodedKeySpec pkc = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey1 = factory.generatePrivate(pkc);
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, privateKey1);
            byte[] decryptByte = c.doFinal(Base64.getDecoder().decode(encryptStr));
            SECRET_KEY = new String(decryptByte);
        } catch (Exception e) {
            System.out.println("Can't receive AES!!!");
        }
    }

    private String decryptDataAES(String data) {
        try {
            SecretKeySpec spec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, spec);
            byte[] decryptedByte = c.doFinal(Base64.getDecoder().decode(data.getBytes()));
            return new String(decryptedByte);
        } catch (Exception e) {
            System.out.println("Can't decrypt data!!!");
            return null;
        }
    }

    private String ecryptDataAES(byte[] byteOut) {
        try {
            SecretKeySpec spec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, spec);
            byte[] encryptedByte = c.doFinal(byteOut);
            String encryptedData = Base64.getEncoder().encodeToString(encryptedByte);

            return encryptedData;
        } catch (Exception e) {
            System.out.println("Can't encrypt data!!!");
            return null;
        }
    }

    // gửi dữ liệu qua client
    private void send(LinkedHashMap<String, String> data) {
        try {

            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(data);
            String ecryptedData = ecryptDataAES(byteOut.toByteArray());
//            outputOb.writeObject(data);
//            ouput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ouput.write(ecryptedData + "\n");
            ouput.flush();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't send data to client!!!");
        }
    }

    // nhận từ client
    private String receive() {
        try {
            String data = input.readLine();
            return decryptDataAES(data);
        } catch (Exception e) {
            System.out.println("Can't receive data!!!");
            return null;
        }
    }

    // đóng cái stream
    private void closeAll() throws IOException {
        outputOb.close();
        ouput.close();
        input.close();
        socket.close();
    }

    // hàm xử lính dữ liệu chính
    private LinkedHashMap<String, String> processData(String data) {
        try {
            LinkedHashMap<String, String> googleResult = getResponseFromGoogle(data);

            String title = googleResult.get("title");
            String subTitle = googleResult.get("subTitle");

            if (subTitle.contains("Bài hát")) {
                String linkVideo = googleResult.get("linkVideo");
                mySleep();
                LinkedHashMap<String, String> songInfo = getLyricFromGG(googleResult);
                mySleep();

                // tim kiem loi bai hat
                if (songInfo == null) {
//                    System.out.println("tim tu bhh" + title + " " + subTitle);
                    mySleep();
                    String link = getLinkLyricFromBHH(  googleResult);
                    songInfo = getLyricFromBHH(link, googleResult);
                }

                // tìm kiếm nhạc sỹ
                String composer = getComposerFormGG(googleResult);
                if (composer == null) {
                    mySleep();
                    String link = getLinkLyricFromBHH(  googleResult);
                    mySleep();
                    composer = getComposerFromBHH(link, googleResult);
                    songInfo.putIfAbsent("songComposer", composer);
                }
                if (composer == null) {
                    songInfo.putIfAbsent("songComposer", null);
                } else {
                    songInfo.putIfAbsent("songComposer", composer);
                }
                songInfo.putIfAbsent("linkVideo", linkVideo);

                // tim thong tin ca si ke ben bai hat
                String linkWiki = getLinkWikiFromGG(subTitle);
                mySleep();
                LinkedHashMap<String, String> allInfo = new LinkedHashMap<>();
                LinkedHashMap<String, String> singerInfo = null;

                if (linkWiki != null) {
                    singerInfo = getInfoFormWiki(linkWiki);
                    allInfo.putAll(singerInfo);
                }
                if (songInfo != null)
                    allInfo.putAll(songInfo);

                allInfo.putIfAbsent("find", "song");

                return allInfo;
            } else {
                subTitle = subTitle.toLowerCase();
                System.out.println(subTitle.contains("sĩ") || subTitle.contains("nhạc"));
                if (subTitle.contains("sĩ") || subTitle.contains("nhạc")
                    || subTitle.contains("nhóm") || subTitle.contains("ban")
                    || subTitle.contains("rapper") || subTitle.contains("c s")
                    || subTitle.contains("m n")) {
                    String linkWiki = getLinkWikiFromGG(title);
                    mySleep();
                    LinkedHashMap<String, String> singerInfo = null;
                    LinkedHashMap<String, String> allInfo = new LinkedHashMap<>();

                    if (linkWiki != null) {
                        singerInfo = getInfoFormWiki(linkWiki);
                        allInfo.putAll(singerInfo);
                    }

                    allInfo.putIfAbsent("title", title);
                    allInfo.putIfAbsent("find", "singer");

                    ArrayList<String> songs = getSongsFromGoogle(title);
                    mySleep();
                    String songStr = String.join(",", songs);
                    if (songs != null)
                        allInfo.putIfAbsent("songs", songStr);
                    return allInfo;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private ArrayList<String> getSongsFromGoogle(String data) {
        try {
            ArrayList<String> songs = new ArrayList<>();
            String q = "Bài hát của " + data;
            String ggSearchUrl = "https://www.google.com/search?q=";
            String fullUrl = ggSearchUrl + q;


            System.out.println(fullUrl);
            Document document = Jsoup.connect(fullUrl)
                    .followRedirects(false)
                    .execute().parse();

            Element element = null;

            try {
                if (element == null) {
                    element = document.getElementsByClass("uciohe").last();
                }
            } catch (Exception e) {

            }

            try {
                if (element == null) {
                    element = document.getElementsByClass("AxJnmb Wdsnue gIcqHd").last();
                }
            } catch (Exception e) {

            }

            try {
                if (element == null) {
                    element = document.getElementsByClass("AxJnmb").first();
                }
            } catch (Exception e) {

            }


            for (Element e: element.getElementsByTag("a")) {
                Element tmp = e.getElementsByClass("junCMe").first();
                String songName = tmp.getElementsByClass("title").text();
                songs.add(songName);
            }

            return songs;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't get songs from Google!!!");
            return null;
        }
    }

    // dùng gg search để tìm tên bài hát và ca sĩ và link bài hát, hoặc tìm kiếm thông tin cá nhân
    private LinkedHashMap<String, String> getResponseFromGoogle(String data) {
        try {

//            System.out.println("data ham tik gg" + data);
            String ggSearchUrl = "https://www.google.com/search?q=";

            String fullUrl = ggSearchUrl + data;

            Document document = Jsoup.connect(fullUrl)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .execute().parse();

            // tach html tu goole search
            Element rnct = document.getElementById("rcnt");
            Element yKMVIe = rnct.getElementsByClass("yKMVIe").get(0);

            LinkedHashMap<String, String> songInfo = new LinkedHashMap<>();

            String title = yKMVIe.text();
//            System.out.println("title " + title);

            title = title.split("\\(")[0].strip();
            if (title.contains("|")) {
                String[] sub = title.split("\\|");
                title = sub[1].strip();
            }
            songInfo.putIfAbsent("title", title);

            Elements wx62f_pzpZlf_x7XAkb = rnct.getElementsByClass("wx62f PZPZlf x7XAkb");
            String subTitle = wx62f_pzpZlf_x7XAkb.get(0).text();

            songInfo.putIfAbsent("subTitle", subTitle);

            try {
                Element H1u2de = rnct.getElementsByClass("H1u2de").first();
                Element tagA = H1u2de.getElementsByTag("a").first();
                songInfo.putIfAbsent("linkVideo", tagA.attr("href"));
            } catch (Exception e ) {
                songInfo.putIfAbsent("linkVideo", null);
            }

            return songInfo;
        } catch (Exception e) {
            System.out.println("Can't connect to Google!!!");
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
            String qLBH = "Lyric ";

//            String fullUrl = ggSearchUrl + qLBH + " " + songName + " của " + singerName;
            String fullUrl = ggSearchUrl + qLBH + " " + songName;

            Document doc = Jsoup.connect(fullUrl)
                    .followRedirects(false)
                    .execute().parse();

            Element search = doc.getElementById("search");
            Element WbKHeb = search.getElementsByAttributeValue("jsname", "WbKHeb").get(0);
            Elements ujudUb = WbKHeb.getElementsByClass("ujudUb");

            String lyric = "";

            for (Element spans: ujudUb) {
                for (Element span: spans.getElementsByTag("span")) {
                    lyric += span.text() + "\n";
                }
            }

            returnHashMap.putIfAbsent("songLyric", lyric);

            return returnHashMap;
        } catch (Exception e) {
            System.out.println("Can't get lyric from Google!!!");
            return null;
        }
    }



    // lấy thông tin tác giả của bài hát thông qua gg search
    private String getComposerFormGG(LinkedHashMap<String, String> songInfo) {
        try {
            String domain = "https://www.google.com/search?q=";
            String songName = songInfo.get("title");
            String[] subTitle = songInfo.get("subTitle").split(" ");

            String singerName = "";
            for (int i = 3; i < subTitle.length; i++) {
                singerName += subTitle[i] + " ";
            }

            String question = "who is the composer of ";

            String fullUrl = domain + question + songName + " " + singerName;

            Document doc = Jsoup.connect(fullUrl)
                    .followRedirects(false)
                    .execute().parse();

            Elements composers = doc.getElementsByClass("bVj5Zb FozYP");

            return composers.first().text();
        } catch (Exception e) {
            return null;
        }

    }

    // lấy thông tin tác giả của bài hát thông qua web baihathay.net
    private String getComposerFromBHH(String linkLyric, LinkedHashMap<String, String> songInfo) {
        try {

            String fullUrl = "https://baihathay.net" + linkLyric;
            Document doc = Jsoup.connect(fullUrl)
                    .followRedirects(false)
                    .execute().parse();

            Element songComposer = doc.getElementsByClass("artist-title").first();
            return songComposer.text();
        } catch (Exception e) {
            return null;
        }
    }

    // lấy link lời bài hát từ baihathay.net
    private String getLinkLyricFromBHH(LinkedHashMap<String, String> songInfo) {
        try {
            String songName = songInfo.get("title").strip();
            songName = songName.replaceAll("\\?", "")
                    .replaceAll("\\*", "")
                    .replaceAll("\\[", "")
                    .replaceAll("\\]", "")
                    .replaceAll("\\$", "")
                    .replaceAll("\\^", "");

            String singerName = "";
            String[] subString = songInfo.get("subTitle").strip().split(" ");

            for (int i = 3; i < subString.length; i++) {
                singerName += subString[i] + " ";
            }

            String bhhUrl = "https://baihathay.net/music/tim-kiem/";
            String fullUrl = bhhUrl + songName + "/trang-1.html";

            Document doc = Jsoup.connect(fullUrl)
                    .followRedirects(false)
                    .execute().parse();

            String link = "";
            Element pureMenuList = doc.getElementsByClass("pure-menu-list").last();
            for (Element pureMenuItem: pureMenuList.getElementsByClass("pure-menu-item")) {
                String song = pureMenuItem.text();
//                int len = song.split("-").length;
                String singer  = song.split("-")[1].strip();

                if (singer.compareToIgnoreCase(singerName.strip()) == 0) {
                    link = pureMenuItem.getElementsByTag("a").first().attr("href");
                    return link;
                }
                if (singer.contains(singerName.strip())) {
                    link = pureMenuItem.getElementsByTag("a").first().attr("href");
                    return link;
                }
            }

            // doạn tìm kiếm link để có thể tìm kiếm nhạc sỹ bài hát
            for (Element pureMenuItem: pureMenuList.getElementsByClass("pure-menu-item")) {
                String line = pureMenuItem.text();
//                int len = song.split("-").length;
                if(line.split("-").length < 3) {
                    continue;
                }
                String song  = line.split("-")[0].strip().toLowerCase();

                if (song.contains(songName.strip().toLowerCase())) {
                    link = pureMenuItem.getElementsByTag("a").first().attr("href");
                    return link;
                }
            }

            pureMenuList = doc.getElementsByClass("pure-menu-list").first();
            link = pureMenuList.getElementsByTag("a").first().attr("href");
            return "/music" + link.split("music")[1];
        } catch (Exception e) {
            System.out.println("Can't get link from BHH");
            return null;
        }
    }

    // lấy lời bài hát từ baihathay.net
    private LinkedHashMap<String, String> getLyricFromBHH(String linkLyric, LinkedHashMap<String, String> songInfo) {
        try {
            String fullUrl = "https://baihathay.net" + linkLyric;
            Document doc = Jsoup.connect(fullUrl)
                    .followRedirects(false)
                    .execute().parse();

            Element songComposer = doc.getElementsByClass("artist-title").first();
            Element tabLyric = doc.getElementsByClass("tab-lyrics").last();
            String lyric = tabLyric.toString().split("\n")[1]
                    .replaceAll("<br>", "\n")
                    .replaceAll("<p>", "")
                    .replaceAll("</p>", "")
                    .strip();


            LinkedHashMap<String, String> returnHashMap = new LinkedHashMap<>();

            returnHashMap.putIfAbsent("singerName", songInfo.get("subTitle"));
            returnHashMap.putIfAbsent("songName", songInfo.get("title"));
            returnHashMap.putIfAbsent("songLyric", lyric);
            returnHashMap.putIfAbsent("songComposer", songComposer.text());

            return returnHashMap;
        } catch (Exception e) {
            return null;
        }
    }

    private String getLinkWikiFromGG(String subTitle) {
        try {
            LinkedHashMap<String, String> returnHashMap = new LinkedHashMap<>();
            String domain = "https://www.google.com/search?q=";

            String name = "";
            if (subTitle.contains("Bài hát")) {
                String[] sub = subTitle.split(" ");
                for (int i = 3; i < sub.length; i++) {
                    name += sub[i] + " ";
                }

                if (name.contains("và")) {
                    name = name.strip();
                    name = name.split("và")[0];
                }
            } else {
                name = subTitle;
            }

            String fullUrl = domain + name;

            Document doc = Jsoup.connect(fullUrl)
                    .followRedirects(false)
                    .execute().parse();

            Element tagA = doc.getElementsByClass("ruhjFe NJLBac fl").first();
            String linkWiki = tagA.attr("href");
            String nameFromLink = linkWiki.split("/")[linkWiki.split("/").length-1];

            return nameFromLink;

        } catch (Exception e) {
            System.out.println("Can't get link info from Google!!!");
            return null;
        }
    }

    private LinkedHashMap<String, String> getInfoFormWiki(String link) {
        try {
            String apiLink = "https://vi.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=";
            String url = apiLink + link;

            Document doc = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
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
            JSONObject revision = (JSONObject) revisions.get(0);
            String content = revision.get("*").toString();

            String[] subContent = content.split("'''");
            String name = subContent[1];

            String fullName = "";
            String dateOfBirth = "";
            String placeOfBirth = "";
            String occupation = "";
            String genre = "";
            String[] infoContent = subContent[0].replaceAll("\\| ", "").split("\n");

            for (String str: infoContent) {
                if(!str.contains(" = ")) continue;
                String key = str.split("=")[0].strip();
                String value = str.split("=")[1].strip();
                if (key.equalsIgnoreCase("birth_name")
                        || key.equalsIgnoreCase("tên khai sinh")) {
                    fullName = value;
                }
                if (key.equalsIgnoreCase("birth_date")
                        || key.equalsIgnoreCase("ngày sinh")) {
                    value = value.replaceAll("\\{", "")
                            .replaceAll("}", "");
                    String[] subValue = value.split("\\|");
                    dateOfBirth = subValue[3] + "/" + subValue [2] + "/" + subValue[1];
                }
                if (key.equalsIgnoreCase("birth_place")
                        || key.equalsIgnoreCase("nơi sinh")) {
                    placeOfBirth = value.replaceAll("\\[", "")
                            .replaceAll("]", "");

                }
                if (key.equalsIgnoreCase("genre")
                        || key.equalsIgnoreCase("thể loại âm nhạc")) {
                    if (value.contains("|")) {
                        value = value.replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll("\\{", "")
                                .replaceAll("}", "");
                        String[] subStr = value.split("\\|");
                        for (int i = 1; i < subStr.length; i++) {
                            genre += subStr[i] + " ";
                        }
                    } else {
                        value = value.replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll("\\{", "")
                                .replaceAll("}", "");
                        genre = value;
                    }
                }
                if (key.equalsIgnoreCase("occupation")
                        || key.equalsIgnoreCase("nghề nghiệp")) {
                    if (value.contains("|")) {
                        value = value.replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll("\\{", "")
                                .replaceAll("}", "");
                        String[] subStr = value.split("\\|");
                        for (int i = 1; i < subStr.length; i++) {
                            occupation += subStr[i] + " ";
                        }
                    }
                    else {
                        value = value.replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll("\\{", "")
                                .replaceAll("}", "");
                        genre = value;
                    }
                }
            }

            LinkedHashMap<String, String> returnHashMap = new LinkedHashMap<>();
            if (fullName.isBlank())
                returnHashMap.putIfAbsent("fullName", name);
            else
                returnHashMap.putIfAbsent("fullName", fullName);
            if (!dateOfBirth.isBlank())
                returnHashMap.putIfAbsent("dateOfBirth", dateOfBirth);
            if (!placeOfBirth.isBlank())
                returnHashMap.putIfAbsent("placeOfBirth", placeOfBirth);
            if (!genre.isBlank())
                returnHashMap.putIfAbsent("genre", genre);
            if (!occupation.isBlank())
                returnHashMap.putIfAbsent("occupation", occupation);

            return returnHashMap;
        } catch (Exception e) {
            System.out.println("Can't get infor from wiki!!!");
            return null;
        }
    }

    private void mySleep() {
        Random ran = new Random();
        int ranNum = 2000 + ran.nextInt(1000);
        int num = 1;
        for (int i = 0; i < ranNum; i++) {
            num += ranNum * 3 / 2 + ranNum;
        }
    }

    // hàm run từ thread
    @Override
    public void run() {
        try {
            while (true) {
                String data = receive();
                if (data.equals("close")) {
                    break;
                }
                LinkedHashMap<String, String> processedData = processData(data);

                send(processedData);
            }
            System.out.println("Close socket: " + socket.toString());
            closeAll();
        } catch (Exception e) {
            System.out.println("Can't stop!!!");
        }
    }
}