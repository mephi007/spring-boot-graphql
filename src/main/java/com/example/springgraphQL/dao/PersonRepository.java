package com.example.springgraphQL.dao;

import com.example.springgraphQL.entity.Person;
import org.springframework.data.repository.CrudRepository;

import java.util.List;




public interface PersonRepository extends CrudRepository<Person, Integer>{

    Person findByEmail(String email);

}
