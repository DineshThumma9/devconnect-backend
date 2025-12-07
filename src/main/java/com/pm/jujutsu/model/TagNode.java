package com.pm.jujutsu.model;


import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Data
public class TagNode {

 @Id
 private String name;

}
