package com.SpringBatchDemo.SpringBatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@AllArgsConstructor
@Data
@EqualsAndHashCode

public class CustomerPk implements Serializable {

    private long emp;
    private String name;



}
