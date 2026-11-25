package com.linkly.web;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.linkly.model.LinkException;
import com.linkly.model.ShortLink;
import com.linkly.service.LinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.net.URI;

@Controller
public class LinkController {

    private final LinkService service;

    public LinkController(LinkService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recent", service.recent());
        return "home";
    }

    @PostMapping("/shorten")
    public String shorten(@RequestParam String url,
                          @RequestParam(required = false) String alias,
                          @RequestParam(required = false) String expiry,
                          RedirectAttributes flash) {
        try {
            ShortLink link = service.create(url, alias, expiry);
            return "redirect:/result/" + link.getCode();
        } catch (LinkException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/result/{code}")
    public String result(@PathVariable String code, Model model) {
        model.addAttribute("link", service.find(code));
        model.addAttribute("shortUrl", shortUrl(code));
        return "result";
    }

    @GetMapping("/stats/{code}")
    public String stats(@PathVariable String code, Model model) {
        model.addAttribute("link", service.find(code));
        model.addAttribute("shortUrl", shortUrl(code));
        return "stats";
    }

    @GetMapping(value = "/qr/{code}.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] qr(@PathVariable String code) throws Exception {
        service.find(code); // 404 if unknown
        BitMatrix matrix = new QRCodeWriter().encode(shortUrl(code), BarcodeFormat.QR_CODE, 300, 300);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    // The regex keeps this route from swallowing other paths
    @GetMapping("/{code:[a-zA-Z0-9_-]+}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String target = service.resolve(code);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }

    private String shortUrl(String code) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/" + code).toUriString();
    }
}
