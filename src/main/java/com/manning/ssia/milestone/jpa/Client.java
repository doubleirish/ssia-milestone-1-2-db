package com.manning.ssia.milestone.jpa;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class Client {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Integer id;

  private String name;
  private String secret;
  @Column(name = "redirect_uri")
  private String redirectUri;
  private String scope;

  @OneToMany(mappedBy = "client", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
  private List<Grant> grants;

  public Client() {
  }

  @Override
  public String toString() {
    return "Client{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", secret='" + secret + '\'' +
            ", redirectUri='" + redirectUri + '\'' +
            ", scope='" + scope + '\'' +
            ", grants=" + grants +
            '}';
  }
}
