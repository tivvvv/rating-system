package com.tiv.rating.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrollDTO<T> {

    private List<T> list;

    private Long lastId;

    private Integer offset;

}
