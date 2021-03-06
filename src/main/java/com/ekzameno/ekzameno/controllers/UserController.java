package com.ekzameno.ekzameno.controllers;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ekzameno.ekzameno.dtos.CreateUserDTO;
import com.ekzameno.ekzameno.filters.Protected;
import com.ekzameno.ekzameno.models.User;
import com.ekzameno.ekzameno.services.UserService;

/**
 * Controller for Users.
 */
@Path("/users")
@Protected
public class UserController {
    private final UserService userService = new UserService();

    /**
     * Retrieve all users.
     *
     * @return all users
     */
    @GET
    @RolesAllowed({ "administrator", "instructor" })
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsers() {
        return userService.getUsers();
    }

    /**
     * Create a user using the information provided in the DTO.
     *
     * @param dto create user DTO
     * @return response to the client.
     */
    @POST
    @RolesAllowed({ "administrator" })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(CreateUserDTO dto) {
        User user = userService.registerUser(
            dto.name,
            dto.email,
            dto.password,
            dto.type
        );

        if (user != null) {
            return Response
                .status(Response.Status.CREATED)
                .entity(user)
                .build();
        } else {
            return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .build();
        }
    }
}
