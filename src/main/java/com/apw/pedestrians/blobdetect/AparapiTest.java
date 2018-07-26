package com.apw.pedestrians.blobdetect;

import com.aparapi.Kernel;
import com.apw.pedestrians.image.FileImage;
import com.apw.pedestrians.image.IPixel;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class AparapiTest extends Application {
    public static void main(String[] args) {
//        basicTest();
        launch(args);
    }

    private static void basicTest() {
        int bufSize = 640;
        int bufCount = 480;
        int[] buf = new int[bufCount * bufSize];
        for (int i = 0; i < bufCount; i++) {
            for (int j = 0; j < bufSize; j++) {
                buf[(i * bufSize) + j] = i;
            }
        }
        System.out.println(Arrays.toString(buf));
        Kernel k = new Kernel() {
            @Override
            public void run() {
                for (int i = 0; i < bufSize; i++) {
                    buf[(getGlobalId() * bufSize) + i] = buf[(getGlobalId() * bufSize) + i] + 1;
                }
            }
        };
        k.execute(bufCount);
        k.dispose();
        System.out.println(Arrays.toString(buf));
    }

    BlobDetection blobDetection = new BlobDetection();
    PrimitiveBlobDetection primitiveBlobDetection = new PrimitiveBlobDetection();
    Canvas canvas;
    HBox box;
    final int SCALE = 2;
    boolean gpu = false;

    void run(FileImage image) {
        image.readCam();
        List<Blob> blobs = gpu ? primitiveBlobDetection.getBlobs(image) : blobDetection.getBlobs(image);

        IPixel[][] img = image.getImage();
        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[0].length; j++) {
                IPixel pixel = img[i][j];
                canvas.getGraphicsContext2D().setFill(getPaint(pixel));
                canvas.getGraphicsContext2D().fillRect(j * SCALE, i * SCALE, SCALE, SCALE);
            }
        }
        canvas.getGraphicsContext2D().setLineWidth(3);
        canvas.getGraphicsContext2D().setStroke(gpu ? Color.LIME : Color.CYAN);
        for (Blob blob : blobs) {
            canvas.getGraphicsContext2D().strokeRect(blob.x * SCALE, blob.y * SCALE, blob.width * SCALE, blob.height * SCALE);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Download from https://drive.google.com/open?id=0B9CBX0V9uENacVpybGxINXA0c0E
        File file = new File("FlyCapped6.By8");
        FileImage image = new FileImage(file.getPath(), true);
        image.readCam();
        IPixel[][] img = image.getImage();
        box = new HBox();
        canvas = new Canvas(img[0].length * SCALE, img.length * SCALE);
        box.getChildren().add(canvas);
        primaryStage.setScene(new Scene(box));
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                run(image);
            }
        }.start();
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> gpu = event.getCode() == KeyCode.SPACE ^ gpu);
        primaryStage.show();
    }

    @Override
    public void stop() {
        primitiveBlobDetection.dispose();
    }

    private static Paint getPaint(IPixel p) {
        switch (p.getColor()) {
            case RED:
                return (Color.RED);
            case GREEN:
                return (Color.LIME);
            case BLUE:
                return (Color.BLUE);
            case GREY:
                return (Color.GRAY);
            case BLACK:
                return (Color.BLACK);
            case WHITE:
                return (Color.WHITE);
            default:
                throw new IllegalStateException("Invalid color code " + p.getColor() + ".");
        }
    }
}