/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hand.demo.model.Dtos;

/**
 *
 * @author Mohammad
 */
import java.util.UUID;

public record GetImageDtoProduct(UUID imageId, boolean main,String url ,Integer sortOrder) {
}