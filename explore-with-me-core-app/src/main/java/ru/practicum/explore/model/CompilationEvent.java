package ru.practicum.explore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "compilations_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompilationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compilation_id", nullable = false)
    private Long compilationId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;
}
