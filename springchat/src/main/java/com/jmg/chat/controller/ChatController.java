package com.jmg.chat.controller;

import java.security.Principal;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import com.jmg.chat.domain.ChatMessage;
import com.jmg.chat.domain.SessionProfanity;
import com.jmg.chat.event.LoginEvent;
import com.jmg.chat.event.ParticipantRepository;
import com.jmg.chat.exception.TooMuchProfanityException;
import com.jmg.chat.util.ProfanityChecker;

/**
 * 
 * @author Julio Muñoz
 */
@Controller
public class ChatController {

	@Autowired private ProfanityChecker profanityFilter;
	@Autowired private SessionProfanity profanity;
	@Autowired private WebSocketMessageBrokerStats stats;
	@Autowired private ParticipantRepository participantRepository;
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	
	@SubscribeMapping("/chat.participants")
	public Collection<LoginEvent> retrieveParticipants() {
		return participantRepository.getActiveSessions().values();
	}
	
	@MessageMapping("/chat.message")
	public ChatMessage filterMessage(@Payload ChatMessage message, Principal principal) {
		checkProfanityAndSanitize(message);
		
		message.setUsername(principal.getName());
		
		return message;
	}
	
	@MessageMapping("/chat.private.{username}")
	public void filterPrivateMessage(@Payload ChatMessage message, @DestinationVariable("username") String username, Principal principal) {
		checkProfanityAndSanitize(message);
		
		message.setUsername(principal.getName());

		simpMessagingTemplate.convertAndSend("/user/" + username + "/queue/chat.message", message);
	}
	
	private void checkProfanityAndSanitize(ChatMessage message) {
		long profanityLevel = profanityFilter.getMessageProfanity(message.getMessage());
		profanity.increment(profanityLevel);
		message.setMessage(profanityFilter.filter(message.getMessage()));
	}
	
	@MessageExceptionHandler
	@SendToUser(value = "/queue/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}
	
	@RequestMapping("/stats")
	public @ResponseBody WebSocketMessageBrokerStats showStats() {
		return stats;
	}
}