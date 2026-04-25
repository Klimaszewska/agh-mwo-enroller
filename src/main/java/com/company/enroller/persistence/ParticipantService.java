package com.company.enroller.persistence;

import java.util.Collection;

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

	public Collection<Participant> getAll() {
		String hql = "FROM Participant";
		Query query = connector.getSession().createQuery(hql);
		return query.list();
	}

	public Participant findByLogin(String login) {
		return connector.getSession().get(Participant.class, login);
	}

    public void save(Participant participant) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().saveOrUpdate(participant);
		transaction.commit();
    }

    public void deleteById(String login) {
		Transaction transaction = connector.getSession().beginTransaction();
		Participant retrievedParticipant = findByLogin(login);
		if (retrievedParticipant != null) {
			connector.getSession().delete(retrievedParticipant);
		}
		transaction.commit();
    }
}
