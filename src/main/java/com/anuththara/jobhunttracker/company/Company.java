package com.anuththara.jobhunttracker.company;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String industry;

    @Column(length = 255)
    private String website;

    @Column(length = 100)
    private String location;

    @Column(length = 1000)
    private String notes;


}
