package com.example.foodfight.yolotest;

import java.io.IOException;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.ui.Model;

@Controller
public class YoloController {
    private final YoloService yoloService;
	
    public YoloController(YoloService yoloService) {
        this.yoloService = yoloService;
    }
    
    @GetMapping("/")
    public String showUploadForm() {
        return "upload"; //upload.html
    }



    @PostMapping("/yolo/predict")
    public String predict(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        YoloResult result = yoloService.sendToYoloApi(file);
        model.addAttribute("results", result.getResults()); //리스트 전달
        model.addAttribute("imagePath", result.getImage()); //이미지 전달
        return "result";
    }
}
