package com.enation.app.shop.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.enation.app.base.core.model.Member;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Socket处理器
 */
@Component
public class MyWebSocketHandler implements WebSocketHandler {
	//用于保存HttpSession与WebSocketSession的映射关系
	public static final Map<Long, WebSocketSession> userSocketSessionMap;
	
	static {
		userSocketSessionMap = new ConcurrentHashMap<Long, WebSocketSession>();
	}
	
	/**
	 * 建立连接后,把登录用户的id写入WebSocketSession
	 */
	public void afterConnectionEstablished(WebSocketSession session)
			throws Exception {
//		Object suid = session.getAttributes().get("uid");
//		if(suid!=null&&suid!=""){
			Long uid = (Long) session.getAttributes().get("uid");
			if (userSocketSessionMap.get(uid) == null) {
				userSocketSessionMap.put(uid, session);
				Message msg = new Message();
				msg.setFrom(0L);//0表示上线消息
				msg.setText(""+uid);
				this.broadcast(new TextMessage(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(msg)));
			}
//		}
	}

	/**
	 * 消息处理，在客户端通过Websocket API发送的消息会经过这里，然后进行相应的处理
	 */
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
			if(message.getPayloadLength()==0)
				return;
			Message msg=new Gson().fromJson(message.getPayload().toString(),Message.class);
			msg.setDate(new Date());
			sendMessageToUser(msg.getTo(), new TextMessage(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(msg)));
	}

	/**
	 * 消息传输错误处理
	 */
	public void handleTransportError(WebSocketSession session,
			Throwable exception) throws Exception {
		if (session.isOpen()) {
			session.close();
		}
		Iterator<Entry<Long, WebSocketSession>> it = userSocketSessionMap.entrySet().iterator();
		// 移除当前抛出异常用户的Socket会话
		while (it.hasNext()) {
			Entry<Long, WebSocketSession> entry = it.next();
			if (entry.getValue().getId().equals(session.getId())) {
				userSocketSessionMap.remove(entry.getKey());
				System.out.println("Socket会话已经移除:用户ID" + entry.getKey());
				String username="zzz";
				Message msg = new Message();
				msg.setFrom(-2L);
				msg.setText(username);
				this.broadcast(new TextMessage(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(msg)));
				break;
			}
		}
	}

	/**
	 * 关闭连接后
	 */
	public void afterConnectionClosed(WebSocketSession session,CloseStatus closeStatus) throws Exception {
		System.out.println("Websocket:" + session.getId() + "已经关闭");
		Iterator<Entry<Long, WebSocketSession>> it = userSocketSessionMap.entrySet().iterator();
		// 移除当前用户的Socket会话
		while (it.hasNext()) {
			Entry<Long, WebSocketSession> entry = it.next();
			if (entry.getValue().getId().equals(session.getId())) {
				userSocketSessionMap.remove(entry.getKey());
				System.out.println("Socket会话已经移除:用户ID" + entry.getKey());
				String username="zzz";
				Message msg = new Message();
				msg.setFrom(-2L);//下线消息，用-2表示
				msg.setText(username);
				this.broadcast(new TextMessage(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(msg)));
				break;
			}
		}
	}

	public boolean supportsPartialMessages() {
		return false;
	}

	/**
	 * 给所有在线用户发送消息
	 * @param message
	 * @throws IOException
	 */
	public void broadcast(final TextMessage message) throws IOException {
		Iterator<Entry<Long, WebSocketSession>> it = userSocketSessionMap.entrySet().iterator();

		//多线程群发
		while (it.hasNext()) {

			final Entry<Long, WebSocketSession> entry = it.next();

			if (entry.getValue().isOpen()) {
				// entry.getValue().sendMessage(message);
				new Thread(new Runnable() {

					public void run() {
						try {
							if (entry.getValue().isOpen()) {
								entry.getValue().sendMessage(message);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}).start();
			}

		}
	}

	/**
	 * 给某个用户发送消息
	 * 
	 * @param userName
	 * @param message
	 * @throws IOException
	 */
	public void sendMessageToUser(Long uid, TextMessage message) throws IOException {
		;
		WebSocketSession session;
		session = userSocketSessionMap.get(uid);
		Member member = UserConext.getCurrentMember();
		String tmpMemberid=(String) ThreadContextHolder.getSession().getAttribute(UserConext.CURRENT_TABLE_KEY);
		Integer memberid;
		if(tmpMemberid==null){
			memberid=member.getMember_id();
		}else{
			memberid=Integer.valueOf(tmpMemberid);
		}
		if (session != null && session.isOpen()) {
			Message msg = new Message();
			msg.setFrom(memberid.longValue());//0表示下单消息
			msg.setFromName(member.getName());
			msg.setText(message.getPayload());
			msg.setDate(new Date());
			session.sendMessage(new TextMessage(new GsonBuilder().setDateFormat("MM-dd HH:mm").create().toJson(msg)));
		}
	}

}
