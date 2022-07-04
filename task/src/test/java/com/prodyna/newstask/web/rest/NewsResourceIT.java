package com.prodyna.newstask.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.prodyna.newstask.IntegrationTest;
import com.prodyna.newstask.domain.News;
import com.prodyna.newstask.repository.NewsRepository;
import com.prodyna.newstask.service.dto.NewsDTO;
import com.prodyna.newstask.service.mapper.NewsMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link NewsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class NewsResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/news";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restNewsMockMvc;

    private News news;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static News createEntity(EntityManager em) {
        News news = new News()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .category(DEFAULT_CATEGORY)
            .createdDate(DEFAULT_CREATED_DATE);
        return news;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static News createUpdatedEntity(EntityManager em) {
        News news = new News()
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .category(UPDATED_CATEGORY)
            .createdDate(UPDATED_CREATED_DATE);
        return news;
    }

    @BeforeEach
    public void initTest() {
        news = createEntity(em);
    }

    @Test
    @Transactional
    void createNews() throws Exception {
        int databaseSizeBeforeCreate = newsRepository.findAll().size();
        // Create the News
        NewsDTO newsDTO = newsMapper.toDto(news);
        restNewsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(newsDTO)))
            .andExpect(status().isCreated());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeCreate + 1);
        News testNews = newsList.get(newsList.size() - 1);
        assertThat(testNews.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testNews.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testNews.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testNews.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
    }

    @Test
    @Transactional
    void createNewsWithExistingId() throws Exception {
        // Create the News with an existing ID
        news.setId(1L);
        NewsDTO newsDTO = newsMapper.toDto(news);

        int databaseSizeBeforeCreate = newsRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restNewsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(newsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllNews() throws Exception {
        // Initialize the database
        newsRepository.saveAndFlush(news);

        // Get all the newsList
        restNewsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(news.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));
    }

    @Test
    @Transactional
    void getNews() throws Exception {
        // Initialize the database
        newsRepository.saveAndFlush(news);

        // Get the news
        restNewsMockMvc
            .perform(get(ENTITY_API_URL_ID, news.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(news.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingNews() throws Exception {
        // Get the news
        restNewsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewNews() throws Exception {
        // Initialize the database
        newsRepository.saveAndFlush(news);

        int databaseSizeBeforeUpdate = newsRepository.findAll().size();

        // Update the news
        News updatedNews = newsRepository.findById(news.getId()).get();
        // Disconnect from session so that the updates on updatedNews are not directly saved in db
        em.detach(updatedNews);
        updatedNews.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).category(UPDATED_CATEGORY).createdDate(UPDATED_CREATED_DATE);
        NewsDTO newsDTO = newsMapper.toDto(updatedNews);

        restNewsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, newsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(newsDTO))
            )
            .andExpect(status().isOk());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeUpdate);
        News testNews = newsList.get(newsList.size() - 1);
        assertThat(testNews.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testNews.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testNews.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testNews.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void putNonExistingNews() throws Exception {
        int databaseSizeBeforeUpdate = newsRepository.findAll().size();
        news.setId(count.incrementAndGet());

        // Create the News
        NewsDTO newsDTO = newsMapper.toDto(news);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNewsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, newsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(newsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchNews() throws Exception {
        int databaseSizeBeforeUpdate = newsRepository.findAll().size();
        news.setId(count.incrementAndGet());

        // Create the News
        NewsDTO newsDTO = newsMapper.toDto(news);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNewsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(newsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamNews() throws Exception {
        int databaseSizeBeforeUpdate = newsRepository.findAll().size();
        news.setId(count.incrementAndGet());

        // Create the News
        NewsDTO newsDTO = newsMapper.toDto(news);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNewsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(newsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateNewsWithPatch() throws Exception {
        // Initialize the database
        newsRepository.saveAndFlush(news);

        int databaseSizeBeforeUpdate = newsRepository.findAll().size();

        // Update the news using partial update
        News partialUpdatedNews = new News();
        partialUpdatedNews.setId(news.getId());

        partialUpdatedNews.name(UPDATED_NAME).category(UPDATED_CATEGORY).createdDate(UPDATED_CREATED_DATE);

        restNewsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNews.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedNews))
            )
            .andExpect(status().isOk());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeUpdate);
        News testNews = newsList.get(newsList.size() - 1);
        assertThat(testNews.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testNews.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testNews.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testNews.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void fullUpdateNewsWithPatch() throws Exception {
        // Initialize the database
        newsRepository.saveAndFlush(news);

        int databaseSizeBeforeUpdate = newsRepository.findAll().size();

        // Update the news using partial update
        News partialUpdatedNews = new News();
        partialUpdatedNews.setId(news.getId());

        partialUpdatedNews.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).category(UPDATED_CATEGORY).createdDate(UPDATED_CREATED_DATE);

        restNewsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNews.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedNews))
            )
            .andExpect(status().isOk());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeUpdate);
        News testNews = newsList.get(newsList.size() - 1);
        assertThat(testNews.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testNews.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testNews.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testNews.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingNews() throws Exception {
        int databaseSizeBeforeUpdate = newsRepository.findAll().size();
        news.setId(count.incrementAndGet());

        // Create the News
        NewsDTO newsDTO = newsMapper.toDto(news);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNewsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, newsDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(newsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchNews() throws Exception {
        int databaseSizeBeforeUpdate = newsRepository.findAll().size();
        news.setId(count.incrementAndGet());

        // Create the News
        NewsDTO newsDTO = newsMapper.toDto(news);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNewsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(newsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamNews() throws Exception {
        int databaseSizeBeforeUpdate = newsRepository.findAll().size();
        news.setId(count.incrementAndGet());

        // Create the News
        NewsDTO newsDTO = newsMapper.toDto(news);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNewsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(newsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the News in the database
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteNews() throws Exception {
        // Initialize the database
        newsRepository.saveAndFlush(news);

        int databaseSizeBeforeDelete = newsRepository.findAll().size();

        // Delete the news
        restNewsMockMvc
            .perform(delete(ENTITY_API_URL_ID, news.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<News> newsList = newsRepository.findAll();
        assertThat(newsList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
