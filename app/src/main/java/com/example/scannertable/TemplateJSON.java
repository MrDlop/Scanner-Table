package com.example.scannertable;

public class TemplateJSON {
    private int n;
    private int[] size;
    private int[][] field;
    private String[] field_name;

    TemplateJSON(int n, int[] size, int[][] field, String[] field_name) {
        this.n = n;
        this.size = size;
        this.field = field;
        this.field_name = field_name;
    }

    int getN() {
        return n;
    }
    int[] getSize(){
        return size;
    }

    int[] getFieldN(int n){
        return field[n];
    }

    String getFieldName(int n){
        return field_name[n];
    }
}
