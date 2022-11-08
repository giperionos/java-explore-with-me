package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.explore.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long>, QuerydslPredicateExecutor<Message> {
}
