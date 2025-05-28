package com.cookandroid.food_check;

import java.util.List;

public class YoloResponse {
    private String status;
    private List<Result> results;
    private String image;

    public static class Result {
        private String label;
        private Integer calories;
        public String getLabel() { return label; }
        public Integer getCalories() { return calories; }
    }

    public String getStatus() { return status; }
    public List<Result> getResults() { return results; }
    public String getImage() { return image; }
}
