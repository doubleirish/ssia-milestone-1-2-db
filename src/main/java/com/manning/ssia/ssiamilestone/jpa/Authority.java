package com.manning.ssia.ssiamilestone.jpa;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.*;

/*
create table  if not exists AUTHORITY
(
    ID INT auto_increment primary key,
    USER_ID INT not null,
    AUTHORITY VARCHAR(50) not null,
    constraint AUTHORITY_USER_USERNAME_FK
        foreign key (USER_ID) references USER (ID)
);
 */
@Data
@Entity
public class Authority {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String authority;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user ;

    public Authority() {
    }

    public Authority(String authority, User user) {
        this.authority=authority;
        this.user=user;
    }

    @Override
    public String toString() {
        return "Authority{" +
                "id=" + id +
                ", authority='" + authority + '\'' +
                '}';
    }
}
