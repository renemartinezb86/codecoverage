/**
 *
 */
package com.crossover.techtrial.controller;

import com.crossover.techtrial.TestUtil;
import com.crossover.techtrial.dto.TopDriverDTO;
import com.crossover.techtrial.model.Person;
import com.crossover.techtrial.model.Ride;
import com.crossover.techtrial.repositories.PersonRepository;
import com.crossover.techtrial.repositories.RideRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Rene
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RideControllerTest {

    MockMvc mockMvc;

    private static final LocalDateTime DEFAULT_STARTTIME = LocalDateTime.parse("2018-08-24T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final LocalDateTime DEFAULT_ENDTIME = LocalDateTime.parse("2018-08-24T12:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final Long DEFAULT_DISTANCE = Long.valueOf(15);
    private static final Person DEFAULT_RIDER = PersonControllerTest.createEntity();
    private static final Person DEFAULT_DRIVER = PersonControllerTest.createEntity();
    private Ride ride;

    @Mock
    private RideController rideController;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    PersonRepository personRepository;

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(rideController).build();
        //formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Before
    public void initTest() {
        rideRepository.deleteAll();
        personRepository.deleteAll();
        ride = createEntity();
    }

    public Ride createEntity() {
        ride = new Ride();
        DEFAULT_DRIVER.setId(null);
        DEFAULT_RIDER.setId(null);
        personRepository.save(DEFAULT_DRIVER);
        personRepository.save(DEFAULT_RIDER);
        ride.setStartTime(DEFAULT_STARTTIME);
        ride.setEndTime(DEFAULT_ENDTIME);
        ride.setDistance(DEFAULT_DISTANCE);
        ride.setRider(DEFAULT_RIDER);
        ride.setDriver(DEFAULT_DRIVER);
        return ride;
    }

    @Test
    public void testRideShouldBeRegistered() throws Exception {
        JSONObject rideJson = new JSONObject();
        rideJson.put("startTime", DEFAULT_STARTTIME.toString());
        rideJson.put("endTime", DEFAULT_ENDTIME.toString());
        rideJson.put("distance", DEFAULT_DISTANCE);
        rideJson.put("rider", new JSONObject().put("id", DEFAULT_RIDER.getId()));
        rideJson.put("driver", new JSONObject().put("id", DEFAULT_DRIVER.getId()));

        HttpEntity<Object> rideHttp = getHttpEntity(rideJson.toString());
        ResponseEntity<Ride> response = template.postForEntity("/api/ride", rideHttp, Ride.class);
        List<Ride> rideList = new ArrayList();
        rideRepository.findAll().forEach(rideList::add);
        Ride testRide = rideList.get(rideList.size() - 1);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(testRide.getDistance()).isEqualTo(DEFAULT_DISTANCE);
        assertThat(testRide.getDuration()).isEqualTo(DEFAULT_STARTTIME.until(DEFAULT_ENDTIME, ChronoUnit.SECONDS));
        rideRepository.delete(ride);
    }

    @Test
    public void testRideShouldNotBeRegistered() throws Exception {
        JSONObject rideJson = new JSONObject();
        rideJson.put("startTime", DEFAULT_STARTTIME.toString());
        rideJson.put("endTime", DEFAULT_STARTTIME.toString());
        rideJson.put("distance", DEFAULT_DISTANCE);
        rideJson.put("rider", new JSONObject().put("id", DEFAULT_RIDER.getId()));
        rideJson.put("driver", new JSONObject().put("id", DEFAULT_DRIVER.getId()));
        HttpEntity<Object> ride = getHttpEntity(
                rideJson.toString());
        ResponseEntity<String> response = template.postForEntity(
                "/api/ride", ride, String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testRideShouldBeLoadedById() throws Exception {
        rideRepository.save(ride);
        ResponseEntity<Ride> response = template.getForEntity(
                "/api/ride/" + ride.getId(), Ride.class);
        assertThat(ride).isEqualTo(response.getBody());
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        rideRepository.delete(ride);
    }

    @Test
    public void testRideShouldGetTopDriver() throws Exception {
        rideRepository.save(ride);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/top-rides")
                .queryParam("startTime", "2018-08-24T08:00:00")
                .queryParam("endTime", "2018-08-24T14:00:00");
        ResponseEntity<TopDriverDTO[]> response = template.getForEntity(builder.toUriString(), TopDriverDTO[].class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), arrayWithSize(greaterThan(0)));
        rideRepository.delete(ride);
    }

    private HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<Object>(body, headers);
    }
}
