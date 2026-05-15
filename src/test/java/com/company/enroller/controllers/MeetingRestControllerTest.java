package com.company.enroller.controllers;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import com.company.enroller.persistence.MeetingService;

@WebMvcTest(MeetingRestController.class)
public class MeetingRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MeetingService meetingService;

    private Meeting meetingA;
    private Meeting meetingB;
    private Participant participantA;
    private Participant participantB;

    @BeforeEach
    public void setUp() {
        meetingA = createMeeting(1L, "Meeting A");
        meetingB = createMeeting(2L, "Meeting B");
        participantA = createParticipant("userA");
        participantB = createParticipant("userB");
    }

    @Test
    public void getMeetings_withoutParams_returnsAllMeetings() throws Exception {
        Collection<Meeting> allMeetings = singletonList(meetingA);
        given(meetingService.getAll(null, null, null)).willReturn(allMeetings);

        mvc.perform(get("/meetings").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].title", is(meetingA.getTitle())));
    }

    @Test
    public void getMeetings_withTitleSortAsc_returnsMeetingsSortedAscending() throws Exception {
        Collection<Meeting> sortedMeetings = asList(meetingA, meetingB);
        given(meetingService.getAll("title", "ASC", null)).willReturn(sortedMeetings);

        mvc.perform(get("/meetings?sortBy=title&sortOrder=ASC").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Meeting A"))).andExpect(jsonPath("$[1].title", is("Meeting B")));
    }

    @Test
    public void getMeetings_withTitleSortDesc_returnsMeetingsSortedDescending() throws Exception {
        Collection<Meeting> sortedMeetings = asList(meetingB, meetingA);
        given(meetingService.getAll("title", "DESC", null)).willReturn(sortedMeetings);

        mvc.perform(get("/meetings?sortBy=title&sortOrder=DESC").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Meeting B"))).andExpect(jsonPath("$[1].title", is("Meeting A")));
    }

    @Test
    public void getMeetings_withTitleSortWithoutSortOrder_returnsMeetingsSortedAscendingByDefault() throws Exception {
        Collection<Meeting> sortedMeetings = asList(meetingA, meetingB);
        given(meetingService.getAll("title", null, null)).willReturn(sortedMeetings);

        mvc.perform(get("/meetings?sortBy=title").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].title", is("Meeting A")))
                .andExpect(jsonPath("$[1].title", is("Meeting B")));
    }

    @Test
    public void getMeetings_withInvalidSortOrder_returnsMeetingsSortedAscendingByDefault() throws Exception {
        Collection<Meeting> sortedMeetings = asList(meetingA, meetingB);
        given(meetingService.getAll("title", "INVALID", null)).willReturn(sortedMeetings);

        mvc.perform(get("/meetings?sortBy=title&sortOrder=INVALID").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Meeting A"))).andExpect(jsonPath("$[1].title", is("Meeting B")));
    }

    @Test
    public void getMeetings_withTitleKey_returnsMeetingsFilteredByTitle() throws Exception {
        Collection<Meeting> filteredMeetings = asList(meetingA, meetingB);
        given(meetingService.getAll(null, null, "Meeting")).willReturn(filteredMeetings);

        mvc.perform(get("/meetings?key=Meeting").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].title", is("Meeting A")))
                .andExpect(jsonPath("$[1].title", is("Meeting B")));
    }

    @Test
    public void getMeetingParticipants_withExistingMeeting_returnsRegisteredParticipants() throws Exception {
        Collection<Participant> participants = asList(participantA, participantB);
        given(meetingService.getParticipants(1L)).willReturn(participants);

        mvc.perform(get("/meetings/1/participants").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].login", is("userA")))
                .andExpect(jsonPath("$[1].login", is("userB")));
    }

    @Test
    public void getMeetingParticipants_withMissingMeeting_returnsNotFound() throws Exception {
        given(meetingService.getParticipants(99L)).willReturn(null);

        mvc.perform(get("/meetings/99/participants").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addParticipantToMeeting_withExistingMeetingAndParticipant_returnsNoContent() throws Exception {
        given(meetingService.addParticipant(1L, "userA")).willReturn(true);

        mvc.perform(post("/meetings/1/participants").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"userA\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void addParticipantToMeeting_withMissingMeetingOrParticipant_returnsNotFound() throws Exception {
        given(meetingService.addParticipant(99L, "userA")).willReturn(false);

        mvc.perform(post("/meetings/99/participants").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"userA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeParticipantFromMeeting_withExistingMeetingAndParticipant_returnsNoContent() throws Exception {
        given(meetingService.removeParticipant(1L, "userA")).willReturn(true);

        mvc.perform(delete("/meetings/1/participants/userA").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void removeParticipantFromMeeting_withMissingMeetingOrParticipant_returnsNotFound() throws Exception {
        given(meetingService.removeParticipant(99L, "userA")).willReturn(false);

        mvc.perform(delete("/meetings/99/participants/userA").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private Meeting createMeeting(Long id, String title) {
        Meeting meeting = new Meeting();
        meeting.setId(id);
        meeting.setTitle(title);
        meeting.setDescription("sample description");
        meeting.setDate("sample date");
        return meeting;
    }

    private Participant createParticipant(String login) {
        Participant participant = new Participant();
        participant.setLogin(login);
        participant.setPassword("testpassword");
        return participant;
    }

}
