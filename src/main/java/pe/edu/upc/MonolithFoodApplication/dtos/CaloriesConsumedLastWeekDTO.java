package pe.edu.upc.MonolithFoodApplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaloriesConsumedLastWeekDTO {
    private String username;
    private Double totalCalories;
}