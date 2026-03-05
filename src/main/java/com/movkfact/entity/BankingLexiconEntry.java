package com.movkfact.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "banking_lexicon")
public class BankingLexiconEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "suggested_type", nullable = false, length = 50)
    private String suggestedType;

    @Column(name = "lexicon_group", nullable = false, length = 100)
    private String lexiconGroup;

    public Long getId() { return id; }
    public String getLabel() { return label; }
    public String getSuggestedType() { return suggestedType; }
    public String getLexiconGroup() { return lexiconGroup; }
}
