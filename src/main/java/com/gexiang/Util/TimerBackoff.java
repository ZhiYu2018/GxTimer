package com.gexiang.Util;
public class TimerBackoff {
    private static final long MAX_INTERVAL = 10*6000*1000L;
    private static final double DEFAULT_MULTIPLIER = 1.5D;
    private static double	DEFAULT_RANDOMIZATION_FACTOR = 0.5D;
    public static final TimerBackoff defaultBackOff = new TimerBackoff(1000, MAX_INTERVAL);
    private long interval;
    private long maxInterval;
    private double randomization_factor;
    private double multiplier;


    public TimerBackoff(long interval, long maxInterval){
        this.interval = interval;
        this.maxInterval = maxInterval;
        randomization_factor = DEFAULT_RANDOMIZATION_FACTOR;
        multiplier = DEFAULT_MULTIPLIER;
    }

    public long nextBackOffSecond(int times){
        long mills = nextBackOffMillis(times);
        long second = mills/1000;
        return ((second == 0)? 1:second);
    }

    public long	nextBackOffMillis(int times){
        long retry_interval = calInterval(times);
        if(retry_interval >= maxInterval) {
            retry_interval = maxInterval;
        }
        double min = 1 - randomization_factor;
        double max = 1 + randomization_factor;
        double rf  = min + Math.random()*(max - min);
        long  randomized_interval = (long)(retry_interval * rf);
        return randomized_interval;
    }

    private long calInterval(int times){
        long retry_interval = interval;
        for(int t = 0; t < times; t++){
            retry_interval = (long)(retry_interval * multiplier);
        }
        return retry_interval;
    }
}
