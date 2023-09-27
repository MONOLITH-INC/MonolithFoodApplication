package pe.edu.upc.MonolithFoodApplication.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.sql.Timestamp;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;
import pe.edu.upc.MonolithFoodApplication.dtos.general.ResponseDTO;
import pe.edu.upc.MonolithFoodApplication.dtos.general.SimpleObjectDTO;
import pe.edu.upc.MonolithFoodApplication.dtos.user.InfoDTO;
import pe.edu.upc.MonolithFoodApplication.dtos.user.ObjectivesResponseDTO;
import pe.edu.upc.MonolithFoodApplication.dtos.user.PhotoDTO;
import pe.edu.upc.MonolithFoodApplication.dtos.foodintake.GetIntakesDTO;
import pe.edu.upc.MonolithFoodApplication.dtos.foodintake.IntakeDTO;
import pe.edu.upc.MonolithFoodApplication.dtos.foodintake.NewIntakeDTO;
import pe.edu.upc.MonolithFoodApplication.dtos.foodintake.UpdateIntakeDTO;
import pe.edu.upc.MonolithFoodApplication.entities.EatEntity;
import pe.edu.upc.MonolithFoodApplication.entities.FoodEntity;
import pe.edu.upc.MonolithFoodApplication.entities.UnitOfMeasurementEnum;
import pe.edu.upc.MonolithFoodApplication.entities.UserEntity;
import pe.edu.upc.MonolithFoodApplication.entities.ObjectiveEntity;
import pe.edu.upc.MonolithFoodApplication.repositories.EatRepository;
import pe.edu.upc.MonolithFoodApplication.repositories.FoodRepository;
import pe.edu.upc.MonolithFoodApplication.repositories.ObjectiveRepository;
import pe.edu.upc.MonolithFoodApplication.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    // * Atributos
    // Inyección de dependencias
    private final UserRepository userRepository;
    private final ObjectiveRepository objectiveRepository;
    private final EatRepository eatRepository;
    private final FoodRepository foodRepository;
    // Log de errores y eventos
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public ResponseDTO getAllFoodIntake(String username) {
        // Verifica que el usuario exista
        // Se usa optional ya que el usuario puede o no existir y de eso se encarga JPA
        Optional<UserEntity> getUser = userRepository.findByUsername(username);
        if(!getUser.isPresent()) return new ResponseDTO("Usuario no encontrado", 404);
        // Se obtiene una lista de Object[] con los resultados de la consulta
        // No se usa Optional porque al ser una consulta personalizada, siempre se obtendrá un resultado con o sin datos
        List<Object[]> results = eatRepository.findAllIntakesByUsername(username);
        if(results.isEmpty()) return new ResponseDTO("No se encontraron alimentos consumidos por el usuario", 200);
        // Mapea el resultado manualmente de una lista de Object[] a una lista de IntakeDTO
        List<IntakeDTO> myIntakes = results.stream().map(result -> {
            return new IntakeDTO(
                (String) result[0],
                (String) result[1],
                (UnitOfMeasurementEnum) result[2],
                (Double) result[3],
                (Double) result[4],
                (Timestamp) result[5]
            );
        }).collect(Collectors.toList());
        return new GetIntakesDTO("Alimentos recuperados correctamente.", 200, myIntakes);
    }

    public ResponseDTO addFoodIntake(String username, NewIntakeDTO foodIntakeDTO) {
        // Verifica que el usuario exista
        Optional<UserEntity> getUser = userRepository.findByUsername(username);
        if(!getUser.isPresent()) return new ResponseDTO("Usuario no encontrado.", 404);
        // Verifica que el alimento exista
        Optional<FoodEntity> getFood = foodRepository.findByName(foodIntakeDTO.getName());
        if(!getFood.isPresent()) return new ResponseDTO("Alimento no encontrado.", 404);
        // Crear EatEntity con la información emitida por el usuario
        EatEntity newEat = new EatEntity();
        newEat.setUser(getUser.get());
        newEat.setFood(getFood.get());
        newEat.setEatQuantity(foodIntakeDTO.getQuantity());
        newEat.setUnitOfMeasurement(foodIntakeDTO.getUnitOfMeasurement());
        newEat.setDate(foodIntakeDTO.getDate());
        // Guardar EatEntity en la lista de Alimentos del usuario
        getUser.get().getEats().add(newEat);
        // Guardar cambios en la base de datos
        userRepository.save(getUser.get());
        // Retornar mensaje de éxito
        logger.info("Alimento registrado correctamente para el usuario " + username + ".");
        return new ResponseDTO("Alimento registrado correctamente.", 200);
    }

    public ResponseDTO updateFoodIntake(String username, UpdateIntakeDTO newFoodIntakeDTO) {
        // Verifica que el usuario exista
        Optional<UserEntity> getUser = userRepository.findByUsername(username);
        if(!getUser.isPresent())
            return new ResponseDTO("Usuario no encontrado.", 404);
        // Verifica que el registro de ingesta exista
        Optional<EatEntity> getEat = eatRepository.findById(newFoodIntakeDTO.getEatId());
        if(!getEat.isPresent())
            return new ResponseDTO("Registro no encontrado.", 404);
        // Verifica que el alimento exista
        Optional<FoodEntity> getFood = foodRepository.findByName(newFoodIntakeDTO.getName());
        if(!getFood.isPresent())
            return new ResponseDTO("Alimento no encontrado", 404);

        // Si la solicitud realizada pertenece al usuario que la realizó
        EatEntity newEat = getEat.get();
        if(!newEat.getUser().getUsername().equals(username))
            return new ResponseDTO("No tiene permisos para actualizar este registro.", 403);
        // Lista para guardar los campos que el usuario haya actualizado
        List<String> updatedFields = new ArrayList<>();

        // Si el elemento de eat tiene .getFood() == null, significa que es un registro de ingesta de una receta
        if(newEat.getFood() == null)
            return new ResponseDTO("No se puede actualizar el registro de ingesta de una receta.", 400);

        newEat.setUser(getUser.get());
        if(newFoodIntakeDTO.getName() != null && !newFoodIntakeDTO.getName().isEmpty()) {
            if(!newEat.getFood().getName().equals(newFoodIntakeDTO.getName())) {
                newEat.setFood(getFood.get());
                updatedFields.add("Alimento");
            }
        }
        if(newFoodIntakeDTO.getQuantity() != null && newFoodIntakeDTO.getQuantity() > 0) {
            if(!newEat.getEatQuantity().equals(newFoodIntakeDTO.getQuantity())) {
                newEat.setEatQuantity(newFoodIntakeDTO.getQuantity());
                updatedFields.add("Cantidad");
            }
        }
        if(newFoodIntakeDTO.getUnitOfMeasurement() != null) {
            if(!newEat.getUnitOfMeasurement().equals(newFoodIntakeDTO.getUnitOfMeasurement())) {
                newEat.setUnitOfMeasurement(newFoodIntakeDTO.getUnitOfMeasurement());
                updatedFields.add("Unidad de medida");
            }
        }
        if(newFoodIntakeDTO.getDate() != null) {
            if(!newEat.getDate().equals(newFoodIntakeDTO.getDate())) {
                newEat.setDate(newFoodIntakeDTO.getDate());
                updatedFields.add("Fecha");
            }
        }
        try {
            if(updatedFields.size() != 0) {
                eatRepository.save(getEat.get());
                logger.info("Registro de ingesta actualizado correctamente para el usuario " + username + ".");
            }
            else {
                logger.info("No se eactualizó ningún campo para el usuario " + username + ".");
                return new ResponseDTO("No se actualizó ningún campo.", 200);
            }
            return new ResponseDTO("Registro de ingesta actualizado correctamente.", 200);
        } catch (Exception e) {
            logger.error("Error al actualizar el registro de ingesta del usuario " + username + ": " + e.getMessage());
            return new ResponseDTO("Error al actualizar el registro de ingesta.", 500);
        }
    }
    
    public ResponseDTO deleteFoodIntake(String username, Long eatId) {
        // Verificar que el usuario exista
        Optional<UserEntity> getUser = userRepository.findByUsername(username);
        if (!getUser.isPresent())
            return new ResponseDTO("Usuario no encontrado.", 404);
        // Verificar que el registro de ingesta exista
        Optional<EatEntity> getEat = eatRepository.findById(eatId);
        if (!getEat.isPresent())
            return new ResponseDTO("Registro no encontrado.", 404);
        // Si la solicitud realizada pertenece al usuario que la realizó
        EatEntity eatToDelete = getEat.get();
        if (!eatToDelete.getUser().getUsername().equals(username))
            return new ResponseDTO("No tiene permisos para eliminar este registro.", 403);
        try {
            // Eliminar el registro
            eatRepository.delete(eatToDelete);
            logger.info("Registro de ingesta eliminado correctamente para el usuario " + username + ".");
            return new ResponseDTO("Registro de ingesta eliminado correctamente.", 200);
        } catch (Exception e) {
            logger.error("Error al eliminar el registro de ingesta del usuario " + username + ": " + e.getMessage());
            return new ResponseDTO("Error al eliminar el registro de ingesta.", 500);
        }
    }
    

    // public CaloricIntakeAlertDTO checkDailyCaloricIntake(String username) {
    //     UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

    //     // 1. Obtener el total de calorías consumidas por el usuario en el día.
    //     List<EatEntity> todaysEats = eatRepository.findByUsernameAndDate(username, LocalDate.now());
    //     double totalCaloriesConsumed = 0;
    //     for (EatEntity eat : todaysEats) {
    //         FoodEntity food = eat.getFood();
    //         List<CompositionEntity> compositions = compositionRepository.findByFood(food);
    //         for (CompositionEntity composition : compositions) {
    //             NutrientEntity nutrient = composition.getNutrient();
    //             if ("Calories".equalsIgnoreCase(nutrient.getName())) {
    //                 totalCaloriesConsumed += composition.getNutrientQuantity();
    //             }
    //         }
    //     }

    //     // 2. Comparar ese total con las calorías recomendadas para el usuario.
    //     UserPersonalInfoEntity userPersonalInfo = user.getUserPersonalInfo();
    //     double recommendedCaloricLimit = userPersonalInfo.getDailyCaloricIntake();

    //     // 3. Si las calorías consumidas superan las recomendadas, mostrar una alerta.
    //     String message;
    //     double exceededAmount = totalCaloriesConsumed - recommendedCaloricLimit;
    //     if (totalCaloriesConsumed > recommendedCaloricLimit) {
    //         message = "Alert: You have exceeded your recommended daily caloric intake!";
    //     } else {
    //         message = "You are within your recommended daily caloric intake.";
    //         exceededAmount = 0.0;
    //     }

    //     return new CaloricIntakeAlertDTO(totalCaloriesConsumed > recommendedCaloricLimit, totalCaloriesConsumed, recommendedCaloricLimit, exceededAmount, message);
    // }

    // * Informacion
    public ResponseDTO getInformation(String username) {
        // Verifica que el usuario exista
        Optional<UserEntity> getUser = userRepository.findByUsername(username);
        if (!getUser.isPresent()) return new ResponseDTO("Usuario no encontrado.", 404);
        // Retorna la información del usuario
        UserEntity u = getUser.get();
        return new InfoDTO("Información recuperada correctamente.", 200,
            u.getUsername(),
            u.getEmail(),
            u.getNames(),
            u.getSurnames(),
            u.getProfileImg()
        );
    }
    public ResponseDTO updatePhoto(String username, String photoUrl) {
        // Verifica que el usuario exista
        Optional<UserEntity> getUser = userRepository.findByUsername(username);
        if (!getUser.isPresent()) return new ResponseDTO("Usuario no encontrado.", 404);
        // Actualiza y guarda la foto de perfil del usuario
        getUser.get().setProfileImg(photoUrl);
        userRepository.save(getUser.get());
        return new PhotoDTO("Foto actualizada correctamente", 200, photoUrl);
    }
    // * Objetivos
    // Obtener todos los usuarios
    public ResponseDTO getAllObjectives() {
        List<ObjectiveEntity> objectives = objectiveRepository.findAll();
        if (objectives.isEmpty()) {
            logger.error("No se encontraron objetivos.");
            return new ResponseDTO("No se encontraron objetivos.", 404);
        }
        List<SimpleObjectDTO> objectiveDTOs = objectives.stream()
                .map(obj -> new SimpleObjectDTO(obj.getName(), obj.getInformation()))
                .collect(Collectors.toList());
        return new ObjectivesResponseDTO("Objetivos recuperados correctamente.", 200, objectiveDTOs);
    }
    // Obtener los objetivos de un usuario
    public ResponseDTO getUserObjectives(String username) {
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            logger.error("Usuario no encontrado.");
            return new ResponseDTO("Usuario no encontrado.", 404);
        }
        List<ObjectiveEntity> objectives = user.get().getObjectives();
        List<SimpleObjectDTO> objectiveDTOs = objectives.stream()
                .map(obj -> new SimpleObjectDTO(obj.getName(), obj.getInformation()))
                .collect(Collectors.toList());
        return new ObjectivesResponseDTO("Objetivos recuperados correctamente.", 200, objectiveDTOs);
    }
    // Guardar o actualizar los objetivos de un usuario
    public ResponseDTO setUserObjectives(String username, List<String> objectives) {
        // Verificar si la lista de objetivos está vacía
        if (objectives.isEmpty()) {
            logger.error("No se seleccionó ningún objetivo.");
            return new ResponseDTO("Debes seleccionar al menos un objetivo.", 400);
        }
        Optional<UserEntity> getUser = userRepository.findByUsername(username);
        if (!getUser.isPresent()) {
            logger.error("Usuario no encontrado.");
            return new ResponseDTO("Usuario no encontrado.", 404);
        }
        List<ObjectiveEntity> allObjectives = objectiveRepository.findAll();
        // Validar que todos los objetivos enviados existen en la base de datos
        for (String objective : objectives) {
            boolean exists = allObjectives.stream().anyMatch(o -> o.getName().equals(objective));
            if (!exists) {
                logger.error("No se encontró uno de los objetivos ingresados.");
                return new ResponseDTO("No se encontro uno de los objetivos ingresados.", 404);
            }
        }
        // Filtrar los objetivos que coinciden con los enviados por el usuario
        List<ObjectiveEntity> newUserObjectives = allObjectives.stream()
                .filter(o -> objectives.contains(o.getName()))
                .collect(Collectors.toList());
        // Guardar los objetivos en el usuario
        getUser.get().setObjectives(newUserObjectives);
        userRepository.save(getUser.get());
        // Convertir la lista de ObjectiveEntity a SimpleObjectDTO
        List<SimpleObjectDTO> savedObjectives = newUserObjectives.stream()
                .map(entity -> new SimpleObjectDTO(entity.getName(), entity.getInformation()))
                .collect(Collectors.toList());
        return new ObjectivesResponseDTO("Objetivos guardados", 200, savedObjectives);
    }
  
}
