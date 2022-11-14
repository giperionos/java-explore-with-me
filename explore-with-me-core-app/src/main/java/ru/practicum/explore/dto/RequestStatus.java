package ru.practicum.explore.dto;

public enum RequestStatus {
    PENDING, //Ожидает модерации
    CONFIRMED, //Подтвержден
    CANCELED, //Завершен
    REJECTED //Отклонен
}
