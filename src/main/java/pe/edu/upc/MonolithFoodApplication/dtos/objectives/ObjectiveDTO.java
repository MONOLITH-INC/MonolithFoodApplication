package pe.edu.upc.MonolithFoodApplication.dtos.objectives;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectiveDTO {
    private String name;
    private String information;
    private Double proteinPerKg;
    private Boolean selected = false;
    
    // @Override
    // public String toString() {
    //     return "ObjectiveDTO {"
    //         + "name='" + name + '\''
    //         + ", information='" + information + '\''
    //         + ", proteinPerKg=" + proteinPerKg
    //         + ", selected=" + selected
    //         + '}';
    // }
}
