package com.gexiang.Util;

import com.gexiang.constant.ConstValue;
import com.gexiang.repository.entity.TimerReq;
import com.gexiang.vo.GxTimerRequest;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Helper {
    private final static char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
    private static Random randGen = new Random();
    public static String randomString(int length) {
        if (length < 1) {
            return null;
        }
        // Create a char buffer to put random letters and numbers in.
        char [] randBuffer = new char[length];
        for (int i=0; i<randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(numbersAndLetters.length - 1)];
        }
        return new String(randBuffer);
    }

    public static boolean isStrLenEqual(String str, int len){
        if(str == null){
            return false;
        }else{
            return (str.length() == len);
        }
    }

    public static boolean isStrLenLess(String str, int len){
        if(str == null){
            return false;
        }else{
            return (str.length() <= len);
        }
    }

    public static boolean isStrLenBigger(String str, int len){
        return ((str != null) && (str.length() > len));
    }

    public static boolean isValueRange(int src, int min, int max){
        return ((src >= min) && (src <= max));
    }


    public static void sleepMills(long millse){
        Uninterruptibles.sleepUninterruptibly(millse, TimeUnit.MILLISECONDS);
    }

    public static TimerReq transFromRequest(GxTimerRequest request){
        TimerReq timerReq = new TimerReq();
        timerReq.setAppId(request.getAppId());
        timerReq.setJobId(request.getJobId());
        timerReq.setReqUrl(request.getReqUrl());
        timerReq.setCbUrl(request.getCbUrl());
        timerReq.setDataType(request.getDataType());
        timerReq.setReqType(request.getReqType());
        timerReq.setReqBody(request.getReqBody());
        timerReq.setReqHeader(request.getReqHeader());
        timerReq.setTimes(0);
        timerReq.setStatus(Integer.valueOf(0));
        if(timerReq.getReqBody() == null){
            timerReq.setReqBody(ConstValue.DATA_EMPTY);
        }
        if(timerReq.getReqHeader() == null){
            timerReq.setReqHeader(ConstValue.DATA_EMPTY);
        }
        if(timerReq.getCbUrl() == null){
            timerReq.setCbUrl(ConstValue.DATA_EMPTY);
        }
        return timerReq;
    }
}
