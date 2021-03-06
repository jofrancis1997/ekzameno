package com.ekzameno.ekzameno.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

import com.ekzameno.ekzameno.exceptions.ConflictException;
import com.ekzameno.ekzameno.mappers.InstructorMapper;
import com.ekzameno.ekzameno.mappers.StudentMapper;
import com.ekzameno.ekzameno.mappers.UserMapper;
import com.ekzameno.ekzameno.models.Administrator;
import com.ekzameno.ekzameno.models.Instructor;
import com.ekzameno.ekzameno.models.Student;
import com.ekzameno.ekzameno.models.User;
import com.ekzameno.ekzameno.shared.DBConnection;
import com.ekzameno.ekzameno.shared.IdentityMap;
import com.ekzameno.ekzameno.shared.UnitOfWork;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Service to handle users.
 */
public class UserService {
    private final UserMapper userMapper = new UserMapper();
    private final InstructorMapper instructorMapper = new InstructorMapper();
    private final StudentMapper studentMapper = new StudentMapper();

    /**
     * Retrieve all users.
     *
     * @return all subjects
     */
    public List<User> getUsers() {
        try (DBConnection connection = DBConnection.getCurrent()) {
            return userMapper.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            IdentityMap.reset();
        }
    }

    /**
     * Register a user.
     *
     * @param name name of the user to register
     * @param email email address of the user to register
     * @param password password of the user to register
     * @param type type of the user to register
     * @return the new user
     */
    public User registerUser(
        String name,
        String email,
        String password,
        String type
    ) {
        String passwordHash = BCrypt.withDefaults().hashToString(
            12,
            password.toCharArray()
        );

        try (DBConnection connection = DBConnection.getCurrent()) {
            User user;

            if (type.toLowerCase().equals("student")) {
                user = new Student(email, name, passwordHash);
            } else if (type.toLowerCase().equals("instructor")) {
                user = new Instructor(email, name, passwordHash);
            } else if (type.toLowerCase().equals("administrator")) {
                user = new Administrator(email, name, passwordHash);
            } else {
                throw new BadRequestException();
            }

            UnitOfWork.getCurrent().commit();
            return user;
        } catch (SQLException e) {
            try {
                UnitOfWork.getCurrent().rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if ("23505".equals(e.getSQLState())) {
                throw new ConflictException();
            }

            e.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    /**
     * Fetches all instructors for a given subject.
     *
     * @param subjectId subject's id
     * @return list of instructors
     */
    public List<Instructor> getInstructorsForSubject(UUID subjectId) {
        try (DBConnection connection = DBConnection.getCurrent()) {
            return instructorMapper.findAllForSubject(subjectId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new InternalServerErrorException();
        } finally {
            IdentityMap.reset();
        }
    }

    /**
     * Fetches all students for a given subject.
     *
     * @param subjectId subject's id
     * @return list of students
     */
    public List<Student> getStudentsForSubject(UUID subjectId) {
        try (DBConnection connection = DBConnection.getCurrent()) {
            return studentMapper.findAllForSubject(subjectId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new InternalServerErrorException();
        } finally {
            IdentityMap.reset();
        }
    }
}
