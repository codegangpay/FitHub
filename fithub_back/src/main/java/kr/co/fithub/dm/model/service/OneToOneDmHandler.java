package kr.co.fithub.dm.model.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.fithub.dm.model.dao.DmDao;
import kr.co.fithub.dm.model.dto.DmDto;
import kr.co.fithub.dm.model.dto.DmMessage;

@Component
public class OneToOneDmHandler extends TextWebSocketHandler {
	@Autowired
	private DmService dmService;
	@Autowired
	private DmAlarmHandler alarmHandler;
    // 회원 번호 (memberNo)를 key로 세션을 value로 저장
    private Map<Integer, WebSocketSession> members = new HashMap<>();
    private Map<Integer, Integer> focusMap = new HashMap<>();
    private ObjectMapper om = new ObjectMapper();
    
    
    
    //주소에서 로그인멤버 No 맵으로 주는 함수
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2) {
                map.put(pair[0], pair[1]);
            }
        }
        return map;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    	
        String query = session.getUri().getQuery(); // "memberNo=123"
        Map<String, String> paramMap = parseQueryString(query);
        int memberNo = Integer.parseInt(paramMap.get("memberNo"));
        int receiverNo = Integer.parseInt(paramMap.get("receiverNo"));
        focusMap.put(memberNo, receiverNo);
        members.put(memberNo, session);
        dmService.changeIsRead(memberNo,receiverNo);
        WebSocketSession receiverSession = members.get(receiverNo);
        if (receiverSession != null) {
        	DmMessage msg = new DmMessage();
        	msg.setIsRead("isReadOk");
        	String data = om.writeValueAsString(msg);
            receiverSession.sendMessage(new TextMessage(data));
        }
        alarmHandler.sendReadYetCountTo(memberNo);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        DmDto dm = om.readValue(payload, DmDto.class);

        String type = dm.getType();


        if ("chat".equals(type)) {
        	HashMap<String, Integer> memberNoMap = new HashMap<>();
        	memberNoMap.put("memberNo1", Math.min(dm.getSenderNo(), dm.getReceiverNo()));
        	memberNoMap.put("memberNo2", Math.max(dm.getSenderNo(), dm.getReceiverNo()));
        	int existRoom = dmService.existRoom(memberNoMap);
        	int result = 0;
        	
        	int receiverNo = dm.getReceiverNo();
            WebSocketSession receiverSession = members.get(receiverNo);
            
            int senderNo = dm.getSenderNo();
            WebSocketSession senderSession = members.get(senderNo);
            
            
            if(existRoom == 0) {
            	result = dmService.createRoom(memberNoMap);
            }
            int isRead=0;
            if (receiverSession != null && focusMap.get(receiverNo) == senderNo) {
            	isRead=1;
            }
            int dmMessageNo = dmService.insertMessage(dm,isRead);
            alarmHandler.sendReadYetCountTo(receiverNo);
            
            
            
            
            
            DmMessage msg = dmService.selectOneMessage(dmMessageNo);
            if (receiverSession != null && focusMap.get(receiverNo) == senderNo) {
            	msg.setIsRead("Y");
            	dmService.changeIsRead(senderNo, receiverNo);
            }
            String data = om.writeValueAsString(msg);
            
            // 받는 사람에게만 메시지 전송
            if (receiverSession != null && focusMap.get(receiverNo) == senderNo) {
                receiverSession.sendMessage(new TextMessage(data));
            }
            
            if (senderSession != null) {
                senderSession.sendMessage(new TextMessage(data));
            }
            
            
            
            
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        
        Integer disconnectedMemberNo = null;
        for (Map.Entry<Integer, WebSocketSession> entry : members.entrySet()) {
            if (entry.getValue().equals(session)) {
                disconnectedMemberNo = entry.getKey();
                break;
            }
        }

        if (disconnectedMemberNo != null) {
            members.remove(disconnectedMemberNo);
            focusMap.remove(disconnectedMemberNo);

            // 나간 메시지 알림 (선택 사항)
            DmDto leaveMsg = new DmDto();
            leaveMsg.setType("out");
            leaveMsg.setSenderNo(disconnectedMemberNo);

            String data = om.writeValueAsString(leaveMsg);
            TextMessage sendMsg = new TextMessage(data);

            // 필요 시 다른 사용자에게 알릴 수 있음 (지금은 안 보냄)
        }
    }
}
