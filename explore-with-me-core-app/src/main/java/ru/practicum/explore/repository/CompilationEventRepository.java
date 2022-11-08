package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.explore.model.CompilationEvent;

public interface CompilationEventRepository extends JpaRepository<CompilationEvent, Long>, QuerydslPredicateExecutor<CompilationEvent> {
}
