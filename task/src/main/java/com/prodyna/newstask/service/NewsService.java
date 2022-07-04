package com.prodyna.newstask.service;

import com.prodyna.newstask.service.dto.NewsDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.prodyna.newstask.domain.News}.
 */
public interface NewsService {
    /**
     * Save a news.
     *
     * @param newsDTO the entity to save.
     * @return the persisted entity.
     */
    NewsDTO save(NewsDTO newsDTO);

    /**
     * Partially updates a news.
     *
     * @param newsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<NewsDTO> partialUpdate(NewsDTO newsDTO);

    /**
     * Get all the news.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<NewsDTO> findAll(Pageable pageable);

    /**
     * Get all the news with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<NewsDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" news.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<NewsDTO> findOne(Long id);

    /**
     * Delete the "id" news.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
