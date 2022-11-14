package ru.practicum.explore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EndpointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "app_name")
    private String app;

    @Column(name = "request_uri")
    private String uri;

    @Column(name = "user_ip")
    private String ip;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;
}
