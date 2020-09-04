package com.manning.ssia.milestone.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

/*
create table if not exists GRANT (
    ID INT auto_increment   primary key,
    GRANT VARCHAR(50) not null,
    CLIENT_ID INT not null,
    constraint GRANT_CLIENT_ID_FK
        foreign key (CLIENT_ID) references CLIENT (ID)
);
*/
@Data
@Entity
public class Grant {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private String grant;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "CLIENT_ID", nullable = false)
    private com.manning.ssia.milestone.jpa.Client client ;

    public Grant() {
    }

    public Grant(String grant, com.manning.ssia.milestone.jpa.Client client) {
        this.grant=grant;
        this.client=client;
    }

    @Override
    public String toString() {
        return "Grant{" +
                "id=" + id +
                ", grant='" + grant + '\'' +
                '}';
    }
}
