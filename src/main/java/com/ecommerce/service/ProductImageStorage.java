package com.ecommerce.service;

import jakarta.servlet.http.Part;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

/** Validates, normalizes, and stores optional seller product photos outside the deployed WAR. */
public final class ProductImageStorage {
    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;
    private static final int MAX_IMAGE_DIMENSION = 6000;

    private ProductImageStorage() { }

    /** Returns null when the file input is empty; otherwise returns the generated filename. */
    public static String save(Part part) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        if (part.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Product photo must be 5 MB or smaller.");
        }

        String contentType = part.getContentType() == null
                ? "" : part.getContentType().toLowerCase(Locale.ROOT).trim();
        final String extension;
        final String outputFormat;
        if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)) {
            extension = ".jpg";
            outputFormat = "jpeg";
        } else if ("image/png".equals(contentType)) {
            extension = ".png";
            outputFormat = "png";
        } else {
            throw new IllegalArgumentException("Upload a JPEG or PNG product photo.");
        }

        BufferedImage image;
        try (InputStream input = part.getInputStream();
             ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) {
                throw new IllegalArgumentException("The selected file is not a readable image.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("The selected file is not a supported image.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String actualFormat = reader.getFormatName().toLowerCase(Locale.ROOT);
                boolean actualPng = actualFormat.contains("png");
                boolean actualJpeg = actualFormat.contains("jpeg") || actualFormat.equals("jpg");
                if (("png".equals(outputFormat) && !actualPng)
                        || ("jpeg".equals(outputFormat) && !actualJpeg)) {
                    throw new IllegalArgumentException("The image content does not match its file type.");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1
                        || width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION
                        || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new IllegalArgumentException("Image dimensions are too large. Use an image up to 6,000 pixels wide or tall.");
                }
                image = reader.read(0);
                if (image == null) {
                    throw new IllegalArgumentException("The selected file is not a valid image.");
                }
            } finally {
                reader.dispose();
            }
        }

        String filename = UUID.randomUUID() + extension;
        Path target = resolve(filename);
        Files.createDirectories(target.getParent());
        try (OutputStream output = Files.newOutputStream(
                target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            if (!ImageIO.write(image, outputFormat, output)) {
                throw new IOException("No image encoder is available for this photo.");
            }
        } catch (IOException | RuntimeException exception) {
            Files.deleteIfExists(target);
            throw exception;
        }
        if (Files.size(target) > MAX_IMAGE_BYTES) {
            Files.deleteIfExists(target);
            throw new IllegalArgumentException("The normalized photo is larger than 5 MB. Choose a smaller image.");
        }
        return filename;
    }

    /** Resolves only generated image filenames, preventing arbitrary filesystem reads. */
    public static Path resolve(String filename) {
        if (filename == null || !filename.matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png)")) {
            throw new IllegalArgumentException("Product image filename is invalid.");
        }
        Path root = storageDirectory();
        Path resolved = root.resolve(filename).normalize();
        if (!resolved.getParent().equals(root)) {
            throw new IllegalArgumentException("Product image filename is invalid.");
        }
        return resolved;
    }

    /** Deletes an old image after its database reference has been removed or replaced. */
    public static void delete(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Files.deleteIfExists(resolve(filename));
        } catch (IOException | IllegalArgumentException ignored) {
            // Cleanup must not hide the result of a successful database operation.
        }
    }

    private static Path storageDirectory() {
        String configuredDirectory = System.getenv("MARKETSQUARE_UPLOAD_DIR");
        if (configuredDirectory != null && !configuredDirectory.isBlank()) {
            return Path.of(configuredDirectory).toAbsolutePath().normalize();
        }
        String catalinaBase = System.getProperty("catalina.base");
        String base = catalinaBase == null || catalinaBase.isBlank()
                ? System.getProperty("user.home", System.getProperty("java.io.tmpdir"))
                : catalinaBase;
        return Path.of(base, "marketsquare-uploads", "products").toAbsolutePath().normalize();
    }
}
