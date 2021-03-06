package br.eti.arthurgregorio.library.domain.repositories;

import br.eti.arthurgregorio.library.application.components.table.Page;
import br.eti.arthurgregorio.library.domain.model.entities.PersistentEntity;
import br.eti.arthurgregorio.library.domain.model.entities.PersistentEntity_;
import java.util.List;
import java.util.Optional;
import javax.persistence.metamodel.SingularAttribute;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 * The default implementation of a repository in the application
 * 
 * Every repository should extend this class to get some features that are not
 * default in the basic Deltaspike implementation
 *
 * @param <T> the type of
 * 
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 1.0.0, 21/03/2018
 */
public interface DefaultRepository<T extends PersistentEntity> extends EntityRepository<T, Long>, CriteriaSupport<T> {

    /**
     * Generic method to find a entity by Id
     * 
     * @param id the id to search
     * @return the entity in a optional state
     */
    Optional<T> findOptionalById(Long id);
    
    /**
     * Generic search method with lazy pagination support
     * 
     * To use this method you need to implement {@link #getRestrictions(java.lang.String)}
     * and {@link #getBlockedProperty()}
     * 
     * @param filter the string filter to use
     * @param blocked the object status of the entity, null means all states
     * @param start the start page
     * @param pageSize the size of the page
     * @return the list of objects found
     */
    default Page<T> findAllBy(String filter, Boolean blocked, int start, int pageSize) {
        
        final int totalRows = this.countPages(filter, blocked);
        
        final Criteria<T, T> criteria = criteria();
        
        if (isNotBlank(filter)) {
            criteria.or(this.getRestrictions(filter));
        } 
        
        if (blocked != null) {
            criteria.eq(this.getBlockedProperty(), blocked);
        }
                
        this.setOrder(criteria);
        
        final List<T> data = criteria.createQuery()
                .setFirstResult(start)
                .setMaxResults(pageSize)
                .getResultList();
        
        return Page.of(data, totalRows);
    }

    /**
     * 
     * @param filter
     * @param blocked
     * @return 
     */
    default int countPages(String filter, Boolean blocked) {
        
        final Criteria<T, T> criteria = criteria()
                .or(this.getRestrictions(filter));
        
        if (blocked != null) {
            criteria.eq(this.getBlockedProperty(), blocked);
        }
        
        return criteria
                .select(Long.class, count(PersistentEntity_.id))
                .getSingleResult()
                .intValue();
    }
    
    /**
     * Generic method to find all unblocked entities
     * 
     * @return a list of all unblocked entities
     */
    default List<T> findAllUnblocked() {
        
        final Criteria<T, T> criteria = criteria()
                .eq(this.getBlockedProperty(), false);

        this.setOrder(criteria);
                
        return criteria.getResultList();
    }
    
    /**
     * Use this method to set the default order to all the queries using the 
     * default repository
     * 
     * @param criteria the criteria to be used
     */
    default void setOrder(Criteria<T, T> criteria) {
        criteria.orderAsc(PersistentEntity_.id);
    }

    /**
     * This method shoud be implemented if the user needs to use the generic 
     * type search with the {@link #findAllBy(java.lang.String, java.lang.Boolean, int, int)}
     * method
     * 
     * With this we can detect all the restrictions to build the criteria 
     * 
     * @param filter the generic filter in {@link String} format
     * @return the criteria for the type of the repository
     */
    default Criteria<T, T> getRestrictions(String filter) {
        throw new RuntimeException("getRestrictions not implemented for query");
    }
    
    /**
     * This method shoud be implemented if the user needs to use the generic 
     * type search with the {@link #findAllBy(java.lang.String, java.lang.Boolean, int, int)}
     * method
     * 
     * @return the attribute responsible for representing the blocked property
     * of the entity
     */
    default SingularAttribute<T, Boolean> getBlockedProperty() {
        throw new RuntimeException("getBlockProperty not implemented for query");
    }
}