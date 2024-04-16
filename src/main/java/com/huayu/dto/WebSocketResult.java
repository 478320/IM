package com.huayu.dto;

import cn.hutool.json.JSONUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket数据返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketResult {

    private String type;
    private Object payload;
    private Status status;
    private String timestamp;

    public static TextWebSocketFrame ok(String type,Object payload,String timestamp){
        return new TextWebSocketFrame(JSONUtil.toJsonStr(new WebSocketResult(type,payload,new Status(200,"ok"),timestamp)));
    }

    public static TextWebSocketFrame fail(String type,Object payload,String timestamp){
        return new TextWebSocketFrame(JSONUtil.toJsonStr(new WebSocketResult(type,payload,new Status(500,"fail"),timestamp)));
    }
}
