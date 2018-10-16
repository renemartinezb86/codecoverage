/**
 *
 */
package com.crossover.techtrial.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.crossover.techtrial.model.Person;
import com.crossover.techtrial.repositories.PersonRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kshah
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PersonControllerTest {

    MockMvc mockMvc;

    @Mock
    private PersonController personController;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    PersonRepository personRepository;

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(personController).build();
    }

    /**
     * Fixed test method name testPanelShouldBeRegistered
     * remove registrationDate from json payload as not defined on Person Model
     */
    @Test
    public void testPersonShouldBeRegistered() throws Exception {
        HttpEntity<Object> person = getHttpEntity(
                "{\"name\": \"test 1\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\"}");
        ResponseEntity<Person> response = template.postForEntity(
                "/api/person", person, Person.class);
        //Delete this user
        personRepository.deleteById(response.getBody().getId());
        Assert.assertEquals("test 1", response.getBody().getName());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testPersonShouldBeLoadedById() throws Exception {
        Person person = new Person();
        Assert.assertTrue(person.toString().contains("Person"));
        person.setName("test 1");
        person.setEmail("test10000000000001@gmail.com");
        person.setRegistrationNumber("41DCT");
        personRepository.save(person);
        ResponseEntity<Person> response = template.getForEntity(
                "/api/person/" + person.getId(), Person.class);
        //Delete this user
        personRepository.deleteById(response.getBody().getId());
        Assert.assertEquals(person, response.getBody());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testPersonShouldBeAllLoaded() throws Exception {
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
        ResponseEntity<Person[]> response = template.getForEntity("/api/person", Person[].class);
        //Delete those users
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

    @Test
    public void when() {
        List<String> l = Mockito.mock(ArrayList.class);
        l.add("o");
        Mockito.verify(l).add("o");
        Assert.assertEquals(0, l.size());
    }

}
