package com.example.scannertable;

public class TemplateJSON {
    int n;
    int[] size;
    int[][] field;

    TemplateJSON(int n, int[] size, int[][] field) {
        this.n = n;
        this.size = size;
        this.field = field;
    }

    int getN() {
        return n;
    }
    int[] getSize(){
        return size;
    }

    int[][] getField(int n){
        return field;
    }

    int[] getFieldN(int n){
        return field[n];
    }
}
