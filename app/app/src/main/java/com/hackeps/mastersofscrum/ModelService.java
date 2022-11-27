package com.hackeps.mastersofscrum;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ModelService {

    CascadeClassifier cascadeClassifier;
    File caseFile;

    public ModelService(CascadeClassifier cascadeClassifier) {
        this.cascadeClassifier = cascadeClassifier;
    }

    public String recogniseFaces(Mat input) throws IOException {
        // TODO Ficar URL correcta
        URL url = new URL("https://9ae2-193-144-12-226.eu.ngrok.io/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        Mat cropped = cropToLargest(input);
        if (cropped == null){
            return "NO FACE";
        }
        String jsonInputString = imageToJSON(resizeImage(input));
        try(OutputStream os = connection.getOutputStream()) {
            byte[] inputBytes = jsonInputString.getBytes("utf-8");
            os.write(inputBytes, 0, inputBytes.length);
        }
        String result;
        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result2 = bis.read();
        while(result2 != -1) {
            buf.write((byte) result2);
            result2 = bis.read();
        }
        result = buf.toString();

        return result;
    }

    public String sendCropped(Mat input) throws IOException {
        URL url = new URL("https://9ae2-193-144-12-226.eu.ngrok.io/api/emotions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = imageToJSON(resizeImage(input));
        try(OutputStream os = connection.getOutputStream()) {
            byte[] inputBytes = jsonInputString.getBytes("utf-8");
            os.write(inputBytes, 0, inputBytes.length);
        }
        String result;
        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result2 = bis.read();
        while(result2 != -1) {
            buf.write((byte) result2);
            result2 = bis.read();
        }
        result = buf.toString();

        return result;
    }

    private String imageToJSON(Mat input) {
        StringBuilder st = new StringBuilder("{\"image\": \"[");
        for(int i = 0; i < input.rows(); i++){
            st.append("[");
            for (int j = 0; j < input.cols(); j++){
                double[] data = input.get(i, j);
                st.append(data[0]);
                st.append(",");
            }
            st.deleteCharAt(st.length()-1);
            st.append("],");
        }
        st.deleteCharAt(st.length()-1);
        st.append("]\"}");
        return st.toString();
    }

    public Mat resizeImageBig(Mat input){
        Mat input_resized = new Mat();
        Size sz = new Size(800, 600);
        //Imgproc.rectangle(input, new Point(0,0), new Point(100,120), new Scalar(255,0,0), 2);
        Imgproc.resize(input, input_resized, sz);
        return input_resized;
    }

    public Mat resizeImage(Mat input){
        Mat input_resized = new Mat();
        Size sz = new Size(48, 48);
        //Imgproc.rectangle(input, new Point(0,0), new Point(100,120), new Scalar(255,0,0), 2);
        Imgproc.resize(input, input_resized, sz);
        return input_resized;
    }

    public Mat cropToLargest(Mat input){
        Rect largest = detectBiggestFace(input);
        if (largest == null){
            return null;
        }
        return new Mat(input, largest);
    }

    public Rect detectBiggestFace(Mat input){
        MatOfRect facedetections = new MatOfRect();
        cascadeClassifier.detectMultiScale(input, facedetections);

        Rect largest = null;
        for (Rect rect : facedetections.toArray()) {
            if (largest == null){
                largest = rect;
            } else {
                if (rect.area() > largest.area()){
                    largest = rect;
                }
            }
        }

        return largest;
    }

    public Mat detection(Mat input){
        MatOfRect facedetections = new MatOfRect();
        cascadeClassifier.detectMultiScale(input, facedetections);

        Rect largest = null;
        for (Rect rect : facedetections.toArray()) {
            if (largest == null){
                largest = rect;
            } else {
                if (rect.area() > largest.area()){
                    largest = rect;
                }
            }
        }

        if (largest == null){
            return input;
        }
        Imgproc.rectangle(input,
                new Point(largest.x, largest.y),
                new Point(largest.x + largest.width, largest.y + largest.height),
                new Scalar(255,0,0), 3);

        return input;
    }

    public Mat cropToRect(Mat input, Rect rect) {
        return new Mat(input, rect);
    }
}
