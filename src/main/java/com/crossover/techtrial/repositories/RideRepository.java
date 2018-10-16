/**
 *
 */
package com.crossover.techtrial.repositories;

import com.crossover.techtrial.dto.TopDriverDTO;
import com.crossover.techtrial.model.Ride;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author crossover
 */
@RestResource(exported = false)
public interface RideRepository extends CrudRepository<Ride, Long> {
    Optional<Ride> findById(Long id);

    //Custom repository method for allDailyElectricityFromYesterday fn
    @Query("select new com.crossover.techtrial.dto.TopDriverDTO (r.driver.name, r.driver.email, sum(r.duration),max(r.duration),avg(r.distance)) " +
            "from Ride r \n" +
            "where r.startTime>?1 and r.endTime < ?2\n" +
            "group by r.driver.id \n" +
            "order by r.duration desc")
    List<TopDriverDTO> getTopDriver(LocalDateTime start, LocalDateTime end);
}
