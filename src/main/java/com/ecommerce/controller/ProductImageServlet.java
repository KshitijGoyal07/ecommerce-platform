package com.ecommerce.controller;

import com.ecommerce.service.ProductImageStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Serves only validated, generated product-photo filenames from the upload directory. */
@WebServlet("/product-images/*")
public final class ProductImageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2 || pathInfo.substring(1).contains("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Path imagePath;
        try {
            imagePath = ProductImageStorage.resolve(pathInfo.substring(1));
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!Files.isRegularFile(imagePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String filename = imagePath.getFileName().toString();
        response.setContentType(filename.endsWith(".png") ? "image/png" : "image/jpeg");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "public, max-age=3600");
        response.setContentLengthLong(Files.size(imagePath));
        try (var input = Files.newInputStream(imagePath)) {
            input.transferTo(response.getOutputStream());
        }
    }
}
