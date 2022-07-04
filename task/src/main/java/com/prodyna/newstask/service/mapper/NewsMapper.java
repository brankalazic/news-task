package com.prodyna.newstask.service.mapper;

import com.prodyna.newstask.domain.News;
import com.prodyna.newstask.service.dto.NewsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link News} and its DTO {@link NewsDTO}.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface NewsMapper extends EntityMapper<NewsDTO, News> {
    @Mapping(target = "author", source = "author", qualifiedByName = "login")
    NewsDTO toDto(News s);
}
