package com.gexiang.vo;

public class GxResult <T,U> {
    private T data;
    private U result;

    public GxResult(T data, U result) {
        this.data = data;
        this.result = result;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public U getResult() {
        return result;
    }

    public void setResult(U result) {
        this.result = result;
    }
}
