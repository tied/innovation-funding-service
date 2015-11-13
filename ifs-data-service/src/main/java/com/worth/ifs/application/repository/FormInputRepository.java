package com.worth.ifs.application.repository;

import com.worth.ifs.form.domain.FormInput;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface FormInputRepository extends PagingAndSortingRepository<FormInput, Long> {
    List<FormInput> findAll();
    List<FormInput> findByCompetitionId(@Param("competitionId") Long competitionId);
}