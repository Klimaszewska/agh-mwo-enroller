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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.company.enroller.model.Meeting;
import com.company.enroller.persistence.MeetingService;

@WebMvcTest(MeetingRestController.class)
public class MeetingRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MeetingService meetingService;

    private Meeting meetingA;
    private Meeting meetingB;

    @BeforeEach
    public void setUp() {
        meetingA = createMeeting(1L, "Meeting A");
        meetingB = createMeeting(2L, "Meeting B");
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

    private Meeting createMeeting(Long id, String title) {
        Meeting meeting = new Meeting();
        meeting.setId(id);
        meeting.setTitle(title);
        meeting.setDescription("sample description");
        meeting.setDate("sample date");
        return meeting;
    }

}
