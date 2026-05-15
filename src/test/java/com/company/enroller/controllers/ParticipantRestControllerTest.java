package com.company.enroller.controllers;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.company.enroller.model.Participant;
import com.company.enroller.persistence.MeetingService;
import com.company.enroller.persistence.ParticipantService;

@WebMvcTest(ParticipantRestController.class)
public class ParticipantRestControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private MeetingService meetingService;

	@MockBean
	private ParticipantService participantService;

	@Test
	public void getParticipants_withoutParams_returnsAllParticipants() throws Exception {
		Participant participant = createParticipant("testlogin");
		Collection<Participant> allParticipants = singletonList(participant);
		given(participantService.getAll(null, null, null)).willReturn(allParticipants);

		mvc.perform(get("/participants").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].login", is(participant.getLogin())));
	}

	@Test
	public void getParticipants_withLoginSortAsc_returnsParticipantsSortedAscending() throws Exception {
		Collection<Participant> sortedParticipants = asList(createParticipant("user2"), createParticipant("user9"));
		given(participantService.getAll("login", "ASC", null)).willReturn(sortedParticipants);

		mvc.perform(get("/participants?sortBy=login&sortOrder=ASC").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].login", is("user2"))).andExpect(jsonPath("$[1].login", is("user9")));
	}

	@Test
	public void getParticipants_withLoginSortDesc_returnsParticipantsSortedDescending() throws Exception {
		Collection<Participant> sortedParticipants = asList(createParticipant("user9"), createParticipant("user2"));
		given(participantService.getAll("login", "DESC", null)).willReturn(sortedParticipants);

		mvc.perform(get("/participants?sortBy=login&sortOrder=DESC").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].login", is("user9"))).andExpect(jsonPath("$[1].login", is("user2")));
	}

	@Test
	public void getParticipants_withLoginSortWithoutSortOrder_returnsParticipantsSortedAscendingByDefault() throws Exception {
		Collection<Participant> sortedParticipants = asList(createParticipant("user2"), createParticipant("user9"));
		given(participantService.getAll("login", null, null)).willReturn(sortedParticipants);

		mvc.perform(get("/participants?sortBy=login").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].login", is("user2")))
				.andExpect(jsonPath("$[1].login", is("user9")));
	}

	@Test
	public void getParticipants_withInvalidSortOrder_returnsParticipantsSortedAscendingByDefault() throws Exception {
		Collection<Participant> sortedParticipants = asList(createParticipant("user2"), createParticipant("user9"));
		given(participantService.getAll("login", "INVALID", null)).willReturn(sortedParticipants);

		mvc.perform(get("/participants?sortBy=login&sortOrder=INVALID").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].login", is("user2"))).andExpect(jsonPath("$[1].login", is("user9")));
	}

	@Test
	public void getParticipants_withLoginKey_returnsParticipantsFilteredByLogin() throws Exception {
		Collection<Participant> filteredParticipants = asList(createParticipant("login1"), createParticipant("mylogin"));
		given(participantService.getAll(null, null, "login")).willReturn(filteredParticipants);

		mvc.perform(get("/participants?key=login").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].login", is("login1")))
				.andExpect(jsonPath("$[1].login", is("mylogin")));
	}

	private Participant createParticipant(String login) {
		Participant participant = new Participant();
		participant.setLogin(login);
		participant.setPassword("testpassword");
		return participant;
	}

}
