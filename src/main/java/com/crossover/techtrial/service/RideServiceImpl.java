/**
 *
 */
package com.crossover.techtrial.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.crossover.techtrial.model.Ride;
import com.crossover.techtrial.repositories.RideRepository;

/**
 * @author crossover
 */
@Service
public class RideServiceImpl implements RideService {

    @Autowired
    RideRepository rideRepository;

    public Ride save(Ride ride) {
        return rideRepository.save(ride);
    }

    @Override
    public Ride findById(Long rideId) {
        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        return optionalRide.orElse(null);
    }
}
