package com.rms.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rms.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}