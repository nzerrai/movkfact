package com.movkfact.repository;

import com.movkfact.entity.BankingLexiconEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankingLexiconRepository extends JpaRepository<BankingLexiconEntry, Long> {
    List<BankingLexiconEntry> findAllByOrderByLexiconGroupAscLabelAsc();
}
