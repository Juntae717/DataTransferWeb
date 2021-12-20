package com.assignment.datatransferweb.Service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class DataControllerWebService {
    private ByteArrayOutputStream dataTemp; // 읽어온 이미지를 임시 저장할 공간
    private final String FILE_PATH = "D:\\DataTransfer_Image\\"; // 파일을 저장할 경로
    private final String FILE_EXTENSION = ".jpg"; // 파일 확장자 정보
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat ("yyyyMMddHHmmSS"); // 파일 이름 포맷 형식(날짜 + 시간)

    /**
     * FUNCTION :: 이미지 캠쳐
     * @param request
     */
    public void captureImage(HttpServletRequest request) {
        System.out.println(getClass() + ".captureImage()");
        connectAPI(request, "http://192.168.0.240/cgi-bin/snapshot.jpg");
    }

    /**
     * FUNCTION :: CCTV API 연결
     * @param request
     * @param API_URL
     */
    public void connectAPI(HttpServletRequest request, final String API_URL) {
        System.out.println(getClass() + ".connectAPI()");
        try {
            URL url = new URL(API_URL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "image/jpeg");
            conn.setRequestMethod("GET");

            conn.setDoOutput(false);

            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                readData(request, conn.getInputStream());
                System.out.println(conn.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * FUNCTION :: 데이터를 불러와 읽은 뒤 저장
     * @param request
     * @param inputStream
     */
    public void readData(HttpServletRequest request, InputStream inputStream) {
        System.out.println(getClass() + ".readData()");
        try {
            ByteArrayOutputStream imgData = new ByteArrayOutputStream();

            int len;
            byte[] buffer = new byte[1024];
            while((len = inputStream.read(buffer)) != -1) {
                imgData.write(buffer, 0, len);
            }
            dataTemp = imgData;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * FUNCTION :: 이미지 전송
     * @param request
     */
    public void sendImage(HttpServletRequest request) {
        System.out.println(getClass() + ".sendImage()");
        connectReceiver(request, "http://192.168.0.104/receive");
    }

    /**
     * FUNCTION :: 데이터를 수신할 URL 연결
     * @param request
     * @param RECEIVER_URL
     */
    public void connectReceiver(HttpServletRequest request, final String RECEIVER_URL) {
        System.out.println(getClass() + ".connectReceiver()");

        /**
         * LINE :: byte array to hex
         */
        StringBuilder builder = new StringBuilder();
        byte[] bytes = dataTemp.toByteArray();
        for(byte data: bytes) {
            builder.append(String.format("%02X", data&0xff));
        }

        try {
            URL url = new URL(RECEIVER_URL + "?param1=" + builder);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("GET");

//            conn.setDoOutput(true);
//
//            sendData(request, conn.getOutputStream());
            System.out.println(conn.getResponseCode());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * FUNCTION :: 읽어 둔 데이터 전송
     * @param request
     * @param outputStream
     */
    public void sendData(HttpServletRequest request, final OutputStream outputStream) {
        System.out.println(getClass() + ".sendData()");
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.write(dataTemp.toByteArray());
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * FUNCTION :: 전송된 데이터 수신
     * @param request
     */
    public void receiveImage(HttpServletRequest request) {
        System.out.println(getClass() + ".receiveImage()");
        Date time = new Date();
        String fileName = FORMAT.format(time);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(FILE_PATH + fileName + FILE_EXTENSION);;

            /**
             * LINE :: hex to byte array
             */
            String hexText = request.getParameter("param1");
            int hxLen = hexText.length();
            byte[] bytes = new byte[hxLen/2];
            for(int i = 0; i < hxLen; i += 2) {
                bytes[i / 2] = (byte) ((Character.digit(hexText.charAt(i), 16) << 4) + Character.digit(hexText.charAt(i + 1), 16));
            }

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            int len;
            byte[] buffer = new byte[1024];
            while((len = byteArrayInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}