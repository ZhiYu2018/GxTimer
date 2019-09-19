# GxTimer
1、定时回调系统 
## 设计意图
* 尝试型、定时调用第三方接口。
* 实时、保障型通知第三方。
## 使用方法

* 提交定时任务，通过调用 http://url/timer/submit/job
* 提交实时保障型通知 http://url/timer/submit/notify
* 终态(成功，最终失败) 回调给使用方 http://url/callback，URL 是使用方的。

## 请求内容

* 定时补偿任务、实时通知的请求格式是application/json

```java
class GxTimerRequest{
    String appId;
    String jobId;
    int reqType;
    int dataType;
    long timeUsed; /**需要尝试的时长，单位秒，0 采用系统默认的时长**/
    String reqUrl;
    String cbUrl;
    String reqBody;
    String reqHeader;
}
```

响应的内容格式，也是application/json

```java
class GxTimerResponse{
    String appId;
    String jobId;
    int    status; /**200 成功，其他的失败**/
    String msg;    /**失败原因**/
}
```

* 回调给调用方的请求也是application/json

```java
class GxTimeCbRequest{
    String appId;
    String jobId;
    int    status; /**200 成功，其他的失败**/
    String msg;    /**失败原因**/
    String body;   /**第三方返回的内容**/
}
```

## 关键设计

* 可靠尝试 采用指数退避算法，避免请求风暴。
* 采用异步方式，单节点执行，双节点冗余。
* 利用时间增量，进行任务轮询，定时任务执行时间间隔是1秒。