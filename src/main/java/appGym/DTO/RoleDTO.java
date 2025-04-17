package appGym.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoleDTO {
    private Long id;
    private String name;

    // Constructor, getters y setters
    public RoleDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}