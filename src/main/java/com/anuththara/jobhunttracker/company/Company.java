package com.anuththara.jobhunttracker.company;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private Long id;
    private String name;
    private String industry;
    private String website;
    private String location;
    private String notes;
}
