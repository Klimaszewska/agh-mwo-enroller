package com.company.enroller.persistence;

import com.company.enroller.model.Meeting;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;

@Component("meetingService")
public class MeetingService {

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
}
