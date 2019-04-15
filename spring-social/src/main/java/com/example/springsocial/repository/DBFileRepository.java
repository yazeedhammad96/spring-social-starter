package com.example.springsocial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springsocial.model.DBFile;

@Repository
public interface DBFileRepository extends JpaRepository<DBFile, String> {

}