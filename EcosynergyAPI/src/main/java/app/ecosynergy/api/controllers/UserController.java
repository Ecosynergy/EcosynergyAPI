package app.ecosynergy.api.controllers;

import app.ecosynergy.api.data.vo.v1.UserVO;
import app.ecosynergy.api.services.UserServices;
import app.ecosynergy.api.util.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Endpoint")
@RestController
@RequestMapping("/api/user/v1")
public class UserController {
    @Autowired
    UserServices services;

    @Operation(summary = "Find user by ID", description = "Retrieve a user by ID")
    @GetMapping(value = "/findId/{id}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public UserVO findById(@PathVariable("id") Long id){
        return services.findById(id);
    }

    @Operation(summary = "Find user by Email", description = "Retrieve a user by Email")
    @GetMapping(value = "/findEmail/{email}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public UserVO findByEmail(@PathVariable("email") String email) {
        return services.findByEmail(email);
    }

    @Operation(summary = "Find user by Username", description = "Retrieve a user by Username")
    @GetMapping(value = "/findUsername/{username}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public UserVO findByUsername(@PathVariable("username") String username){
        return services.findByUsername(username);
    }

    @Operation(summary = "Search Users by Username", description = "Retrieve users by Username")
    @GetMapping(value = "/search/{username}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public List<UserVO> searchUserByUsername(@PathVariable("username") String username){
        return services.findByUsernameContaining(username);
    }

    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML})
    public ResponseEntity<PagedModel<EntityModel<UserVO>>> findAll(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ){
        page--;

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, limit, Sort.by(sortDirection, "fullName"));

        return ResponseEntity.ok(services.findAll(pageable));
    }

    @Operation(summary = "Update user", description = "Update an existing user")
    @PutMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML},
            consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public UserVO update(@PathVariable Long id, @RequestBody UserVO user){
        return services.update(id, user);
    }

    @Operation(summary = "Delete user", description = "Delete a user by ID")
    @DeleteMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public ResponseEntity<?> delete(@PathVariable("id") Long id){
        services.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Recovery Password", description = "Recovery a user password")
    @PostMapping(value = "/recoverPassword",
                produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML},
                consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public UserVO recoverPassword(@RequestBody UserVO user){
        return services.recoverPassword(user);
    }
}
