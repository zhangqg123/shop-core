
package com.enation.app.shop.websocket;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.enation.app.base.core.model.Member;
import com.enation.eop.sdk.context.UserConext;


/**
 * Socket建立连接（握手）和断开
 */
public class HandShake implements HandshakeInterceptor {

	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		System.out.println("Websocket:用户[ID:" + ((ServletServerHttpRequest) request).getServletRequest().getSession(false).getAttribute("uid") + "]已经建立连接");
		if (request instanceof ServletServerHttpRequest) {
			Member member = UserConext.getCurrentMember();
			// 标记用户
//			String uid = 123L;
			if(member!=null){
				attributes.put("uid", member.getMember_id().longValue());
				attributes.put("name", member.getName());
			}else{
				return false;
			}
		}
		return true;
	}

	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
		System.out.println("after hand");
	}

}
