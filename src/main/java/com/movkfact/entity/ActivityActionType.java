package com.movkfact.entity;

/**
 * Types d'actions pour le tracking d'activité sur les datasets.
 */
public enum ActivityActionType {
    DOWNLOADED,
    MODIFIED,
    VIEWED,
    CREATED,
    RESET,
    DELETED,
    ROW_MODIFIED,
    ROW_DELETED
}