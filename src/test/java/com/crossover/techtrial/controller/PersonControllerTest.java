/**
 *
 */
package com.crossover.techtrial.controller;

import com.crossover.techtrial.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.crossover.techtrial.model.Person;
import com.crossover.techtrial.repositories.PersonRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


/**
 * @author kshah
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PersonControllerTest {

    MockMvc mockMvc;

    private static final String DEFAULT_NAME = "PersonName";
    private static final String DEFAULT_EMAIL = "person@email.com";
    private static final String DEFAULT_REGISTRATIONNUMBER = "PersonRegistrationNumber";

    private Person person;

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

    @Before
    public void initTest() {
        personRepository.deleteAll();
        person = createEntity();
    }

    public static Person createEntity() {
        Person person = new Person();
        person.setName(DEFAULT_NAME);
        person.setEmail(DEFAULT_EMAIL);
        person.setRegistrationNumber(DEFAULT_REGISTRATIONNUMBER);
        return person;
    }

    /**
     * Test method for creating a new entity and Entity getters.
     * remove registrationDate from json payload as not defined on Person Model
     */
    @Test
    public void testPersonShouldBeRegistered() throws Exception {
        HttpEntity<Object> personHttp = getHttpEntity(TestUtil.convertObjectToJsonString(person));
        ResponseEntity<Person> response = template.postForEntity("/api/person", personHttp, Person.class);
        List<Person> personList = new ArrayList();
        personRepository.findAll().forEach(personList::add);
        Person testPerson = personList.get(personList.size() - 1);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(testPerson.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPerson.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testPerson.getRegistrationNumber()).isEqualTo(DEFAULT_REGISTRATIONNUMBER);
    }

    @Test
    public void testPersonShouldBeLoadedById() throws Exception {
        personRepository.save(person);
        ResponseEntity<Person> response = template.getForEntity(
                "/api/person/" + person.getId(), Person.class);
        assertThat(person).isEqualTo(response.getBody());
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void testPersonShouldNotBeFound() throws Exception {
        ResponseEntity<Person> response = template.getForEntity(
                "/api/person/" + Long.MAX_VALUE, Person.class);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void testPersonShouldBeAllLoaded() throws Exception {
        personRepository.save(person);
        person.setId(null);
        personRepository.save(person);
        ResponseEntity<Person[]> response = template.getForEntity("/api/person", Person[].class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), arrayWithSize(greaterThan(1)));
    }

    private HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<Object>(body, headers);
    }
}
