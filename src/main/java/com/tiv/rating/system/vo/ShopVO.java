package com.tiv.rating.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.tiv.rating.system.entity.Shop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopVO {

    @JsonUnwrapped
    private Shop shop;

    /**
     * 距离/m
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double distance;

}
