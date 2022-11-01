package ru.practicum.explore.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.explore.dto.EventState;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @CreationTimestamp
    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(name = "latitude")
    private Float lat;

    @Column(name = "longitude")
    private Float lon;

    @Column
    private boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "request_moderation")
    private boolean requestModeration;

    @Column
    @Enumerated(EnumType.STRING)
    private EventState state;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;
}
