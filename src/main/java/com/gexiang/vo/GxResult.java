package com.gexiang.vo;

public class GxResult <T,U> {
    private T data;
    private U result;
    private int status;

    public GxResult(T data, U result, int status) {
        this.data = data;
        this.result = result;
        this.status = status;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
