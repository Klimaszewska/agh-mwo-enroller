package com.company.enroller.persistence;

import java.util.Collection;
import java.util.Locale;

import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import com.company.enroller.model.Participant;

@Component("participantService")
public class ParticipantService {

    DatabaseConnector connector;

    public ParticipantService() {
        connector = DatabaseConnector.getInstance();
    }

    public Collection<Participant> getAll(String sortBy, String sortOrder, String key) {
        StringBuilder hql = new StringBuilder("FROM Participant p");
        boolean hasKey = key != null && !key.trim().isEmpty();

        if (hasKey) {
            hql.append(" WHERE LOWER(p.login) LIKE :key");
        }
        if ("login".equalsIgnoreCase(sortBy)) {
            String order = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
            hql.append(" ORDER BY p.login ").append(order);
        }

        Query<Participant> query = connector.getSession().createQuery(hql.toString(), Participant.class);
        if (hasKey) {
            query.setParameter("key", "%" + key.toLowerCase(Locale.ROOT) + "%");
        }
        return query.list();
    }

    public Participant findByLogin(String login) {
        return connector.getSession().get(Participant.class, login);
    }

    public void save(Participant participant) {
        Transaction transaction = connector.getSession().beginTransaction();
        try {
            connector.getSession().save(participant);
            transaction.commit();
        } catch (RuntimeException e) {
            transaction.rollback();
            throw e;
        }
    }

    public void deleteById(String login) {
        Transaction transaction = connector.getSession().beginTransaction();
        Participant retrievedParticipant = findByLogin(login);
        if (retrievedParticipant != null) {
            connector.getSession().delete(retrievedParticipant);
        }
        transaction.commit();
    }

    public void update(Participant participant) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().update(participant);
        transaction.commit();
    }
}
