package com.company.enroller.persistence;

import com.company.enroller.model.Meeting;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("meetingService")
public class MeetingService {

    DatabaseConnector connector;

    public MeetingService() {
        connector = DatabaseConnector.getInstance();
    }

    public Collection<Meeting> getAll() {
        String hql = "FROM Meeting";
        Query<Meeting> query = connector.getSession().createQuery(hql, Meeting.class);
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
}
