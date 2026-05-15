package com.company.enroller.persistence;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;

@Component("meetingService")
public class MeetingService {

    @Autowired
    ParticipantService participantService;

    DatabaseConnector connector;

    public MeetingService() {
        connector = DatabaseConnector.getInstance();
    }

    public Collection<Meeting> getAll(String sortBy, String sortOrder, String key) {
        StringBuilder hql = new StringBuilder("FROM Meeting m");

        boolean hasKey = key != null && !key.trim().isEmpty();
        if (hasKey) {
            hql.append(" WHERE LOWER(m.title) LIKE :key");
        }

        if ("title".equalsIgnoreCase(sortBy)) {
            String order = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
            hql.append(" ORDER BY m.title ").append(order);
        }

        Query<Meeting> query = connector.getSession().createQuery(hql.toString(), Meeting.class);
        if (hasKey) {
            query.setParameter("key", "%" + key.toLowerCase(Locale.ROOT) + "%");
        }
        return query.list();
    }

    public Meeting findById(Long id) {
        return connector.getSession().get(Meeting.class, id);
    }

    public void save(Meeting meeting) {
        Transaction transaction = connector.getSession().beginTransaction();
        try {
            connector.getSession().save(meeting);
            transaction.commit();
        } catch (RuntimeException e) {
            transaction.rollback();
            throw e;
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = connector.getSession().beginTransaction();
        Meeting retrievedMeeting = findById(id);
        if (retrievedMeeting != null) {
            connector.getSession().delete(retrievedMeeting);
        }
        transaction.commit();
    }

    public void update(Meeting meeting) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().update(meeting);
        transaction.commit();
    }

    public boolean addParticipant(Long meetingId, String login) {
        Transaction transaction = connector.getSession().beginTransaction();
        try {
            Meeting retrievedMeeting = findById(meetingId);
            Participant retrievedParticipant = participantService.findByLogin(login);
            if (retrievedMeeting == null || retrievedParticipant == null) {
                transaction.rollback();
                return false;
            }
            retrievedMeeting.addParticipant(retrievedParticipant);
            transaction.commit();
            return true;
        } catch (RuntimeException e) {
            transaction.rollback();
            throw e;
        }
    }

    public Collection<Participant> getParticipants(Long meetingId) {
        Meeting retrievedMeeting = findById(meetingId);
        if (retrievedMeeting == null) {
            return null;
        }
        return retrievedMeeting.getParticipants();
    }

    public boolean removeParticipant(Long meetingId, String login) {
        Transaction transaction = connector.getSession().beginTransaction();
        try {
            Meeting retrievedMeeting = findById(meetingId);
            Participant retrievedParticipant = participantService.findByLogin(login);
            if (retrievedMeeting == null || retrievedParticipant == null) {
                transaction.rollback();
                return false;
            }
            retrievedMeeting.removeParticipant(retrievedParticipant);
            transaction.commit();
            return true;
        } catch (RuntimeException e) {
            transaction.rollback();
            throw e;
        }
    }
}
