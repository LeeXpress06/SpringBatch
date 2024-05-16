package com.SpringBatchDemo.SpringBatch;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CustomerPk.class)
public class Customer {

    @Id
    private long emp;
    @Id
    private String name;
    private String department;
    private String gender;
}
