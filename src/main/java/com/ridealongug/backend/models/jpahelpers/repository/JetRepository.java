package com.ridealongug.backend.models.jpahelpers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface JetRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
    void refresh(T t);
}
