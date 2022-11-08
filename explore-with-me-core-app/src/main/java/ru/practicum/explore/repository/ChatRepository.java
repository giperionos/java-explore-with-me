package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.explore.model.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long>, QuerydslPredicateExecutor<Chat> {
}
