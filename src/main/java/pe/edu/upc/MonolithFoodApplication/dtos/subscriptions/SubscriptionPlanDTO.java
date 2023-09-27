package pe.edu.upc.MonolithFoodApplication.dtos.subscriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.MonolithFoodApplication.entities.RoleEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private RoleEnum subscriptionPlan;
    private Boolean confirmed;
    // private int durationMonths;
   
}