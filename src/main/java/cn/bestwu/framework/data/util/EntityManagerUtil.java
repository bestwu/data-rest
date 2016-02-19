package cn.bestwu.framework.data.util;

import org.hibernate.Session;
import org.hibernate.search.exception.SearchException;

import javax.persistence.EntityManager;

/**
 * @author Peter Wu
 */
public class EntityManagerUtil {

	public static Session getSession(EntityManager entityManager) {
		Object delegate = entityManager.getDelegate();
		if (delegate == null) {
			throw new SearchException(
					"Trying to use Hibernate Search without an Hibernate EntityManager (no delegate)"
			);
		} else if (Session.class.isAssignableFrom(delegate.getClass())) {
			return (Session) delegate;
		} else if (EntityManager.class.isAssignableFrom(delegate.getClass())) {
			//Some app servers wrap the EM twice
			delegate = ((EntityManager) delegate).getDelegate();
			if (delegate == null) {
				throw new SearchException(
						"Trying to use Hibernate Search without an Hibernate EntityManager (no delegate)"
				);
			} else if (Session.class.isAssignableFrom(delegate.getClass())) {
				return (Session) delegate;
			} else {
				throw new SearchException(
						"Trying to use Hibernate Search without an Hibernate EntityManager: " + delegate.getClass()
				);
			}
		} else {
			throw new SearchException(
					"Trying to use Hibernate Search without an Hibernate EntityManager: " + delegate.getClass()
			);
		}
	}
}
