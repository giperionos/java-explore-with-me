package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.explore.model.EndpointView;
import ru.practicum.explore.model.EndpointEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointEntityRepository extends JpaRepository<EndpointEntity, Long> {

    /**
     *
     * @param start дата и время начала периода выборки
     * @param end дата и время конца периода выборки
     * @param uris список uri, для которых нужно сформировать выборку
     * @return результат в виде списка EndpointView
     */
    @Query("select new ru.practicum.explore.model.EndpointView(ee.app, ee.uri, count(distinct ee.ip)) "
            + " from EndpointEntity ee "
            + " where ee.creationDate between ?1 and ?2 "
            + " and ee.uri in (?3) "
            + " group by ee.app, ee.uri")
    List<EndpointView> getEndpointStatsByDateAndUniqueIpAndUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    /**
     *
     * @param start дата и время начала периода выборки
     * @param end дата и время конца периода выборки
     * @return результат в виде списка EndpointView
     */
    @Query("select new ru.practicum.explore.model.EndpointView(ee.app, ee.uri, count(distinct ee.ip)) "
            + " from EndpointEntity ee "
            + " where ee.creationDate between ?1 and ?2 "
            + " group by ee.app, ee.uri")
    List<EndpointView> getEndpointStatsByDateAndUniqueIp(LocalDateTime start, LocalDateTime end);

    /**
     *
     * @param start дата и время начала периода выборки
     * @param end дата и время конца периода выборки
     * @param uris список uri, для которых нужно сформировать выборку
     * @return результат в виде списка EndpointView
     */
    @Query("select new ru.practicum.explore.model.EndpointView(ee.app, ee.uri, count(ee.ip)) "
            + " from EndpointEntity ee "
            + " where ee.creationDate between ?1 and ?2 "
            + " and ee.uri in (?3) "
            + " group by ee.app, ee.uri")
    List<EndpointView> getEndpointStatsByDateAndUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    /**
     *
     * @param start дата и время начала периода выборки
     * @param end дата и время конца периода выборки
     * @return результат в виде списка EndpointView
     */
    @Query("select new ru.practicum.explore.model.EndpointView(ee.app, ee.uri, count(ee.ip)) "
            + " from EndpointEntity ee "
            + " where ee.creationDate between ?1 and ?2 "
            + " group by ee.app, ee.uri")
    List<EndpointView> getEndpointStatsByDate(LocalDateTime start, LocalDateTime end);
}