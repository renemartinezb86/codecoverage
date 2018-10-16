/**
 *
 */
package com.crossover.techtrial.controller;

import com.crossover.techtrial.dto.TopDriverDTO;
import com.crossover.techtrial.model.Person;
import com.crossover.techtrial.model.Ride;
import com.crossover.techtrial.repositories.PersonRepository;
import com.crossover.techtrial.repositories.RideRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Rene
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RideControllerTest {

    MockMvc mockMvc;

    @Mock
    private RideController rideController;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    PersonRepository personRepository;

    //private DateTimeFormatter formatter;

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(rideController).build();
        //formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Test
    public void testRideShouldBeRegistered() throws Exception {
        Person person1 = new Person();
        person1.setName("test 1");
        person1.setEmail("test10000000000001@gmail.com");
        person1.setRegistrationNumber("41DCT");
        personRepository.save(person1);
        Person person2 = new Person();
        person2.setName("test 2");
        person2.setEmail("test20000000000001@gmail.com");
        person2.setRegistrationNumber("41DCT");
        personRepository.save(person2);
        HttpEntity<Object> ride = getHttpEntity(
                "{\"startTime\":\"2018-08-24T09:00:00\",\"endTime\":\"2018-08-24T10:00:00\",\"distance\":15,\"driver\":{\"id\":" + person1.getId() + "},\"rider\":{\"id\":" + person2.getId() + "}}");
        ResponseEntity<Ride> response = template.postForEntity(
                "/api/ride", ride, Ride.class);
        //Delete
        personRepository.deleteById(person1.getId());
        personRepository.deleteById(person2.getId());
        Assert.assertEquals(new Long(15), response.getBody().getDistance());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testRideShouldNotBeRegistered() throws Exception {
        HttpEntity<Object> ride = getHttpEntity(
                "{\"startTime\":\"2018-08-24T10:00:00\",\"endTime\":\"2018-08-24T10:00:00\",\"distance\":15,\"driver\":{\"id\":0},\"rider\":{\"id\":0}}");
        ResponseEntity<String> response = template.postForEntity(
                "/api/ride", ride, String.class);
        Assert.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void testRideShouldBeLoadedById() throws Exception {
        Ride ride = new Ride();
        Assert.assertTrue(ride.toString().contains("Ride"));
        ride.setStartTime(LocalDateTime.parse("2018-08-24T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        ride.setEndTime(LocalDateTime.parse("2018-08-24T11:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        ride.setDistance(new Long(15));
        rideRepository.save(ride);
        ResponseEntity<Ride> response = template.getForEntity(
                "/api/ride/" + ride.getId(), Ride.class);
        //Delete this ride
        rideRepository.deleteById(response.getBody().getId());
        Assert.assertEquals(ride, response.getBody());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testRideShouldGetTopDriver() throws Exception {
        Person person1 = new Person();
        person1.setName("test 1");
        person1.setEmail("test10000000000001@gmail.com");
        person1.setRegistrationNumber("41DCT");
        personRepository.save(person1);
        Person person2 = new Person();
        person2.setName("test 2");
        person2.setEmail("test20000000000001@gmail.com");
        person2.setRegistrationNumber("41DCT");
        personRepository.save(person2);
        HttpEntity<Object> ride1 = getHttpEntity(
                "{\"startTime\":\"2018-08-24T09:00:00\",\"endTime\":\"2018-08-24T10:00:00\",\"distance\":15,\"driver\":{\"id\":" + person1.getId() + "},\"rider\":{\"id\":" + person2.getId() + "}}");
        HttpEntity<Object> ride2 = getHttpEntity(
                "{\"startTime\":\"2018-08-24T09:00:00\",\"endTime\":\"2018-08-24T10:30:00\",\"distance\":15,\"driver\":{\"id\":" + person2.getId() + "},\"rider\":{\"id\":" + person1.getId() + "}}");
        ResponseEntity<Ride> response1 = template.postForEntity(
                "/api/ride", ride1, Ride.class);
        ResponseEntity<Ride> response2 = template.postForEntity(
                "/api/ride", ride2, Ride.class);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/top-rides")
                .queryParam("startTime", "2018-08-24T08:00:00")
                .queryParam("endTime", "2018-08-24T14:00:00");
        ResponseEntity<TopDriverDTO[]> response = template.getForEntity(builder.toUriString(), TopDriverDTO[].class);
        //Delete
        personRepository.deleteById(person1.getId());
        personRepository.deleteById(person2.getId());
        Assert.assertTrue(response.getBody().length > 1);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    private HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<Object>(body, headers);
    }

}
