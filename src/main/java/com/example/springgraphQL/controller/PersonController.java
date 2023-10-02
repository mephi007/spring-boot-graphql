package com.example.springgraphQL.controller;

import com.example.springgraphQL.dao.PersonRepository;
import com.example.springgraphQL.entity.Person;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import jakarta.annotation.PostConstruct;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class PersonController {

    @Autowired
    private PersonRepository personRepository;

    @Value("classpath:person.graphqls")
    private Resource schemaResource;

    private GraphQL graphQl;

    @PostConstruct
    public void loadSchema() throws IOException {
        File schemaFile = schemaResource.getFile();
        TypeDefinitionRegistry registry = new SchemaParser().parse(schemaFile);
        RuntimeWiring wiring = buildWiring();
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, wiring);
        graphQl = GraphQL.newGraphQL(schema).build();
    }

    private RuntimeWiring buildWiring() {

        DataFetcher<List<Person>> fetcher1 = data->{
            return (List<Person>) personRepository.findAll();
        };

        DataFetcher<Person> fetcher2 = data->{
            return personRepository.findByEmail(data.getArgument("email"));
        };

        return RuntimeWiring.newRuntimeWiring().type("Query", typeWriting->
            typeWriting.dataFetcher("getAllPerson", fetcher1).dataFetcher("findPerson", fetcher2)).build();

    }

    @GetMapping("/")
    public String home(){
        return "home";
    }

    @PostMapping("/addPerson")
    public String addPerson(@RequestBody List<Person> persons){
        personRepository.saveAll(persons);
        return String.format("record inserted %d", persons.size());
    }

    @GetMapping("/findAllPerson")
    public List<Person> getPersons(){
        return (List<Person>) personRepository.findAll();
    }

    @PostMapping("/getAll")
    public ResponseEntity<Object> getAll(@RequestBody String query){
        ExecutionResult result = graphQl.execute(query);
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }

    @PostMapping("/getPersonByEmail")
    public ResponseEntity<Object> getPersonByEmail(@RequestBody String query){
        ExecutionResult result = graphQl.execute(query);
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }
}
