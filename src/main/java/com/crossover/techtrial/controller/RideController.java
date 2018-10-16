/**
 *
 */
package com.crossover.techtrial.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.crossover.techtrial.repositories.RideRepository;
import com.crossover.techtrial.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.crossover.techtrial.dto.TopDriverDTO;
import com.crossover.techtrial.model.Ride;
import com.crossover.techtrial.service.RideService;

/**
 * RideController for Ride related APIs.
 *
 * @author crossover
 */
@RestController
public class RideController {

    @Autowired
    RideService rideService;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    PersonService personService;

    @PostMapping(path = "/api/ride")
    public ResponseEntity<Ride> createNewRide(@RequestBody Ride ride) {
        String errors = "";
        //Validating existing drivers and riders
        if (personService.findById(ride.getDriver().getId()) == null)
            errors += "Only registered drivers allowed. ";

        if (personService.findById(ride.getRider().getId()) == null)
            errors += "Only registered riders allowed. ";

        //Validating chronological rides time
        if (!ride.getEndTime().isAfter(ride.getStartTime())) {
            errors += "Ride endTime should be greater than startTime. ";
        }
        //Pre-calculation of ride duration for new API
        long seconds = ride.getStartTime().until(ride.getEndTime(), ChronoUnit.SECONDS);
        ride.setDuration(seconds);
        return errors.length() > 0 ? new ResponseEntity(errors, HttpStatus.BAD_REQUEST) : ResponseEntity.ok(rideService.save(ride));
    }

    @GetMapping(path = "/api/ride/{ride-id}")
    public ResponseEntity<Ride> getRideById(@PathVariable(name = "ride-id", required = true) Long rideId) {
        Ride ride = rideService.findById(rideId);
        return ride != null ? ResponseEntity.ok(ride) : ResponseEntity.notFound().build();
    }

    /**
     * This API returns the top 5 drivers with their email,name, total minutes, maximum ride duration in minutes.
     * Only rides that starts and ends within the mentioned durations should be counted.
     * Any rides where either start or endtime is outside the search, should not be considered.
     * <p>
     * DONT CHANGE METHOD SIGNATURE AND RETURN TYPES
     *
     * @return
     */
    @GetMapping(path = "/api/top-rides")
    public ResponseEntity<List<TopDriverDTO>> getTopDriver(
            @RequestParam(value = "max", defaultValue = "5") Long count,
            @RequestParam(value = "startTime", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime) {
        List<TopDriverDTO> topDrivers = rideRepository.
                getTopDriver(startTime, endTime).stream().limit(5).collect(Collectors.toList());
        /**
         * Your Implementation Here. And Fill up topDrivers Arraylist with Top
         *
         */

        return ResponseEntity.ok(topDrivers);

    }

}
