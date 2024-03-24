package com.example.ws;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;


@RestController
@RequestMapping("/barcode")
public class BarCodeController {

    @PostMapping(value = "/decode", consumes = "multipart/form-data")
    public ResponseEntity<String> decodeBarcode(@RequestParam("file") MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            String decodedText = decodeBarcode(image);
            return ResponseEntity.ok(decodedText);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error decoding barcode");
        }
    }

    private String decodeBarcode(BufferedImage image) {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        MultiFormatReader reader = new MultiFormatReader();
        LuminanceSource luminanceSource = new BufferedImageLuminanceSource(image);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));

        try {
            Result result = reader.decode(binaryBitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
            return "Barcode not found";
        }
    }

    @PostMapping("/")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile image) throws IOException, NotFoundException {
        // Check if image is uploaded
        if (image.isEmpty()) {
            return new ResponseEntity<>("Please select an image to upload", HttpStatus.BAD_REQUEST);
        }

        // Save the image (optional)
//        String filePath = saveImage(image);

        // Decode barcode from image
        String barcodeData = decodeBarcodeG("123");

        return new ResponseEntity<>("Barcode data: " + barcodeData, HttpStatus.OK);
    }


    // Method to decode barcode (using ZXing)
    private String decodeBarcodeG(String filePath) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(new File(filePath));
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        Result result = reader.decode(bitmap);
        return result.getText();
    }


    @GetMapping(value = "/generate/{content}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateBarcode(
            @PathVariable String content,
            HttpServletResponse response) {
        try {
            byte[] barcodeImage = generateBarcodeImage(content);

            // Set the response headers
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=barcode.png");

            return ResponseEntity.ok().body(barcodeImage);
        } catch (IOException | WriterException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping(value = "/generate-2/{content}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> generateQRCode(
            @PathVariable String content,
            HttpServletResponse response) {
        try {
            BufferedImage barcodeImage = generateQRCodeImage(content);

            // Set the response headers
//            response.setContentType(MediaType.IMAGE_PNG_VALUE);
////            response.setHeader("Content-Disposition", "attachment; filename=barcode.png");
//            response.setHeader("Content-Disposition", "inline; filename=barcode.png");

            return ResponseEntity.ok().body(barcodeImage);
        } catch (IOException | WriterException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generateBarcodeImage(String content) throws IOException, WriterException {
        int width = 300;
        int height = 150;

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, com.google.zxing.BarcodeFormat.CODE_128, width, height, hints);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);

        return outputStream.toByteArray();
    }

    public static BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
                barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public static BufferedImage generateBarcodeImage2(String barcodeText) throws Exception {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(barcodeText, BarcodeFormat.CODE_128, 200, 200);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    @GetMapping(value = "/generate-3/{content}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> generateBarcode2(
            @PathVariable String content,
            HttpServletResponse response) {
        try {
            BufferedImage barcodeImage = generateBarcodeImage2(content);

            // Set the response headers
//            response.setContentType(MediaType.IMAGE_PNG_VALUE);
////            response.setHeader("Content-Disposition", "attachment; filename=barcode.png");
//            response.setHeader("Content-Disposition", "inline; filename=barcode.png");

            return ResponseEntity.ok().body(barcodeImage);
        } catch (IOException | WriterException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}