package com.example.foodfight.yolotest;

import java.util.List;

public class YoloResult {
    private String status;
    private String image;
    
    private List<Prediction> results;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public List<Prediction> getResults() {
        return results;
    }
    public void setResults(List<Prediction> results) {
        this.results = results;
    }

    public static class Prediction {
        private String label;
        private Integer calories;

        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }

        public Integer getCalories() {
            return calories;
        }
        public void setCalories(Integer calories) {
            this.calories = calories;
        }
    }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
